package com.krishna.Pujamart.order.service;

import com.krishna.Pujamart.cart.model.Cart;
import com.krishna.Pujamart.cart.model.CartItem;
import com.krishna.Pujamart.cart.repository.CartRepository;
import com.krishna.Pujamart.catalog.model.Product;
import com.krishna.Pujamart.catalog.model.ProductVariant;
import com.krishna.Pujamart.catalog.repository.ProductRepository;
import com.krishna.Pujamart.catalog.repository.ProductVariantRepository;
import com.krishna.Pujamart.identity.dto.ApiResponse;
import com.krishna.Pujamart.kits.model.PujaKit;
import com.krishna.Pujamart.kits.model.PujaKitItem;
import com.krishna.Pujamart.order.dto.CreateOrderRequest;
import com.krishna.Pujamart.order.dto.OrderResponse;
import com.krishna.Pujamart.order.dto.UpdateOrderStatusRequest;
import com.krishna.Pujamart.order.enums.OrderStatus;
import com.krishna.Pujamart.order.enums.PaymentStatus;
import com.krishna.Pujamart.order.exception.EmptyCartException;
import com.krishna.Pujamart.order.exception.InsufficientStockException;
import com.krishna.Pujamart.order.exception.InvalidOrderStateException;
import com.krishna.Pujamart.order.exception.OrderNotFoundException;
import com.krishna.Pujamart.order.model.*;
import com.krishna.Pujamart.order.repository.OrderRepository;
import com.krishna.Pujamart.order.utility.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final OrderMapper orderMapper;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ShippingService shippingService;

    @Override
    @Transactional
    public ApiResponse<OrderResponse> createOrderFromCart(UUID userId, CreateOrderRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EmptyCartException("Cart is empty or does not exist"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new EmptyCartException("Cannot place order with an empty cart");
        }

        // Validate stock and snapshot items
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .userId(userId)
                .orderStatus(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .shippingAddress(orderMapper.toShippingAddress(request.getShippingAddress()))
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;


        BigDecimal totalWeight = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            validateStock(cartItem);
            deductStock(cartItem);

            OrderItem orderItem = buildOrderItemSnapshot(cartItem);

            totalWeight = totalWeight.add(getTotalWeight(cartItem));

            subtotal = subtotal.add(orderItem.getTotalPrice());
            order.addOrderItem(orderItem);
        }

        BigDecimal shippingFee = shippingService.calculateShippingRate(request.getShippingAddress()
                .getPostalCode(),totalWeight,false,subtotal);
        BigDecimal taxAmount = BigDecimal.ZERO; // No tax for now
        BigDecimal totalAmount = subtotal.add(shippingFee).add(taxAmount);

        order.setSubtotalAmount(subtotal);
        order.setShippingFee(shippingFee);
        order.setTaxAmount(taxAmount);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        // Clear cart after successful order initialization
        cart.getItems().clear();
        cartRepository.save(cart);

        return ApiResponse.success("Order created successfully", orderMapper.toOrderResponse(savedOrder));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<OrderResponse> getOrderById(UUID userId, UUID orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        return ApiResponse.success("Order retrieved successfully", orderMapper.toOrderResponse(order));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Page<OrderResponse>> getUserOrders(UUID userId, Pageable pageable) {
                Page<Order> ordersPage = orderRepository.findByUserIdOrderByPlacedAtDesc(userId, pageable);

                if (!ordersPage.isEmpty()) {
                        List<UUID> orderIds = ordersPage.getContent().stream().map(Order::getId).toList();
                        orderRepository.findAllWithItemsByIds(orderIds);
                }

                return ApiResponse.success("User orders retrieved successfully", ordersPage.map(orderMapper::toOrderResponse));
    }
    @Override
    @Transactional
    public ApiResponse<OrderResponse> cancelOrder(UUID userId, UUID orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        // Allow cancellation only if order is in PENDING or CONFIRMED status
        if (order.getOrderStatus() != OrderStatus.PENDING && order.getOrderStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidOrderStateException("Order cannot be cancelled in status: " + order.getOrderStatus());
        }

        // Restore stock for each item in the order
        for (OrderItem item : order.getItems()) {
            restoreStock(item);
        }

        order.setOrderStatus(OrderStatus.CANCELLED);

        // If payment was already completed, mark payment as pending refund
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        Order savedOrder = orderRepository.save(order);
        return ApiResponse.success("Order cancelled successfully", orderMapper.toOrderResponse(savedOrder));
    }

    @Override
    @Transactional
    public ApiResponse<OrderResponse> updateOrderStatus(UUID orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        OrderStatus newStatus = request.getStatus();

        // Prevent transition to same status
        if (order.getOrderStatus() == newStatus) {
            return ApiResponse.success("Order status is already " + newStatus, orderMapper.toOrderResponse(order));
        }

        // Handle stock rollback if admin manually cancels the order
        if (newStatus == OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                restoreStock(item);
            }
        }

        order.setOrderStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        return ApiResponse.success("Order status updated successfully to " + newStatus, orderMapper.toOrderResponse(updatedOrder));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Page<OrderResponse>> getAllOrdersForAdmin(OrderStatus status, Pageable pageable) {
                Page<Order> ordersPage;
        if (status != null) {

                        ordersPage = orderRepository.findByOrderStatusOrderByPlacedAtDesc(status, pageable);
        } else {

                        ordersPage = orderRepository.findAllByOrderByPlacedAtDesc(pageable);
        }
                        // Pre-fetch items to prevent N+1 queries during mapping
                               if (!ordersPage.isEmpty()) {
                        List<UUID> orderIds = ordersPage.getContent().stream().map(Order::getId).toList();
                        orderRepository.findAllWithItemsByIds(orderIds);
                    }

                        return ApiResponse.success("Admin orders fetched successfully", ordersPage.map(orderMapper::toOrderResponse));
    }

    // Calculates the total weight of this cart item in kilograms (kg)
    public BigDecimal getTotalWeight(CartItem cartItem) {
        BigDecimal unitWeight = BigDecimal.ZERO;

        PujaKit kit = cartItem.getKit();
        ProductVariant variant = cartItem.getVariant();
        Product product = cartItem.getProduct();

        if (kit != null) {
            if (kit.getItems() != null) {
                for (PujaKitItem kitItem : kit.getItems()) {
                    BigDecimal itemUnitWeight = BigDecimal.ZERO;
                    if (kitItem.getVariant() != null && kitItem.getVariant().getWeight() != null) {
                        itemUnitWeight = kitItem.getVariant().getWeight();
                    } else if (kitItem.getProduct() != null && kitItem.getProduct().getWeight() != null) {
                        itemUnitWeight = kitItem.getProduct().getWeight();
                    } else {
                        itemUnitWeight = BigDecimal.ZERO;
                    }
                    BigDecimal totalItemWeight = itemUnitWeight.multiply(BigDecimal.valueOf(kitItem.getDefaultQuantity()));
                    unitWeight = unitWeight.add(totalItemWeight);
                }
            }
        } else if (variant != null) {
            if (variant.getWeight() != null) {
                unitWeight = variant.getWeight();
            } else {
                unitWeight = BigDecimal.ZERO;
            }
        } else if (product != null) {
            if (product.getWeight() != null) {
                unitWeight = product.getWeight();
            } else {
                unitWeight = BigDecimal.ZERO;
            }
        }

        return unitWeight.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
    }

    private void restoreStock(OrderItem item) {
        if (item.getKit() != null) {
            for (PujaKitItem kitItem : item.getKit().getItems()) {
                int restoreQuantity = kitItem.getDefaultQuantity() * item.getQuantity();
                if (kitItem.getVariant() != null) {
                    ProductVariant variant = kitItem.getVariant();
                    variant.setStockQuantity(variant.getStockQuantity() + restoreQuantity);
                    productVariantRepository.save(variant);
                } else if (kitItem.getProduct() != null) {
                    Product product = kitItem.getProduct();
                    product.setStockQuantity(product.getStockQuantity() + restoreQuantity);
                    productRepository.save(product);
                }
            }
        } else if (item.getVariant() != null) {
            ProductVariant variant = item.getVariant();
            variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
            productVariantRepository.save(variant);
        } else if (item.getProduct() != null) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }
    }

    private void deductStock(CartItem item) {
        if (item.getKit() != null) {
            for (PujaKitItem kitItem : item.getKit().getItems()) {
                int deductQuantity = kitItem.getDefaultQuantity() * item.getQuantity();
                if (kitItem.getVariant() != null) {
                    ProductVariant variant = kitItem.getVariant();
                    variant.setStockQuantity(variant.getStockQuantity() - deductQuantity);
                    productVariantRepository.save(variant);
                } else if (kitItem.getProduct() != null) {
                    Product product = kitItem.getProduct();
                    product.setStockQuantity(product.getStockQuantity() - deductQuantity);
                    productRepository.save(product);
                }
            }
        } else if (item.getVariant() != null) {
            ProductVariant variant = item.getVariant();
            variant.setStockQuantity(variant.getStockQuantity() - item.getQuantity());
            productVariantRepository.save(variant);
        } else if (item.getProduct() != null) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
        }
    }

    private void validateStock(CartItem item) {
        if (item.getKit() != null) {
            for (PujaKitItem kitItem : item.getKit().getItems()) {
                int requiredQuantity = kitItem.getDefaultQuantity() * item.getQuantity();

                if (kitItem.getVariant() != null) {
                    if (kitItem.getVariant().getStockQuantity() < requiredQuantity) {
                        throw new InsufficientStockException("Insufficient stock for kit item: " + kitItem.getVariant().getSku());
                    }
                } else if (kitItem.getProduct() != null) {
                    if (kitItem.getProduct().getStockQuantity() < requiredQuantity) {
                        throw new InsufficientStockException("Insufficient stock for kit item: " + kitItem.getProduct().getName());
                    }
                }
            }
        } else if (item.getVariant() != null) {
            if (item.getVariant().getStockQuantity() < item.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for variant: " + item.getVariant().getSku());
            }
        } else if (item.getProduct() != null) {
            if (item.getProduct().getStockQuantity() < item.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product: " + item.getProduct().getName());
            }
        }
    }


    private OrderItem buildOrderItemSnapshot(CartItem cartItem) {
        OrderItem.OrderItemBuilder builder = OrderItem.builder()
                .quantity(cartItem.getQuantity());

        if (cartItem.getKit() != null) {
            String imageUrl = (cartItem.getKit().getImageUrls() != null && !cartItem.getKit().getImageUrls().isEmpty())
                    ? cartItem.getKit().getImageUrls().get(0)
                    : null;

            BigDecimal unitPrice = cartItem.getKit().getActualPrice();
            builder.kit(cartItem.getKit())
                    .itemName(cartItem.getKit().getName())
                    .sku(null)
                    .unitPrice(unitPrice)
                    .totalPrice(unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                    .imageUrl(imageUrl);

        } else if (cartItem.getProduct() != null) {
            String imageUrl = (cartItem.getProduct().getImageUrls() != null && !cartItem.getProduct().getImageUrls().isEmpty())
                    ? cartItem.getProduct().getImageUrls().get(0)
                    : null;

            builder.product(cartItem.getProduct())
                    .imageUrl(imageUrl);

            if (cartItem.getVariant() != null) {
                BigDecimal unitPrice = cartItem.getVariant().getDiscountPriceOverride() != null
                        ? cartItem.getVariant().getDiscountPriceOverride()
                        : (cartItem.getVariant().getBasePriceOverride() != null
                        ? cartItem.getVariant().getBasePriceOverride()
                        : (cartItem.getProduct().getDiscountPrice()!=null
                ? cartItem.getProduct().getDiscountPrice():cartItem.getProduct().getPrice()));

                builder.variant(cartItem.getVariant())
                        .itemName(cartItem.getProduct().getName())
                        .sku(cartItem.getVariant().getSku())
                        .unitPrice(unitPrice)
                        .totalPrice(unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            } else {
                BigDecimal unitPrice = cartItem.getProduct().getDiscountPrice() != null
                        ? cartItem.getProduct().getDiscountPrice()
                        : cartItem.getProduct().getPrice();

                builder.itemName(cartItem.getProduct().getName())
                        .sku(cartItem.getProduct().getSku())
                        .unitPrice(unitPrice)
                        .totalPrice(unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            }
        }

        return builder.build();
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        int randomDigits = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return "PJM-" + timestamp + "-" + randomDigits;
    }
}
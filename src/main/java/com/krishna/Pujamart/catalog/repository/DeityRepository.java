package com.krishna.Pujamart.catalog.repository;

import com.krishna.Pujamart.catalog.model.Deity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface DeityRepository extends JpaRepository<Deity, UUID> {
    Optional<Deity> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}

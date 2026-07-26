package com.krishna.Pujamart.identity.utility;

import com.krishna.Pujamart.identity.dto.UserResponse;
import com.krishna.Pujamart.identity.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);
}


package org.unidelivery.user.mapper;

import org.mapstruct.Mapper;
import org.unidelivery.user.dto.ProfileResponse;
import org.unidelivery.user.dto.RegisterRequest;
import org.unidelivery.user.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(RegisterRequest registerRequest);
    ProfileResponse toProfileResponse(User user);
}

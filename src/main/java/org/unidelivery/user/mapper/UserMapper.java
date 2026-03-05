package org.unidelivery.user.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.unidelivery.user.dto.ProfileResponse;
import org.unidelivery.user.dto.RegisterRequest;
import org.unidelivery.user.dto.UpdateProfileRequestDTO;
import org.unidelivery.user.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(RegisterRequest registerRequest);
    ProfileResponse toProfileResponse(User user);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UpdateProfileRequestDTO dto, @MappingTarget User entity);
}

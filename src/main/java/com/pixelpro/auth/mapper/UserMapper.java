package com.pixelpro.auth.mapper;

import com.pixelpro.auth.dto.UserDto;
import com.pixelpro.auth.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roleName", expression = "java(entity.getRole().getRoleName().name())")
    UserDto toDto(UserEntity entity);
}


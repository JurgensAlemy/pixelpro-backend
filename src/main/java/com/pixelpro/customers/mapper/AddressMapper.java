package com.pixelpro.customers.mapper;

import com.pixelpro.customers.dto.shared.AddressDto;
import com.pixelpro.customers.entity.AddressEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AddressMapper {
    AddressDto toDto(AddressEntity entity);

    List<AddressDto> toDtoList(List<AddressEntity> entities);

    AddressEntity toEntity(AddressDto dto);

    // PATCH/UPDATE (evita settear campo por campo)
    void updateEntityFromDto(AddressDto dto, @MappingTarget AddressEntity entity);

    @AfterMapping
    default void normalize(@MappingTarget AddressEntity entity) {
        if (entity.getAddressType() != null) entity.setAddressType(entity.getAddressType().trim());
        if (entity.getDepartment() != null) entity.setDepartment(entity.getDepartment().trim());
        if (entity.getProvince() != null) entity.setProvince(entity.getProvince().trim());
        if (entity.getDistrict() != null) entity.setDistrict(entity.getDistrict().trim());
        if (entity.getAddressLine() != null) entity.setAddressLine(entity.getAddressLine().trim());
        if (entity.getAddressReference() != null) entity.setAddressReference(entity.getAddressReference().trim());
        if (entity.getAddressPhone() != null) entity.setAddressPhone(entity.getAddressPhone().trim());
    }

}

package com.pixelpro.customers.mapper;

import com.pixelpro.customers.dto.admin.CustomerAdminPatchRequest;
import com.pixelpro.customers.dto.admin.CustomerAdminRequest;
import com.pixelpro.customers.dto.admin.CustomerAdminResponse;
import com.pixelpro.customers.dto.web.CustomerWebPatchRequest;
import com.pixelpro.customers.dto.web.CustomerWebRequest;
import com.pixelpro.customers.dto.web.CustomerWebResponse;
import com.pixelpro.customers.entity.CustomerEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {AddressMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CustomerMapper {

    // Web mappings
    CustomerEntity toEntity(CustomerWebRequest dto);

    CustomerWebResponse toWebResponse(CustomerEntity entity);

    // PUT
    void updateFromWebRequest(CustomerWebRequest dto, @MappingTarget CustomerEntity entity);

    // PATCH: copia solo campos no nulos
    void updateFromWebPatch(CustomerWebPatchRequest dto, @MappingTarget CustomerEntity entity);

    // Admin mappings
    CustomerEntity toEntity(CustomerAdminRequest dto);

    CustomerAdminResponse toAdminResponse(CustomerEntity entity);

    List<CustomerAdminResponse> toAdminResponseList(List<CustomerEntity> entities);

    // PUT
    void updateFromAdminRequest(CustomerAdminRequest dto, @MappingTarget CustomerEntity entity);

    // PATCH
    void updateFromAdminPatch(CustomerAdminPatchRequest dto, @MappingTarget CustomerEntity entity);

    // ---------- HELPERS ----------
    // Si quisieras formatear nombres, emails, etc., puedes usar @AfterMapping
    @AfterMapping
    default void normalize(@MappingTarget CustomerEntity entity) {
        if (entity.getEmail() != null) {
            entity.setEmail(entity.getEmail().trim().toLowerCase());
        }
        if (entity.getFirstName() != null) {
            entity.setFirstName(entity.getFirstName().trim());
        }
        if (entity.getLastName() != null) {
            entity.setLastName(entity.getLastName().trim());
        }
    }
}

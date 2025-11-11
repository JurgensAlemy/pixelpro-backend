package com.pixelpro.customers.repository;

import com.pixelpro.customers.entity.CustomerEntity;
// --- CAMBIO AQUÍ ---
import com.pixelpro.auth.entity.UserEntity; // Apunta a la UserEntity de 'auth'
// --- FIN DE CAMBIO ---
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
    boolean existsByEmail(String email);

    boolean existsByDocumentNumber(String documentNumber);

    // Esta línea ahora funciona porque el import es correcto
    Optional<CustomerEntity> findByUserAccount(UserEntity userAccount);

    // Para UPDATE: validar unicidad excluyendo el propio id
    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByDocumentNumberAndIdNot(String documentNumber, Long id);

    // Búsqueda admin (ILIKE para búsqueda case-insensitive)
    @Query("""
            SELECT c
            FROM CustomerEntity c
            WHERE LOWER(CONCAT(COALESCE(c.firstName,''),' ',COALESCE(c.lastName,''))) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(c.email) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(c.documentNumber) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<CustomerEntity> search(@Param("q") String query, Pageable pageable);
}
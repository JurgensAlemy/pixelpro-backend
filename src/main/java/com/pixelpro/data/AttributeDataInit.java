package com.pixelpro.data;

import com.pixelpro.attributes.entity.AttributeEntity;
import com.pixelpro.attributes.entity.AttributeValueEntity;
import com.pixelpro.attributes.repository.AttributeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(2)
@RequiredArgsConstructor
public class AttributeDataInit implements CommandLineRunner {

    private final AttributeRepository attributeRepository;

    @Override
    public void run(String... args) {
        if (attributeRepository.count() > 0) {
            System.out.println("⚠️ Attributes ya estaban cargados, omitiendo inicialización...");
            return;
        }

        System.out.println("🎨 Cargando atributos base (color, talla)...");

        AttributeEntity color = AttributeEntity.builder()
                .name("Color")
                .build();

        // Valores de color
        List<String> colores = List.of("Rojo", "Negro", "Blanco", "Azul", "Amarillo", "Rosado");
        colores.forEach(valor -> color.getValues().add(
                AttributeValueEntity.builder()
                        .attributeValueName(valor)
                        .attribute(color)
                        .build()
        ));

        AttributeEntity talla = AttributeEntity.builder()
                .name("Talla")
                .build();

        // Valores de talla
        List<String> tallas = List.of("XS", "S", "M", "L", "XL");
        tallas.forEach(valor -> talla.getValues().add(
                AttributeValueEntity.builder()
                        .attributeValueName(valor)
                        .attribute(talla)
                        .build()
        ));

        attributeRepository.saveAll(List.of(color, talla));

        System.out.println("✅ Atributos y valores cargados correctamente.");
    }
}

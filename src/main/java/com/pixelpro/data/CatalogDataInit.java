package com.pixelpro.data;

import com.pixelpro.catalog.dto.admin.CategoryAdminCreateRequest;
import com.pixelpro.catalog.dto.admin.ProductAdminCreateRequest;
import com.pixelpro.catalog.dto.admin.ProductImageAdminRequest;
import com.pixelpro.catalog.service.CategoryService;
import com.pixelpro.catalog.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(3)
@RequiredArgsConstructor
public class CatalogDataInit implements CommandLineRunner {

    private final CategoryService categoryService;
    private final ProductService productService;

    @Override
    public void run(String... args) throws Exception {

        System.out.println("📚 Inicializando categorías y productos de catálogo...");

        // Evitar duplicado
        Long bikiniId = createCategory("Bikini");
        Long cacheteroId = createCategory("Cachetero");
        Long semiId = createCategory("Semi Hilo");
        Long topsitoId = createCategory("Topsito");

        // BIKINIS
        createProduct("Bikini Animal Print", "Bikini con estampado animal print, ideal para destacar en la playa con estilo y confianza.",
                bikiniId, "/uploads/products/bikinis/bikini-animal-print1.jpg");
        createProduct("Bikini Blonda", "Bikini con detalles de blonda delicada, combinación perfecta de sensualidad y elegancia.",
                bikiniId, "/uploads/products/bikinis/bikini-blonda.jpg");
        createProduct("Bikini Blondita Completa", "Conjunto completo de bikini con diseño suave y femenino, para un look delicado.",
                bikiniId, "/uploads/products/bikinis/bikini-blondita-completa.jpg");
        createProduct("Bikini Clásico", "Diseño clásico y atemporal, ideal para quienes prefieren un estilo tradicional.",
                bikiniId, "/uploads/products/bikinis/bikini-clasico.jpg");
        createProduct("Bikini Estrella", "Bikini con detalles de estrella que aportan un toque juvenil y divertido.",
                bikiniId, "/uploads/products/bikinis/bikini-estrella.jpg");
        createProduct("Bikini Pretina Ancha", "Cómodo bikini con pretina ancha que brinda mejor ajuste y soporte.",
                bikiniId, "/uploads/products/bikinis/bikini-pretina-ancha.jpg");

        // CACHETEROS
        createProduct("Cachetero Blonda con Logo", "Cachetero con elegantes acabados en blonda y logo decorativo.",
                cacheteroId, "/uploads/products/cacheteros/cachetero-blonda-con-logo.jpg");
        createProduct("Cachetero Blonda", "Cachetero clásico de blonda, suave y cómodo para el uso diario.",
                cacheteroId, "/uploads/products/cacheteros/cachetero-blonda.jpg");
        createProduct("Cachetero Corazón Estampado", "Cachetero con estampado de corazones, ideal para un look tierno y coqueto.",
                cacheteroId, "/uploads/products/cacheteros/cachetero-corazon-estampado-1.jpg");
        createProduct("Cachetero Corazón", "Cachetero con detalles románticos en forma de corazón, perfecto para ocasiones especiales.",
                cacheteroId, "/uploads/products/cacheteros/cachetero-corazon-1.jpg");
        createProduct("Cachetero Dije Estampado", "Cachetero con pequeño dije decorativo y estampados modernos.",
                cacheteroId, "/uploads/products/cacheteros/cachetero-dije-estampado-1.jpeg");
        createProduct("Cachetero Dije", "Cachetero elegante con dije en el frente, para un detalle extra de estilo.",
                cacheteroId, "/uploads/products/cacheteros/cachetero-dije-1.jpeg");
        createProduct("Cachetero Encaje Atrás", "Cachetero con diseño de encaje en la parte posterior, sexy y sofisticado.",
                cacheteroId, "/uploads/products/cacheteros/cachetero-encaje-atras-1.jpeg");
        createProduct("Cachetero Señorial Floreado", "Diseño floreado con estilo señorial, mezcla de tradición y elegancia.",
                cacheteroId, "/uploads/products/cacheteros/cachetero-señorial-floreado.jpg");

        // SEMI HILO
        createProduct("Semi Hilo Clásico", "Diseño clásico de semi hilo, para quienes buscan comodidad y sensualidad.",
                semiId, "/uploads/products/semi/semi-hilo-clasico.jpg");
        createProduct("Semi Hilo Dije", "Semi hilo decorado con dije, moderno y atractivo para cualquier ocasión.",
                semiId, "/uploads/products/semi/semi-hilo-dije.jpg");
        createProduct("Semi Hilo Pretina Ancha", "Semi hilo con pretina ancha que proporciona ajuste ideal y confort.",
                semiId, "/uploads/products/semi/semi-hilo-pretina-ancha.jpg");
        createProduct("Semi Hilo Señorial Juvenil", "Estilo que combina elegancia señorial con frescura juvenil.",
                semiId, "/uploads/products/semi/semi-señorial-juvenil-1.jpg");

        // TOPSITOS
        createProduct("Topsito Clásico", "Topsito de corte clásico, esencial para cualquier guardarropa íntimo.",
                topsitoId, "/uploads/products/topsitos/topsito-clasico.jpg");
        createProduct("Topsito con Tirante", "Topsito cómodo con tirantes ajustables, ideal para uso diario.",
                topsitoId, "/uploads/products/topsitos/topsito-con-tirante.jpg");
        createProduct("Topsito Olímpico", "Bikini estilo deportivo tipo topsito olímpico, brinda soporte y estilo.",
                topsitoId, "/uploads/products/topsitos/topsito-olimpico.jpg");

        System.out.println("✅ Catálogo inicial cargado con éxito.");
    }

    private Long createCategory(String name) {
        var saved = categoryService.create(new CategoryAdminCreateRequest(name, null));
        return saved.id();
    }

    private void createProduct(String name, String desc, Long catId, String imgPath) {
        ProductImageAdminRequest img = new ProductImageAdminRequest(imgPath, (byte) 1);
        productService.create(new ProductAdminCreateRequest(
                name,
                desc,
                List.of(catId),
                List.of(img)
        ));
    }
}

package com.pixelpro.data;

import com.pixelpro.catalog.entity.CategoryEntity;
import com.pixelpro.catalog.entity.ProductEntity;
import com.pixelpro.catalog.repository.CategoryRepository;
import com.pixelpro.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;


@Component
@Order(2)
@RequiredArgsConstructor
public class CatalogDataInit implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        System.out.println("📚 Inicializando categorías y productos de catálogo...");
        initializeCategories();
        initializeProducts();
        System.out.println("✅ Inicialización de catálogo completada.");
    }

    private void initializeCategories() {
        if (categoryRepository.count() == 0) {
            List<CategoryEntity> categories = List.of(
                    CategoryEntity.builder().name("Teclados").build(),
                    CategoryEntity.builder().name("Mouses").build(),
                    CategoryEntity.builder().name("Monitores").build(),
                    CategoryEntity.builder().name("Auriculares").build(),
                    CategoryEntity.builder().name("Graficas").build()
            );
            categoryRepository.saveAll(categories);
            System.out.println("   - Categorías base guardadas: 5");
        } else {
            System.out.println("   - Las categorías ya existen, omitiendo creación.");
        }
    }

    private void initializeProducts() {
        if (productRepository.count() > 0) {
            System.out.println("   - Los productos ya existen, omitiendo creación.");
            return;
        }

        List<CategoryEntity> existingCategories = categoryRepository.findAll();
        Map<String, CategoryEntity> categoryMap = existingCategories.stream()
                .collect(Collectors.toMap(CategoryEntity::getName, c -> c));

        List<ProductEntity> products = new ArrayList<>();
        Random random = new Random();

        // Estructura de datos
        Map<String, List<String>> productData = Map.of(
                "Teclados", List.of(
                        "Teclado_HyperX_Alloy_Core_Bluetooth",
                        "Teclado_HyperX_Alloy_Core_USB",
                        "Teclado_HyperX_Alloy_Origins_Bluetooth",
                        "Teclado_HyperX_Alloy_Origins_USB",
                        "Teclado_Logitech_G213_Bluetooth",
                        "Teclado_Logitech_G213_USB",
                        "Teclado_Logitech_G_Pro_TKL_Bluetooth",
                        "Teclado_Logitech_G_Pro_TKL_USB",
                        "Teclado_Razer_BlackWidow_V4_Pro_Bluetooth",
                        "Teclado_Razer_BlackWidow_V4_Pro_USB"
                ),
                "Auriculares", List.of(
                        "HyperX_Cloud_Core_Bluetooth",
                        "HyperX_Cloud_Stinger_Bluetooth",
                        "JBL_Quantum_350_USB",
                        "Logitech_G53_Bluetooth",
                        "Logitech_G335_Bluetooth",
                        "Logitech_G335_USB"
                ),
                "Graficas", List.of(
                        "RTX_3050",
                        "RTX_3080",
                        "RTX_4060",
                        "RTX_4090"
                ),
                "Monitores", List.of(
                        "Monitor_ASUS_TUF_GAMING_24",
                        "Monitor_ASUS_TUF_GAMING_27",
                        "Monitor_ASUS_TUF_GAMING_CURVADO_24",
                        "Monitor_ASUS_TUF_GAMING_CURVADO_27",
                        "Monitor_LG_24GN65R_24",
                        "Monitor_LG_24GN65R_27",
                        "Monitor_LG_24GS50F-B_24",
                        "Monitor_LG_24GS50F-B_27",
                        "Monitor_Samsung_Odyssey_G3_24",
                        "Monitor_Samsung_Odyssey_G3_27"
                ),
                "Mouses", List.of(
                        "HyperX_Pulsefire_FPS_Pro_Bluetooth",
                        "HyperX_Pulsefire_FPS_Pro_USB",
                        "HyperX_Pulsefire_Raid_Bluetooth",
                        "HyperX_Pulsefire_Raid_USB",
                        "Logitech_G203_Bluetooth",
                        "Logitech_G502_X_Bluetooth",
                        "Logitech_G502_X_USB",
                        "Razer_Basilisk_V3_Bluetooth"
                )
        );

        for (Map.Entry<String, List<String>> entry : productData.entrySet()) {
            String categoryName = entry.getKey();
            List<String> fileNames = entry.getValue();
            CategoryEntity category = categoryMap.get(categoryName);

            if (category == null) {
                System.err.println("¡Advertencia! Categoría no encontrada en BD: " + categoryName);
                continue;
            }

            for (String fileNameBase : fileNames) {
                String sku = generateSku(categoryName, fileNameBase);
                String name = formatProductName(fileNameBase, categoryName);
                String imageUrl = generateImageUrl(categoryName, fileNameBase);
                BigDecimal price = generatePrice(categoryName, random);
                int stock = random.nextInt(50) + 10;

                String description = generateProductDescription(categoryName, name, fileNameBase);

                ProductEntity product = ProductEntity.builder()
                        .sku(sku)
                        .name(name)
                        .model(getModelFromFileName(fileNameBase))
                        .description(description)
                        .price(price)
                        .imageUrl(imageUrl)
                        .status("ACTIVO")
                        .qtyStock(stock)
                        .category(category)
                        .build();

                products.add(product);
            }
        }

        productRepository.saveAll(products);
        System.out.println("   - Productos de catálogo guardados: " + products.size());
    }

    // ----------------------------------------------------------------------
    // --- MÉTODOS AUXILIARES ---
    // ----------------------------------------------------------------------

    private String generateProductDescription(String category, String name, String fileNameBase) {
        String connectivity = fileNameBase.contains("Bluetooth") ? "Inalámbrica Bluetooth" : "Con cable USB";

        String[] nameParts = name.split(" ");
        String brand = nameParts[0];

        switch (category) {
            case "Teclados":
                if (fileNameBase.contains("TKL")) {
                    return String.format(
                            "¡Máxima precisión y portabilidad! Este %s %s es ideal para gaming competitivo. Su diseño Tenkeyless (TKL) te da más espacio para el mouse. Conexión %s y switches táctiles de alta respuesta. ¡Dominarás cada partida!",
                            category, name, connectivity
                    );
                }
                return String.format(
                        "El %s %s redefine la experiencia de escritura y juego. Ofrece una construcción robusta de grado aeronáutico y una conexión %s estable. Disfruta de una respuesta ultrarrápida, esencial para el trabajo o el entretenimiento.",
                        category, name, connectivity
                );
            case "Mouses":
                if (fileNameBase.contains("Razer")) {
                    return String.format(
                            "El %s %s de %s, diseñado para el rendimiento. Cuenta con un sensor óptico de última generación de alta precisión y un diseño ergonómico para largas sesiones de juego. Configurable a través de software, con conexión %s.",
                            category, name, brand, connectivity
                    );
                }
                return String.format(
                        "Mouse %s %s: Tu arma secreta para la victoria. Ofrece un seguimiento preciso y un peso optimizado para movimientos rápidos. La conexión %s garantiza cero latencia. Perfecto para juegos FPS y MOBA.",
                        category, name, connectivity
                );
            case "Monitores":
                String size = name.contains("24") ? "24 pulgadas" : "27 pulgadas";
                String curved = name.contains("CURVADO") ? " con inmersiva pantalla curva" : "";
                return String.format(
                        "Monitor %s %s de %s. Disfruta de una experiencia visual impresionante con su panel de %s y alta tasa de refresco. Los colores vibrantes y el tiempo de respuesta ultrarrápido lo hacen ideal para el gaming profesional.",
                        brand, name, size, curved
                );
            case "Auriculares":
                return String.format(
                        "Sumérgete en el audio 7.1 con los %s %s. Diseñados para la comodidad extrema en largas horas de uso, estos auriculares ofrecen un micrófono con cancelación de ruido y una calidad de sonido cristalina. Conexión %s para total libertad.",
                        brand, name, connectivity
                );
            case "Graficas":
                String series = name.replace("RTX ", "");

                return String.format(
                        "Potencia tu PC con la tarjeta gráfica %s %s. Experimenta el trazado de rayos (Ray Tracing) en tiempo real y resoluciones 4K. Ideal para los títulos más exigentes del mercado. Un componente esencial para el PC gamer de alta gama.",
                        brand, series
                );
            default:
                return "Dispositivo de gaming de alto rendimiento. Ideal para entusiastas y profesionales.";
        }
    }

    private String generateSku(String category, String filename) {
        String prefix = category.substring(0, 3).toUpperCase();
        String modelCode = filename.substring(0, Math.min(filename.length(), 4)).toUpperCase();
        return String.format("%s-%s-%03d", prefix, modelCode, new Random().nextInt(999));
    }

    private String generateImageUrl(String category, String fileNameBase) {
        String safeCategory = category.toLowerCase();
        return String.format("/uploads/products/%s/%s.webp", safeCategory, fileNameBase);
    }

    private BigDecimal generatePrice(String category, Random random) {
        double base = switch (category) {
            case "Graficas" -> random.nextInt(2000) + 500;
            case "Monitores" -> random.nextInt(500) + 150;
            case "Auriculares", "Mouses" -> random.nextInt(100) + 50;
            default -> random.nextInt(150) + 80;
        };

        // --- LÍNEA CORREGIDA ---
        return BigDecimal.valueOf(base + (random.nextInt(99) / 100.0)).setScale(2, RoundingMode.HALF_UP);
    }

    private String formatProductName(String fileName, String categoryName) {
        // 1. Reemplazar guiones bajos por espacios
        String rawName = fileName.replace("_", " ");

        // 2. Obtener el singular gramaticalmente correcto
        String singularCategory = getSingularCategoryName(categoryName);

        // 3. Verificar si ya empieza con la categoría (Case Insensitive)
        if (rawName.toLowerCase().startsWith(singularCategory.toLowerCase())) {
            return rawName;
        } else if (rawName.toLowerCase().startsWith(categoryName.toLowerCase())) {
            return rawName;
        }

        // 4. Si no tiene el prefijo, lo agregamos
        return singularCategory + " " + rawName;
    }

    // Nuevo auxiliar para singularizar correctamente
    private String getSingularCategoryName(String plural) {
        return switch (plural) {
            case "Auriculares" -> "Auricular";
            case "Monitores" -> "Monitor";
            case "Mouses" -> "Mouse"; // Mouse es un anglicismo, su singular es igual o Mouse
            case "Teclados" -> "Teclado";
            case "Graficas" -> "Grafica";
            default -> {
                if (plural.endsWith("es")) {
                    yield plural.substring(0, plural.length() - 2);
                } else if (plural.endsWith("s")) {
                    yield plural.substring(0, plural.length() - 1);
                }
                yield plural;
            }
        };
    }

    private String getModelFromFileName(String fileName) {
        String[] parts = fileName.split("_");
        if (parts.length > 1) {
            return parts[parts.length - 1];
        }
        return "";
    }
}
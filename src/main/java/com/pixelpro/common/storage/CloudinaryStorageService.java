package com.pixelpro.common.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryStorageService implements FileStorageService {

    // Inyección de credenciales desde application.properties
    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    private Cloudinary cloudinary;

    /**
     * Inicializa el cliente de Cloudinary una vez al arrancar la aplicación.
     */
    @PostConstruct
    public void init() {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true // Forzar HTTPS
        ));
    }

    @Override
    public String uploadProductImage(MultipartFile file) {
        try {
            // Subimos el archivo (convertido a bytes)
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    // Configuramos la subida:
                    // "folder": Organiza las imágenes en una carpeta 'pixelpro/products' dentro de tu nube
                    // "resource_type": "auto" detecta si es jpg, png, etc.
                    ObjectUtils.asMap(
                            "folder", "pixelpro/products",
                            "resource_type", "auto",
                            // --- AQUÍ ESTÁ LA MAGIA ---
                            // 1. Transformation: Redimensionar si es muy grande (ahorra storage)
                            //    c_limit: No estira imágenes pequeñas, solo encoge las grandes.
                            //    w_800: Ancho máximo 800px.
                            //    q_auto: Calidad inteligente (reduce peso sin perder nitidez visible).
                            //    f_auto: Guarda en el mejor formato posible (WebP/AVIF) si quieres,
                            //            o déjalo para la entrega.
                            "transformation", "c_limit,w_800,q_auto,f_auto"
                    ));

            // Obtenemos y retornamos la URL segura (HTTPS)
            // Ejemplo: https://res.cloudinary.com/tu-cloud/image/upload/v1234/pixelpro/products/imagen.jpg
            return (String) uploadResult.get("secure_url");

        } catch (IOException e) {
            throw new RuntimeException("Error crítico al subir imagen a Cloudinary: " + file.getOriginalFilename(), e);
        }
    }
}
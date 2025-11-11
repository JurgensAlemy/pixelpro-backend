package com.pixelpro.common.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class LocalFileStorageService implements FileStorageService {

    @Value("${app.upload.dir.products}")
    private String uploadDir;

    @Override
    public String uploadProductImage(MultipartFile file) {
        try {
            // Convertir la ruta a un Path
            Path uploadPath = Paths.get(uploadDir);
            // Crear el directorio si no existe
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            // Generar un nombre de archivo único
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            // Combinar la ruta con el nombre del archivo (uploadPath/filename)
            Path filePath = uploadPath.resolve(filename);
            // Guardar el archivo en el sistema de archivos local
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            // URL relativa HTTP para acceder al archivo
            return "/uploads/products/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }
}

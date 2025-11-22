package com.WeedTitlan.server.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.WeedTitlan.server.dto.CloudinaryUploadResult;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
        @Value("${cloudinary.cloud-name}") String cloudName,
        @Value("${cloudinary.api-key}") String apiKey,
        @Value("${cloudinary.api-secret}") String apiSecret
    ) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret,
            "secure", true
        ));
    }

    public CloudinaryUploadResult subirImagen(MultipartFile file) throws IOException {

        // VALIDACIÓN DE TIPO
        String contentType = file.getContentType();
        if (contentType == null ||
            !(contentType.equals("image/jpeg") ||
              contentType.equals("image/png")  ||
              contentType.equals("image/webp"))) {

            throw new IllegalArgumentException(
                "Formato no permitido. Solo JPG, PNG y WEBP."
            );
        }

        String nombre = file.getOriginalFilename().toLowerCase();
        if (!(nombre.endsWith(".jpg") || nombre.endsWith(".jpeg")
                || nombre.endsWith(".png") || nombre.endsWith(".webp"))) {
            throw new IllegalArgumentException("Extensión no permitida.");
        }

        // ARCHIVO TEMPORAL
        File tempFile = File.createTempFile("upload-", nombre);
        file.transferTo(tempFile);

        // SUBIDA CON COMPRESIÓN MÁXIMA
        Map<?, ?> result = cloudinary.uploader().upload(
            tempFile,
            ObjectUtils.asMap(
                "folder", "productos",
                "resource_type", "image",

                // 🔥 COMPRESIÓN REAL
                "quality", "auto:eco",   // eco = muy optimizado sin perder calidad
                "format", "webp",        // conversión obligatoria a WebP (muy ligero)

                // 🔥 Limitar tamaño final
                "width", 1000,
                "height", 1000,
                "crop", "limit"
            )
        );

        tempFile.delete();

        return new CloudinaryUploadResult(
            result.get("secure_url").toString(),
            result.get("public_id").toString()
        );
    }


    public boolean eliminarImagen(String publicId) {
        try {
            Map<?, ?> result = cloudinary.uploader().destroy(
                publicId,
                ObjectUtils.asMap(
                    "resource_type", "image",
                    "type", "upload",
                    "invalidate", true
                )
            );
            return "ok".equals(result.get("result"));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String extraerPublicIdDesdeUrl(String url) {
        if (url == null || url.isEmpty()) return null;

        int startIndex = url.indexOf("/upload/") + 8;
        String pathWithExtension = url.substring(startIndex);

        return pathWithExtension.replaceFirst("\\.[a-zA-Z0-9]+$", "");
    }
}

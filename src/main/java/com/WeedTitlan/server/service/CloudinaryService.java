package com.WeedTitlan.server.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.WeedTitlan.server.dto.CloudinaryUploadResult;

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

        // ============================
        //  VALIDACIÓN DE TIPO DE ARCHIVO
        // ============================
        String contentType = file.getContentType();

        if (contentType == null || 
            !(contentType.equals("image/jpeg") ||
              contentType.equals("image/png")  ||
              contentType.equals("image/webp"))) {

            throw new IllegalArgumentException(
                    "Formato no permitido. Solo se aceptan JPG, PNG y WEBP."
            );
        }


        // ============================
        //  SUBIDA A CLOUDINARY CON COMPRESIÓN
        // ============================
        Map<?, ?> result = cloudinary.uploader().upload(
            file.getBytes(),
            ObjectUtils.asMap(
                "folder", "productos",
                "resource_type", "image",
                "quality", "auto",        // compresión inteligente
                "fetch_format", "auto",   // convierte a webp/avif si es posible
                "crop", "limit",          // limita tamaño máximo
                "width", 1000,
                "height", 1000
            )
        );

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
        return pathWithExtension.replaceFirst("\\.[a-zA-Z0-9]+$", ""); // Quita la extensión .jpg, .png, etc.
    }


}
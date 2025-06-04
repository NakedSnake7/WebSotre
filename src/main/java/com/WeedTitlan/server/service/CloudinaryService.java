package com.WeedTitlan.server.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    public String subirImagen(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("temp-", file.getOriginalFilename());
        file.transferTo(tempFile);

        Map<?, ?> result = cloudinary.uploader().upload(tempFile, ObjectUtils.emptyMap());

        tempFile.delete(); // Limpieza del archivo temporal
        return result.get("secure_url").toString();
    }
}
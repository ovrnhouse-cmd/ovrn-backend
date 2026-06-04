package com.Ishwarjit.Wolf_OVRN_backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String upload(MultipartFile file) throws IOException {
        return upload(file, "wolf-ovrn/products");
    }

    public String upload(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        Map<String, Object> options = ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "image");
        @SuppressWarnings("unchecked")
        Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), options);
        return Objects.toString(result.get("secure_url"), null);
    }
}

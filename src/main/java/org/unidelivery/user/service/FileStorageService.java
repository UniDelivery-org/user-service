package org.unidelivery.user.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {


    public String storeAvatar(MultipartFile file) {
        String uploadDir = "uploads/avatars/";
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir + fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
            return "/uploads/avatars/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store avatar", e);
        }
    }
}
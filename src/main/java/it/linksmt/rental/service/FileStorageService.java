package it.linksmt.rental.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {
    @Value("${file.upload-dir}")
    private String uploadDir;

    public String storeFile(MultipartFile file) throws IOException {
        //generate unique Filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        //save the file
        Path targetLocation = Paths.get(uploadDir, uniqueFileName);
        Files.copy(file.getInputStream(),targetLocation);
        return uniqueFileName;
    }

    public Resource getImage(String imagePath) throws IOException {
        Path filePath = Paths.get(uploadDir, imagePath);
        Resource resource = new UrlResource(filePath.toUri());
        if(resource.exists()){
            return resource;
        }
        else
            return null;
    }
}

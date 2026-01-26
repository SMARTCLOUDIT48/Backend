package com.scit48.common.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${file.upload.profile-dir}")
    private String profileUploadDir;

    public String saveProfileImage(MultipartFile file) {

        if (file == null || file.isEmpty())
            return null;

        try {
            String original = file.getOriginalFilename();
            String ext = original.substring(original.lastIndexOf("."));

            String savedName = UUID.randomUUID() + ext;
            Path savePath = Paths.get(profileUploadDir, savedName);

            Files.createDirectories(savePath.getParent());
            file.transferTo(savePath.toFile());

            return savedName;

        } catch (Exception e) {
            throw new RuntimeException("프로필 이미지 저장 실패", e);
        }
    }
}

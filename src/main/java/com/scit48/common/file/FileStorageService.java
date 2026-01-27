package com.scit48.common.file;

import com.scit48.common.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload.profile-dir}")
    private String profileUploadDir;

    private static final List<String> ALLOWED_EXT = List.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    public String saveProfileImage(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("이미지가 없습니다.");
        }

        String original = file.getOriginalFilename();

        if (original == null || !original.contains(".")) {
            throw new BadRequestException("잘못된 파일 형식입니다.");
        }

        String ext = original.substring(original.lastIndexOf(".")).toLowerCase();

        if (!ALLOWED_EXT.contains(ext)) {
            throw new BadRequestException("이미지 파일만 업로드 가능합니다.");
        }

        try {
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

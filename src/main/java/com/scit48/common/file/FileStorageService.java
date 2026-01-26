package com.scit48.common.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 파일 저장 서비스 (로컬)
 * - 추후 S3로 교체 가능
 */
@Service
public class FileStorageService {

    @Value("${file.upload-path}")
    private String uploadPath;

    public String save(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("잘못된 파일 형식입니다.");
        }

        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        String savedFileName = UUID.randomUUID() + ext;

        File dir = new File(uploadPath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("업로드 디렉토리 생성 실패");
        }

        try {
            file.transferTo(new File(dir, savedFileName));
            return savedFileName;
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }
}

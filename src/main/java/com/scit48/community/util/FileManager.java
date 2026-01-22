package com.scit48.community.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class FileManager {
	
	/**
	 * 파일을 저장하고 저장된 파일명을 리턴한다.
	 *
	 * @param path 폴더의 절대경로
	 * @param file 저장할 파일 정보
	 * @return 저장된 파일명
	 * @throws IOException 파일 저장 중 발생한 예외
	 */
	public String saveFile(String path, MultipartFile file) throws IOException {
		
		// 디렉토리가 없으면 생성
		File directoryPath = new File(path);
		
		// isDirectory()는 directoryPath가 실제로 존재하는 디렉토리인지 검사. 존재하지 않거나 파일일 경우 false 리턴
		if (!directoryPath.isDirectory()) {
			// mkdirs()는 경로상에 존재하지 않는 디렉토리들을 모두 생성
			directoryPath.mkdirs();
		}
		
		// ==== 서버에 저장할 파일명 생성 ====
		// 파일의 원래 이름
		String originalFileName = file.getOriginalFilename();
		//원래 이름의 확장자
		String extension = originalFileName
				.substring(originalFileName.lastIndexOf("."));
		//오늘 날짜를 문자열로 변환
		String dateString = LocalDate.now()
				.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		//UUID(Universally Unique Identifier) 생성
		//UUID는 128비트 숫자로 고유한 식별자를 생성하기 위한 표준.
		//예를 들어 "123e4567-e89b-12d3-a456-426614174000" 형태(보통 36자리 문자열)
		String uuidString = UUID.randomUUID().toString();
		String fileName = dateString + "_" + uuidString + extension;
		
		// 파일 복사하여 저장
		File filePath = new File(directoryPath + "/" + fileName);
		// 업로드된 파일을 지정한 경로(filePath)에 실제로 저장하는 역할
		file.transferTo(filePath);
		
		log.debug("파일 정보 : 원래 이름: {}, 저장된 이름: {}, 크기: {} bytes"
				, file.getOriginalFilename(), fileName, file.getSize());
		return fileName;
	}
	
	/**
	 * 지정된 경로와 파일명으로 디스크에서 파일을 삭제한다.
	 *
	 * @param path 파일이 위치한 폴더의 절대경로
	 * @param fileName 삭제할 파일명
	 * @return 파일 삭제 성공 여부
	 */
	public boolean deleteFile(String path, String fileName) throws Exception {
		/*
    		Path, Files
    		  - java.nio.file 패키지에서 제공하는 파일 및 디렉토리 작업을 위한 핵심 클래스들
    		  - Path: 파일 시스템 내의 경로(디렉토리 + 파일 이름 포함)를 추상적으로 표현한 객체(Path는 변경 불가능 (immutable))
    		  		  인터페이스 (실제 구현은 UnixPath, WindowsPath)
    		  - Files: Path 객체를 기반으로 실제 파일에 대해 작업하는 정적 메서드 모음 클래스
    		  		   복사, 삭제, 이동, 읽기, 쓰기 등 다양한 작업을 제공
    	 */
		// 파일의 전체 경로(Path) 객체를 생성
		Path filePath = Paths.get(path, fileName);
		// 해당 파일이 존재하면 삭제하고, true 반환 or 존재하지 않으면 false 반환
		return Files.deleteIfExists(filePath);
	}
}

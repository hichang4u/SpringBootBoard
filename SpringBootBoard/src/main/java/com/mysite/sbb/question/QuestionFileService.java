package com.mysite.sbb.question;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class QuestionFileService {

    private final QuestionFileRepository questionFileRepository;

    @Value("${spring.file.upload-dir}")
    private String uploadDir;
    
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    public QuestionFile saveFile(MultipartFile file, Question question) throws IOException {
    	String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String storedFileName = UUID.randomUUID().toString();
        String fileExtension = getFileExtension(originalFileName);
        storedFileName += fileExtension;

        Path filePath = Paths.get(uploadDir, storedFileName);

        try {
            Files.copy(file.getInputStream(), filePath);
        } catch (IOException e) {
            throw new IOException("Could not save file: " + originalFileName, e);
        }

        QuestionFile questionFile = new QuestionFile();
        questionFile.setOriginalFileName(originalFileName);
        questionFile.setStoredFileName(storedFileName);
        questionFile.setFilePath(filePath.toString());
        questionFile.setQuestion(question);

        return questionFileRepository.save(questionFile);
    }
    
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }
    
    public Optional<QuestionFile> getFile(Long fileId) {
        return questionFileRepository.findById(fileId);
    }

    public Resource loadFileAsResource(String storedFileName) throws MalformedURLException, FileNotFoundException {
        Path filePath = Paths.get(uploadDir).resolve(storedFileName).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        if(resource.exists()) {
            return resource;
        } else {
            throw new FileNotFoundException("File not found: " + storedFileName);
        }
    }
    
    public List<QuestionFile> getFilesByQuestion(Question question) {
        return questionFileRepository.findByQuestion(question);
    }
    
    public void deleteFiles(List<Long> fileIds) throws IOException {
        List<QuestionFile> filesToDelete = questionFileRepository.findAllById(fileIds);
        for (QuestionFile file : filesToDelete) {
        	Path filePath = Paths.get(uploadDir, file.getStoredFileName());
        	try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                throw new IOException("Could not delete file: " + file.getOriginalFileName(), e);
            }
            questionFileRepository.delete(file);
        }
    }
}

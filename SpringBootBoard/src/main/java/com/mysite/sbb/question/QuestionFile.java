package com.mysite.sbb.question;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class QuestionFile {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	private String originalFileName;  // 원본 파일명
    private String storedFileName;    // 저장된 파일명 (UUID)
    private String filePath;          // 파일 경로

    @ManyToOne
    private Question question;
}

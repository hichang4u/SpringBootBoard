package com.mysite.sbb.question;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionFileRepository extends JpaRepository<QuestionFile, Long> {
	List<QuestionFile> findByQuestion(Question question);
}

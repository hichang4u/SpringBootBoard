package com.mysite.sbb.answer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mysite.sbb.question.Question;
import com.mysite.sbb.user.SiteUser;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {
	Page<Answer> findByQuestion(Question question, Pageable pageable);
	Page<Answer> findByAuthor(SiteUser siteUser, Pageable pageable);
	Page<Answer> findAll(Specification<Answer> spec, Pageable pageable);
}

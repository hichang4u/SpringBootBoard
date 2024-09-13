package com.mysite.sbb.comment;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mysite.sbb.question.Question;
import com.mysite.sbb.user.SiteUser;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
	List<Comment> findByQuestion(Question question);
	Page<Comment> findByAuthor(SiteUser siteUser, Pageable pageable);
}

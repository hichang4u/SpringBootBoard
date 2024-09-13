package com.mysite.sbb.comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.user.SiteUser;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;

    // 댓글 조회
    public Comment getComment(int id) {
        Optional<Comment> comment = this.commentRepository.findById(id);
        if (comment.isPresent()) {
            return comment.get();
        } else {
            throw new DataNotFoundException("comment not found");
        }
    }

    // 댓글 등록
    public Comment create(String content
    					, Question question
    					, Answer answer
    					, SiteUser siteUser) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setQuestion(question);
        comment.setAnswer(answer);
        comment.setAuthor(siteUser);
        comment.setCreateDate(LocalDateTime.now());
        this.commentRepository.save(comment);
        return comment;
    }

    // 댓글 목록
    public List<Comment> getCommentList(Question question) {
        return this.commentRepository.findByQuestion(question);
    }
    
    // 댓글 삭제
    public void delete(Comment comment) {
        this.commentRepository.delete(comment);
    }
    
    // 댓글 목록(페이징)
    public Page<Comment> getListByAuthor(int page, SiteUser user) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 5, Sort.by(sorts));
        return this.commentRepository.findByAuthor(user, pageable);
    }
}
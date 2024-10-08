package com.mysite.sbb.question;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.category.Category;
import com.mysite.sbb.user.SiteUser;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    
    // 검색(JPA의 Specification 인터페이스 사용)
    private Specification<Question> search(String kw) {
        return new Specification<>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Predicate toPredicate(Root<Question> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
                query.distinct(true);  // 중복을 제거 
                Join<Question, SiteUser> u1 = q.join("author", JoinType.LEFT);
                Join<Question, Answer> a = q.join("answerList", JoinType.LEFT);
                Join<Answer, SiteUser> u2 = a.join("author", JoinType.LEFT);
                return cb.or(cb.like(q.get("subject"), "%" + kw + "%"), 	 // 제목 
                        	 cb.like(q.get("content"), "%" + kw + "%"),      // 내용 
                        	 cb.like(u1.get("username"), "%" + kw + "%"),    // 질문 작성자 
                        	 cb.like(a.get("content"), "%" + kw + "%"),      // 답변 내용 
                        	 cb.like(u2.get("username"), "%" + kw + "%")	 // 답변 작성자
                            );    
            }
        };
    }

    // 목록 조회(페이징)
    public Page<Question> getList(int page, String kw) {
    	List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        // JPA Specification 사용
//        Specification<Question> spec = search(kw);
//        return this.questionRepository.findAll(spec, pageable);
        // @Query 애너테이션 사용
        return this.questionRepository.findAllByKeyword(kw, pageable);
    }
    
    // 상세 조회
    public Question getQuestion(Integer id) {  
        Optional<Question> question = this.questionRepository.findById(id);
        if (question.isPresent()) {
        	// 조회수 1 증가
        	Question question_views = question.get();
        	question_views.setViews(question_views.getViews() + 1);
            this.questionRepository.save(question_views);
            return question_views;
        } else {
            throw new DataNotFoundException("question not found");
        }
    }
    
    // 저장
    public Question create(String subject, String content, SiteUser user, Category category) {
        Question q = new Question();
        q.setSubject(subject);
        q.setContent(content);
//        q.setCreateDate(LocalDateTime.now());
        q.setAuthor(user);
        q.setCategory(category);
        this.questionRepository.save(q);
        
        return q;
    }
    
    // 수정
    public void modify(Question question, String subject, String content, Category category) {
        question.setSubject(subject);
        question.setContent(content);
//        question.setModifyDate(LocalDateTime.now());
        question.setCategory(category);
        this.questionRepository.save(question);
    }
    
    // 삭제
    public void delete(Question question) {
        this.questionRepository.delete(question);
    }
    
    // 추천
    public void vote(Question question, SiteUser siteUser) {
        question.getVoter().add(siteUser);
        this.questionRepository.save(question);
    }
    
    // 내정보 질문 페이징
    public Page<Question> getListByAuthor(int page, SiteUser siteUser) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 5, Sort.by(sorts));
        return this.questionRepository.findByAuthor(siteUser, pageable);
    }
    
    // 추천한 글은 기존에 Set로 정의한 voter에서 특정 SiteUser가 그 Set에 존재하는지 확인
    public Specification<Question> hasVoter(SiteUser siteUser) {
        return new Specification<Question>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Question> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
                query.distinct(true);
                return cb.isMember(siteUser, q.get("voter"));
            }
        };
    }
    
    // 추천한 질문 조회
    public Page<Question> getListByVoter(int page, SiteUser siteUser) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 5, Sort.by(sorts));
        Specification<Question> spec = this.hasVoter(siteUser);
        return this.questionRepository.findAll(spec, pageable);
    }
    
    // 카테고리별 질문 목록 조회
    public Page<Question> getCategoryQuestionList(Category category, int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        return this.questionRepository.findByCategory(category, pageable);
    }
}
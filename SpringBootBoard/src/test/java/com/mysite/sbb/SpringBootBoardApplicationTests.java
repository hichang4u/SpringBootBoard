package com.mysite.sbb;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mysite.sbb.answer.AnswerService;
import com.mysite.sbb.category.CategoryService;
import com.mysite.sbb.comment.CommentService;
import com.mysite.sbb.question.QuestionService;
import com.mysite.sbb.user.UserService;

@SpringBootTest
class SpringBootBoardApplicationTests {

	@Autowired
	private QuestionService questionService;

	@Autowired
	private AnswerService answerService;

	@Autowired
	private UserService userService;
	
	@Autowired
    private CommentService commentService;
	
	@Autowired
    private CategoryService categoryService;

	@Test
	void testJpa() {        
        
        
	}

}

package com.mysite.sbb.user;

import java.security.Principal;
import java.security.SecureRandom;
import java.util.Random;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.answer.AnswerService;
import com.mysite.sbb.comment.Comment;
import com.mysite.sbb.comment.CommentService;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

	private final QuestionService questionService;
	private final AnswerService answerService;
	private final UserService userService;
	private final JavaMailSender mailSender;
	private final CommentService commentService;

    @GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm) {
        return "user/signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "user/signup";
        }

        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect", 
                    "2개의 패스워드가 일치하지 않습니다.");
            return "user/signup";
        }

        try {
            userService.create(userCreateForm.getUsername(), 
                    userCreateForm.getEmail(), userCreateForm.getPassword1());
        }catch(DataIntegrityViolationException e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            return "user/signup";
        }catch(Exception e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", e.getMessage());
            return "user/signup";
        }

        return "redirect:/";
    }
    
    @GetMapping("/login")
    public String login() {
        return "user/login";
    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public String profile(Model model
    					, UserUpdateForm userUpdateForm
			 			, @RequestParam(value="question-page", defaultValue="0") int questionPage
                        , @RequestParam(value="ans-page", defaultValue="0") int ansPage
                        , @RequestParam(value="question-vote-page", defaultValue="0") int questionVoterPage
                        , @RequestParam(value="ans-vote-page", defaultValue="0") int ansVoterPage
                        , @RequestParam(value="comment-page", defaultValue="0") int commentPage
                        , Principal principal) {
        SiteUser siteUser = this.userService.getUser(principal.getName());
        Page<Question> wroteQuestions = this.questionService.getListByAuthor(questionPage, siteUser);
        Page<Answer> wroteAnswers = this.answerService.getListByAuthor(ansPage, siteUser);
        Page<Question> votedQuestions = this.questionService.getListByVoter(questionVoterPage, siteUser);
        Page<Answer> votedAnswers = this.answerService.getListByVoter(ansVoterPage, siteUser);
        Page<Comment> wroteComments = this.commentService.getListByAuthor(commentPage, siteUser);

        model.addAttribute("wrote_question_paging", wroteQuestions);
        model.addAttribute("wrote_answer_paging", wroteAnswers);
        model.addAttribute("voted_question_paging", votedQuestions);
        model.addAttribute("voted_answer_paging", votedAnswers);
        model.addAttribute("username", siteUser.getUsername());
        model.addAttribute("userEmail", siteUser.getEmail());
        model.addAttribute("wrote_comment_paging", wroteComments);
        return "user/profile";
    }
    
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/profile")
    public String update(@Valid UserUpdateForm userUpdateForm
    				   , BindingResult bindingResult
    				   , Model model
    				   , Principal principal) {
        SiteUser siteUser = this.userService.getUser(principal.getName());
        Page<Question> wroteQuestions = this.questionService.getListByAuthor(0, siteUser);
        Page<Answer> wroteAnswers = this.answerService.getListByAuthor(0, siteUser);
        Page<Question> votedQuestions = this.questionService.getListByVoter(0, siteUser);
        Page<Answer> votedAnswers = this.answerService.getListByVoter(0, siteUser);
        Page<Comment> wroteComments = this.commentService.getListByAuthor(0, siteUser);

        model.addAttribute("wrote_question_paging", wroteQuestions);
        model.addAttribute("wrote_answer_paging", wroteAnswers);
        model.addAttribute("voted_question_paging", votedQuestions);
        model.addAttribute("voted_answer_paging", votedAnswers);
        model.addAttribute("username", siteUser.getUsername());
        model.addAttribute("userEmail", siteUser.getEmail());
        model.addAttribute("wrote_comment_paging", wroteComments);
        
        if (bindingResult.hasErrors()) {
            return "user/profile";
        }

        if(!this.userService.isMatch(userUpdateForm.getOriginPassword(), siteUser.getPassword())) {
            bindingResult.rejectValue("originPassword", "passwordInCorrect",
                    "기존 패스워드가 일치하지 않습니다.");
            return "user/profile";
        }
        if(!userUpdateForm.getPassword1().equals(userUpdateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect",
                    "확인 패스워드가 일치하지 않습니다.");
            return "user/profile";
        }

        try {
            userService.update(siteUser, userUpdateForm.getPassword1());
        } catch(Exception e) {
            e.printStackTrace();
            bindingResult.reject("updateFailed", e.getMessage());
        }
        return "user/profile";
    }
    
    @GetMapping("/find")
    public String findAccount(Model model) {
        model.addAttribute("sendConfirm", false);
        model.addAttribute("error", false);
        return "user/find";
    }
    
    @PostMapping("/find")
    public String findAccount(Model model, @RequestParam(value="email") String email) {
    	String ADMIN_ADDRESS = "관리자메일";	// 관리자메일
        try {
            SiteUser siteUser = this.userService.getUserByEmail(email);
            model.addAttribute("sendConfirm", true);
            model.addAttribute("userEmail", email);
            model.addAttribute("error", false);
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setFrom(ADMIN_ADDRESS);
            simpleMailMessage.setTo(email);
            simpleMailMessage.setSubject("계정 정보입니다.");
            StringBuffer sb = new StringBuffer();

            String newPassword = PasswordGenerator.generateRandomPassword();
            sb.append(siteUser.getUsername())
              .append("계정의 비밀번호를 새롭게 초기화 했습니다..\n")
			  .append("새 비밀번호는 ")
			  .append(newPassword).append("입니다.\n")
			  .append("로그인 후 내 정보에서 새로 비밀번호를 지정해주세요.");
            simpleMailMessage.setText(sb.toString());
            this.userService.update(siteUser, newPassword);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mailSender.send(simpleMailMessage);
                }
            }).start();
        } catch(DataNotFoundException e) {
            model.addAttribute("sendConfirm", false);
            model.addAttribute("error", true);
        }
        return "user/find";
    }
    
    public static class PasswordGenerator {
        private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        private static final String NUMBER = "0123456789";
        private static final String OTHER_CHAR = "!@#$%&*()_+-=[]?";

        private static final String PASSWORD_ALLOW_BASE = CHAR_LOWER + CHAR_UPPER + NUMBER + OTHER_CHAR;
        private static final int PASSWORD_LENGTH = 12;

        public static String generateRandomPassword() {
            if (PASSWORD_LENGTH < 1) throw new IllegalArgumentException("Password length must be at least 1");

            StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
            Random random = new SecureRandom();
            for (int i = 0; i < PASSWORD_LENGTH; i++) {
                int rndCharAt = random.nextInt(PASSWORD_ALLOW_BASE.length());
                char rndChar = PASSWORD_ALLOW_BASE.charAt(rndCharAt);
                sb.append(rndChar);
            }

            return sb.toString();
        }
    }
}
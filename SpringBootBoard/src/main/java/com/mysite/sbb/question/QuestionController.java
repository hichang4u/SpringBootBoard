package com.mysite.sbb.question;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.answer.AnswerForm;
import com.mysite.sbb.answer.AnswerService;
import com.mysite.sbb.category.Category;
import com.mysite.sbb.category.CategoryService;
import com.mysite.sbb.comment.Comment;
import com.mysite.sbb.comment.CommentForm;
import com.mysite.sbb.comment.CommentService;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/question")
@RequiredArgsConstructor
@Controller
public class QuestionController {

	private final QuestionService questionService;
	private final AnswerService answerService;
	private final UserService userService;
	private final CommentService commentService;
	private final CategoryService categoryService;
	private final QuestionFileService questionFileService;

	@GetMapping("/list")
    public String list(Model model
    				 , @RequestParam(value="page", defaultValue="0") int page
    				 , @RequestParam(value = "kw", defaultValue = "") String kw) {
        Page<Question> paging = this.questionService.getList(page, kw);        
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        List<Category> categoryList = this.categoryService.getAll();
        model.addAttribute("category_list", categoryList);
        return "question/lists";
    }
    
    @GetMapping(value = "/detail/{id}")
    public String detail(Model model
    				   , @PathVariable("id") Integer id
    				   , AnswerForm answerForm
    				   , CommentForm commentForm
    				   , @RequestParam(value="answerPage", defaultValue="0") int answerPage
    				   , @RequestParam(value="answerSort", defaultValue="time") String answerSort) {
    	Question question = this.questionService.getQuestion(id);
        model.addAttribute("question", question);
        Page<Answer> answerPaging = this.answerService.getAnswerList(question, answerPage, answerSort);
        model.addAttribute("answerPage", answerPaging);
        List<Comment> commentList = this.commentService.getCommentList(question);
        model.addAttribute("comment_list", commentList);
        List<Category> categoryList = this.categoryService.getAll();
        model.addAttribute("category_list", categoryList);
        return "question/detail";
    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String questionCreate(QuestionForm questionForm
    						   , Model model) {
    	List<Category> categoryList = this.categoryService.getAll();
        model.addAttribute("category_list", categoryList);
        return "question/form";
    }
    
    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String questionCreate(@Valid QuestionForm questionForm
    						   , BindingResult bindingResult
    						   , Principal principal)  throws IOException {
        if (bindingResult.hasErrors()) {
            return "question/form";
        }
        SiteUser siteUser = this.userService.getUser(principal.getName());
        Category category = this.categoryService.getCategoryById(questionForm.getCategory());
        Question question = this.questionService.create(questionForm.getSubject(), questionForm.getContent(), siteUser, category);
        // 파일 업로드 처리
        if (questionForm.getFiles() != null) {
            for (MultipartFile file : questionForm.getFiles()) {
                if (!file.isEmpty()) {
                    try {
                        questionFileService.saveFile(file, question);
                    } catch (IOException e) {
                        // Log the error and handle it appropriately
                        e.printStackTrace();
                        // You might want to add an error message to the model here
                    }
                }
            }
        }
        
        return "redirect:/question/list";
    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String questionModify(QuestionForm questionForm
    						   , @PathVariable("id") Integer id
    						   , Principal principal
    						   , Model model) {
        Question question = this.questionService.getQuestion(id);
        if(!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        questionForm.setSubject(question.getSubject());
        questionForm.setContent(question.getContent());
        questionForm.setCategory(question.getCategory().getId().intValue());
        // 기존 파일 정보를 모델에 추가
        List<QuestionFile> existingFiles = questionFileService.getFilesByQuestion(question);
        model.addAttribute("file_list", existingFiles);
        
        List<Category> categoryList = this.categoryService.getAll();
        model.addAttribute("category_list", categoryList);
        return "question/form";
    }
    
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String questionModify(@Valid QuestionForm questionForm
    						   , BindingResult bindingResult
    						   , Principal principal
    						   , @PathVariable("id") Integer id
    						   , @RequestParam(value = "deleteFileIds", required = false) List<Long> deleteFileIds
    						   , RedirectAttributes redirectAttributes) throws IOException {
        if (bindingResult.hasErrors()) {
            return "question/form";
        }
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        Category category = this.categoryService.getCategoryById(questionForm.getCategory());
        this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent(), category);
        // 삭제할 파일 처리
        if (deleteFileIds != null && !deleteFileIds.isEmpty()) {
            questionFileService.deleteFiles(deleteFileIds);
        }        
        // 새 파일 업로드 처리
        if (questionForm.getFiles() != null) {
            for (MultipartFile file : questionForm.getFiles()) {
                if (!file.isEmpty()) {
                    try {
                        questionFileService.saveFile(file, question);
                    } catch (IOException e) {
                        // Log the error and handle it appropriately
                        e.printStackTrace();
                        // You might want to add an error message to the model here
                    }
                }
            }
        }
        return String.format("redirect:/question/detail/%s", id);
    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String questionDelete(Principal principal, @PathVariable("id") Integer id) {
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }
        this.questionService.delete(question);
        return "redirect:/";
    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String questionVote(Principal principal, @PathVariable("id") Integer id) {
        Question question = this.questionService.getQuestion(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.questionService.vote(question, siteUser);
        return String.format("redirect:/question/detail/%s", id);
    }
    
    @GetMapping("/file/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("fileId") Long fileId) {
        QuestionFile questionFile = questionFileService.getFile(fileId)
            .orElseThrow(() -> new RuntimeException("File not found"));

        try {
            Resource resource = questionFileService.loadFileAsResource(questionFile.getStoredFileName());
            
            String encodedFileName = URLEncoder.encode(questionFile.getOriginalFileName(), StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                    .body(resource);
        } catch (IOException e) {
            throw new RuntimeException("Error downloading file", e);
        }
    }
}
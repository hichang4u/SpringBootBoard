package com.mysite.sbb.category;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/category")
@RequiredArgsConstructor
@Controller
public class CategoryController {

    private final CategoryService categoryService;
    private final QuestionService questionService;

    @GetMapping("/{category}")
    public String contentList(Model model
    						, @PathVariable("category") int categoryId
    						, @RequestParam(value="page", defaultValue="0") int page) {
        Category category = this.categoryService.getCategoryById(categoryId);
        List<Category> categoryList = this.categoryService.getAll();
        Page<Question> paging = this.questionService.getCategoryQuestionList(category, page);

        model.addAttribute("category_id", categoryId);
        model.addAttribute("category_name", category.getName());
        model.addAttribute("category_list", categoryList);
        model.addAttribute("paging", paging);
        return "question/list";
    }
}
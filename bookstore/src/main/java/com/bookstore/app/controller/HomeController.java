package com.bookstore.app.controller;

import com.bookstore.app.model.Book;
import com.bookstore.app.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final BookService bookService;

    @GetMapping("/")
    public String home(Model model) {
        List<Book> featured = bookService
                .search(null, null, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
                .getContent();
        model.addAttribute("featuredBooks", featured);
        model.addAttribute("categories", bookService.allCategories());
        model.addAttribute("spotlight", featured.isEmpty() ? null : featured.get(0));
        model.addAttribute("shelves", bookService.shelvesByCategory(6, 8));
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("name", "");
        model.addAttribute("email", "");
        return "register";
    }
}

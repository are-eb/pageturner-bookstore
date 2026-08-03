package com.bookstore.app.controller;

import com.bookstore.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public String register(@RequestParam String name,
                            @RequestParam String email,
                            @RequestParam String password,
                            @RequestParam(required = false) String address,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        try {
            userService.register(name, email, password, address);
            redirectAttributes.addFlashAttribute("success", "Account created! Please sign in.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            return "register";
        }
    }
}

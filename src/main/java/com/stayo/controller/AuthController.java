package com.stayo.controller;

import com.stayo.model.User;
import com.stayo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/signin")
    public String showSignInForm() {
        return "signin";
    }

    @PostMapping("/signin")
    public String processSignIn(@ModelAttribute("user") User user,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (userService.authenticate(user.getEmail(), user.getPassword())) {
            User authenticatedUser = userService.findByEmail(user.getEmail()).get();
            session.setAttribute("user", authenticatedUser);
            return "redirect:/";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid email or password");
            return "redirect:/signin";
        }
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@ModelAttribute("user") User user,
            RedirectAttributes redirectAttributes) {
        try {
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("success", "Registration successful. Please sign in.");
            return "redirect:/signin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/signout")
    public String signOut(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
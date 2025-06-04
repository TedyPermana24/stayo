package com.stayo.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.stayo.model.User;
import com.stayo.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/signin")
    public String showSignInForm() {
        return "signin";
    }
    
    // Tambahkan method GET untuk register
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/signin")
    public String signIn(@RequestParam String email, @RequestParam String password, 
                        HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            if (userService.authenticate(email, password)) {
                Optional<User> userOpt = userService.findByEmail(email);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    HttpSession session = request.getSession();
                    session.setAttribute("user", user);
                    session.setAttribute("userId", user.getId());
                    session.setAttribute("userRole", user.getRole().toString());
                    
                    // Redirect based on role
                    switch (user.getRole()) {
                        case ADMIN:
                            return "redirect:/admin/dashboard";
                        case VENDOR:
                            return "redirect:/vendor/dashboard";
                        case USER:
                        default:
                            return "redirect:/";
                    }
                }
            }
            redirectAttributes.addFlashAttribute("error", "Invalid email or password");
            return "redirect:/signin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Login failed: " + e.getMessage());
            return "redirect:/signin";
        }
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            // Set default role as USER
            user.setRole(User.Role.USER);
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please sign in.");
            return "redirect:/signin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Registration failed: " + e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/signout")
    public String signOut(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
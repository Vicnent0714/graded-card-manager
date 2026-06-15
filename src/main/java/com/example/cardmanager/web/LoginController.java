package com.example.cardmanager.web;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.cardmanager.model.User;
import com.example.cardmanager.repository.UserRepository;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

        @GetMapping("/login")
    public String showLoginPage() {
        return "login";   // → login.html
    }

        @PostMapping("/login")
    public String login(String username, String password,
                        HttpSession session, Model model) {

        User user = userRepository.findByUsername(username);

        if (user == null) {
            model.addAttribute("error", "ユーザー名が存在しません。");
            return "login";
        }

        if (!user.getPassword().equals(password)) {
            model.addAttribute("error", "パスワードが間違っています。");
            return "login";
        }

        session.setAttribute("loginUser", user);

        return "redirect:/cards";
    }

    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}

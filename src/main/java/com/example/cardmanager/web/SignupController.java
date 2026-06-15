package com.example.cardmanager.web;

import java.time.LocalDate;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.cardmanager.model.User;
import com.example.cardmanager.repository.UserRepository;

@Controller
public class SignupController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/signup")
    public String showSignupPage() {
        return "signup";   
    }

    
    @PostMapping("/signup")
    public String signup(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam String birthYear,
            @RequestParam String birthMonth,
            @RequestParam String birthDay,
            @RequestParam String country,
            HttpSession session,
            Model model) {

        if (userRepository.findByUsername(username) != null) {
            model.addAttribute("error", "このユーザー名は既に使用されています。");
            return "signup";
        }

        if (userRepository.existsByEmail(email)) {
            model.addAttribute("error", "このメールアドレスは既に登録されています。");
            return "signup";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password); 
        user.setEmail(email);
        user.setCountry(country);

      
        try {
            LocalDate birthDate = LocalDate.of(
                    Integer.parseInt(birthYear),
                    Integer.parseInt(birthMonth),
                    Integer.parseInt(birthDay)
            );
            user.setBirthDate(birthDate);
        } catch (Exception e) {
            model.addAttribute("error", "生年月日の形式が正しくありません。");
            return "signup";
        }

     
        User savedUser = userRepository.save(user);

        
        session.setAttribute("loginUser", savedUser);

        return "redirect:/cards";
    }
}

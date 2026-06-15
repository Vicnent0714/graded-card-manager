package com.example.cardmanager.web;

import java.time.LocalDate;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.cardmanager.model.User;
import com.example.cardmanager.repository.UserRepository;

@Controller
public class MyPageController {

    private final UserRepository userRepository;

    public MyPageController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/mypage")
    public String myPage(
            HttpSession session,
            Model model,
            @RequestParam(name = "edit", required = false, defaultValue = "false") boolean edit
    ) {
        User loginUser = getLoginUser(session);
        if (loginUser == null) return "redirect:/login";

        User user = userRepository.findById(loginUser.getId()).orElse(null);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("editMode", edit);

        return "mypage";
    }

    @PostMapping("/mypage")
    public String updateProfile(
            HttpSession session,
            Model model,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String birthDate
    ) {
        User loginUser = getLoginUser(session);
        if (loginUser == null) return "redirect:/login";

        User user = userRepository.findById(loginUser.getId()).orElse(null);
        if (user == null) return "redirect:/login";

        // username 重複チェック（自分はOK）
        if (!user.getUsername().equals(username) && userRepository.existsByUsername(username)) {
            return renderMyPageWithError(user, model, "mypage.error.username");
        }

        // email 重複チェック（自分はOK）
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            return renderMyPageWithError(user, model, "mypage.error.email");
        }

        // ★国 必須チェック
        if (country == null || country.isBlank()) {
            return renderMyPageWithError(user, model, "mypage.error.countryRequired");
        }

        // ★生年月日 必須チェック
        if (birthDate == null || birthDate.isBlank()) {
            return renderMyPageWithError(user, model, "mypage.error.birthdateRequired");
        }

        user.setUsername(username);
        user.setEmail(email);
        user.setCountry(country);

        try {
            user.setBirthDate(LocalDate.parse(birthDate)); // yyyy-MM-dd
        } catch (Exception e) {
            return renderMyPageWithError(user, model, "mypage.error.birthdate");
        }

        User saved = userRepository.save(user);

        // session 同期
        session.setAttribute("loginUser", saved);
        session.setAttribute("user", saved);

        return "redirect:/mypage";
    }

    private String renderMyPageWithError(User user, Model model, String errorKey) {
        model.addAttribute("user", user);
        model.addAttribute("editMode", true);
        model.addAttribute("errorKey", errorKey);
        return "mypage";
    }

    private User getLoginUser(HttpSession session) {
        Object u1 = session.getAttribute("loginUser");
        if (u1 instanceof User) return (User) u1;

        Object u2 = session.getAttribute("user");
        if (u2 instanceof User) return (User) u2;

        return null;
    }
}
package com.example.cardmanager.web;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.cardmanager.model.Card;
import com.example.cardmanager.model.User;
import com.example.cardmanager.repository.CardRepository;

@Controller
public class CardController {

    @Autowired
    private CardRepository cardRepository;

    @GetMapping({"/cards", "/cards/list"})
    public String list(
            Model model,
            HttpSession session,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {

        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        Long userId = loginUser.getId();

        String kw = (keyword == null) ? "" : keyword.trim();
        model.addAttribute("keyword", kw);

        var cards = (!kw.isEmpty())
                ? cardRepository.findByUserIdAndNameContainingIgnoreCase(userId, kw)
                : cardRepository.findByUserId(userId);

        model.addAttribute("cards", cards);

        var allCards = cardRepository.findByUserId(userId);

        model.addAttribute("totalCount", allCards.size());

        long totalPrice = 0;
        for (Card c : allCards) {
            if (c.getPrice() == null) continue;

            String digits = c.getPrice().replaceAll("[^0-9]", "");
            if (digits.isEmpty()) continue;

            try {
                totalPrice += Long.parseLong(digits);
            } catch (NumberFormatException e) {
            }
        }
        model.addAttribute("totalPrice", totalPrice);

        return "cards";
    }

    @GetMapping("/cards/add")
    public String showAddForm(HttpSession session) {
        if (session.getAttribute("loginUser") == null) {
            return "redirect:/login";
        }
        return "add-card";
    }

    @GetMapping("/cards/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, HttpSession session) {

        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid card ID: " + id));

        if (card.getUserId() == null || !card.getUserId().equals(loginUser.getId())) {
            return "redirect:/cards";
        }

        model.addAttribute("card", card);
        return "card-edit";
    }

    @PostMapping("/cards/edit/{id}")
    public String updateCard(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("company") String company,
            @RequestParam("grade") String grade,
            @RequestParam("price") String price,
            @RequestParam("source") String source,
            @RequestParam(value = "certNumber", required = false) String certNumber,
            @RequestParam(value = "marketKeyword", required = false) String marketKeyword,
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam(value = "deleteImage", required = false) String deleteImage,
            HttpSession session
    ) throws IOException {

        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid card ID: " + id));

        if (card.getUserId() == null || !card.getUserId().equals(loginUser.getId())) {
            return "redirect:/cards";
        }

        card.setName(name);
        card.setCompany(company);
        card.setGrade(grade);
        card.setPrice(price);
        card.setSource(source);
        card.setCertNumber(
                (certNumber != null && !certNumber.trim().isEmpty()) ? certNumber.trim() : null
        );
        card.setMarketKeyword(
                (marketKeyword != null && !marketKeyword.trim().isEmpty()) ? marketKeyword.trim() : null
        );

        if ("true".equals(deleteImage)) {
            if (card.getImagePath() != null && !card.getImagePath().equals("default-card.jpg")) {
                Files.deleteIfExists(Paths.get("uploads/" + card.getImagePath()));
            }
            card.setImagePath("default-card.jpg");
        }
        else if (!imageFile.isEmpty()) {

            if (card.getImagePath() != null && !card.getImagePath().equals("default-card.jpg")) {
                Files.deleteIfExists(Paths.get("uploads/" + card.getImagePath()));
            }

            String uploadDir = "uploads/";
            File folder = new File(uploadDir);
            if (!folder.exists()) folder.mkdirs();

            String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
            Path path = Paths.get(uploadDir + fileName);
            Files.write(path, imageFile.getBytes());

            card.setImagePath(fileName);
        }

        cardRepository.save(card);
        return "redirect:/cards";
    }

    @PostMapping("/cards/add")
    public String addCard(
            @RequestParam("name") String name,
            @RequestParam("company") String company,
            @RequestParam("grade") String grade,
            @RequestParam("price") String price,
            @RequestParam("source") String source,
            @RequestParam(value = "certNumber", required = false) String certNumber,
            @RequestParam(value = "marketKeyword", required = false) String marketKeyword,
            @RequestParam("image") MultipartFile imageFile,
            HttpSession session
    ) throws IOException {

        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        String imagePath = "default-card.jpg";

        if (!imageFile.isEmpty()) {
            String uploadDir = "uploads/";
            File folder = new File(uploadDir);
            if (!folder.exists()) folder.mkdirs();

            String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
            Path path = Paths.get(uploadDir + fileName);
            Files.write(path, imageFile.getBytes());

            imagePath = fileName;
        }

        Card newCard = new Card();
        newCard.setName(name);
        newCard.setCompany(company);
        newCard.setGrade(grade);
        newCard.setPrice(price);
        newCard.setSource(source);

        if (certNumber != null && !certNumber.trim().isEmpty()) {
            newCard.setCertNumber(certNumber.trim());
        }

        newCard.setMarketKeyword(
                (marketKeyword != null && !marketKeyword.trim().isEmpty()) ? marketKeyword.trim() : null
        );

        newCard.setImagePath(imagePath);
        newCard.setUserId(loginUser.getId());

        cardRepository.save(newCard);

        return "redirect:/cards";
    }

    @PostMapping("/cards/delete/{id}")
    public String deleteCard(@PathVariable Long id, HttpSession session) {

        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid card ID: " + id));

        if (card.getUserId() == null || !card.getUserId().equals(loginUser.getId())) {
            return "redirect:/cards";
        }

        if (card.getImagePath() != null && !card.getImagePath().equals("default-card.jpg")) {
            try {
                Files.deleteIfExists(Paths.get("uploads/" + card.getImagePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        cardRepository.deleteById(id);
        return "redirect:/cards";
    }

    @PostMapping("/cards/bulk-delete")
    public String bulkDelete(@RequestParam(name = "cardIds", required = false) List<Long> cardIds, HttpSession session) {
        
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        if (cardIds != null && !cardIds.isEmpty()) {
            for (Long id : cardIds) {
                cardRepository.findById(id).ifPresent(card -> {
                    if (card.getUserId() != null && card.getUserId().equals(loginUser.getId())) {
                        if (card.getImagePath() != null && !card.getImagePath().equals("default-card.jpg")) {
                            try {
                                Files.deleteIfExists(Paths.get("uploads/" + card.getImagePath()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        cardRepository.deleteById(id);
                    }
                });
            }
        }
        
        return "redirect:/cards";
    }
}
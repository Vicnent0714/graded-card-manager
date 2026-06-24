package com.example.cardmanager.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.cardmanager.model.Card;
import com.example.cardmanager.model.User;
import com.example.cardmanager.repository.CardRepository;

@Service
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public boolean deleteCard(Long id, User loginUser) {

        if (loginUser == null) {
            return false;
        }

        Card card = cardRepository.findById(id).orElse(null);

        if (card == null) {
            return false;
        }

        if (card.getUserId() == null || !card.getUserId().equals(loginUser.getId())) {
            return false;
        }

        deleteCardImage(card);

        cardRepository.deleteById(id);
        return true;
    }

    public void bulkDeleteCards(List<Long> cardIds, User loginUser) {

        if (loginUser == null) {
            return;
        }

        if (cardIds == null || cardIds.isEmpty()) {
            return;
        }

        for (Long id : cardIds) {
            deleteCard(id, loginUser);
        }
    }

    private void deleteCardImage(Card card) {

        if (card.getImagePath() != null && !card.getImagePath().equals("default-card.jpg")) {
            try {
                Files.deleteIfExists(Paths.get("uploads/" + card.getImagePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
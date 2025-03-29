package asz.vizsgaremek.service;

import asz.vizsgaremek.model.Chat;
import asz.vizsgaremek.model.User;
import asz.vizsgaremek.repository.ChatRepository;
import asz.vizsgaremek.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatRepository repository;

    @Autowired
    private UserRepository userRepository;

    public List<Chat> getChatsForUser(String username) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return repository.findByUser1OrUser2(user, user);
    }

    public Chat getChatById(Integer chatId) {
        return repository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
    }

    public Chat startChat(Integer userId, Integer contactUserId) {
        User user1 = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        User user2 = userRepository.findById(contactUserId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + contactUserId));

        // Ellenőrizzük, hogy létezik-e már chat a két felhasználó között
        List<Chat> existingChats = repository.findByUser1OrUser2(user1, user2);
        for (Chat chat : existingChats) {
            if ((chat.getUser1().equals(user1) && chat.getUser2().equals(user2)) ||
                    (chat.getUser1().equals(user2) && chat.getUser2().equals(user1))) {
                return chat; // Ha már létezik, visszaadjuk
            }
        }

        // Ha nem létezik, létrehozunk egy újat
        Chat newChat = new Chat();
        newChat.setUser1(user1);
        newChat.setUser2(user2);
        return repository.save(newChat);
    }

    public void deleteChat(Integer chatId) {
        Chat chat = repository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found with ID: " + chatId));

        messagingTemplate.convertAndSendToUser(
                chat.getUser1().getUserName(),"/queue/chat-deleted",chatId
        );
        messagingTemplate.convertAndSendToUser(
                chat.getUser2().getUserName(),"/queue/chat-deleted",chatId
        );

        repository.delete(chat);
    }
}

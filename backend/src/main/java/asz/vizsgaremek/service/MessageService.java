package asz.vizsgaremek.service;

import asz.vizsgaremek.dto.user.message.MessageDTO;
import asz.vizsgaremek.model.Chat;
import asz.vizsgaremek.model.Message;
import asz.vizsgaremek.model.User;
import asz.vizsgaremek.repository.ChatRepository;
import asz.vizsgaremek.repository.MessageRepository;
import asz.vizsgaremek.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRepository chatRepository;

    public List<MessageDTO> getMessagesForChat(int chatId, String username) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!chat.getUser1().equals(user) && !chat.getUser2().equals(user)) {
            throw new RuntimeException("User is not part of this chat");
        }

        List<Message> messages = messageRepository.findByChatOrderByCreatedAtAsc(chat);

        return messages.stream()
                .map(m -> new MessageDTO(m.getSender().getUserName(), m.getMessage(), m.getCreatedAt()))
                .toList();
    }


    public MessageDTO sendMessage(int chatId, String username, String message) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        User sender = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Ellenőrizd, hogy a felhasználó része-e a beszélgetésnek
        if (!chat.getUser1().equals(sender) && !chat.getUser2().equals(sender)) {
            throw new RuntimeException("User is not part of this chat");
        }

        // Trimmeljük az üzenetet
        String trimmedMessage = message.trim();
        if (trimmedMessage.isEmpty()) {
            throw new RuntimeException("Message cannot be empty");
        }

        Message newMessage = new Message();
        newMessage.setChat(chat);
        newMessage.setSender(sender);
        newMessage.setMessage(trimmedMessage);
        newMessage.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        messageRepository.save(newMessage);

        // DTO visszaadása
        return new MessageDTO(sender.getUserName(), newMessage.getMessage(), newMessage.getCreatedAt());
    }

    public String getChatPartner(int chatId, String senderUsername) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        if (chat.getUser1().getUsername().equals(senderUsername)) {
            return chat.getUser2().getUsername();
        } else if (chat.getUser2().getUsername().equals(senderUsername)) {
            return chat.getUser1().getUsername();
        } else {
            throw new RuntimeException("User is not part of this chat");
        }
    }
}
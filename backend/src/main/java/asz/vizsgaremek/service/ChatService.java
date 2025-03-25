package asz.vizsgaremek.service;

import asz.vizsgaremek.auth.JwtUtil;
import asz.vizsgaremek.model.Chat;
import asz.vizsgaremek.model.User;
import asz.vizsgaremek.repository.ChatRepository;
import asz.vizsgaremek.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ChatRepository repository;

    @Autowired
    private UserRepository userRepository;

    public List<Chat> getChatsForUser(String username) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return repository.findByUser1OrUser2(user, user);
    }

}

package asz.vizsgaremek.service;

import asz.vizsgaremek.dto.user.message.MessageDTO;
import asz.vizsgaremek.model.Chat;
import asz.vizsgaremek.model.Message;
import asz.vizsgaremek.model.User;
import asz.vizsgaremek.repository.ChatRepository;
import asz.vizsgaremek.repository.MessageRepository;
import asz.vizsgaremek.repository.UserRepository;
import asz.vizsgaremek.websocket.WebSocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private WebSocketController webSocketController;

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
                .map(m -> new MessageDTO(m.getSender().getUserName(), m.getMessage(), m.getCreatedAt(),m.getMessageType()))
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
        newMessage.setMessageType("text");
        newMessage.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        messageRepository.save(newMessage);

        // DTO visszaadása
        return new MessageDTO(sender.getUserName(), newMessage.getMessage(), newMessage.getCreatedAt(),newMessage.getMessageType());
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

    public String storeChatFile(MultipartFile file, Integer senderId, Integer receiverId, Integer chatId) {
        String uploadDir = "uploads/files/";
        String subFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        Path fullPath = Paths.get(uploadDir, subFolderName);

        try {
            Files.createDirectories(fullPath);
        } catch (IOException e) {
            throw new RuntimeException("Nem sikerült létrehozni a célmappát", e);
        }

        String fileExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String baseFileName = StringUtils.stripFilenameExtension(file.getOriginalFilename());

        if (fileExtension == null || baseFileName == null) {
            throw new RuntimeException("Érvénytelen fájlnév");
        }

        String uniqueFileName = baseFileName + "-" + UUID.randomUUID() + "." + fileExtension;
        Path destinationFilePath = fullPath.resolve(uniqueFileName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException("Hiba történt a fájl mentésekor", ex);
        }

        String publicPath = "/files/" + subFolderName + "/" + uniqueFileName;

        // Save message into database
        Message savedMessage = saveFileMessage(senderId, chatId, publicPath);
        webSocketController.sendMessage(publicPath);
        return publicPath;
    }


    public Message saveFileMessage(Integer senderId, Integer chatId, String filePath) {
        Message message = new Message();
        message.setSender(userRepository.findById(senderId).orElseThrow());
        message.setChat(chatRepository.findById(chatId).orElseThrow());
        message.setMessage(filePath);
        message.setMessageType("file");
        message.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        return messageRepository.save(message);
    }
}
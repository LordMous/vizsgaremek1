package asz.vizsgaremek.controller;

import asz.vizsgaremek.auth.JwtUtil;
import asz.vizsgaremek.model.User;
import asz.vizsgaremek.service.UserService;
import asz.vizsgaremek.websocket.ChatWebSocketHandler;
import asz.vizsgaremek.websocket.WebSocketController;
import asz.vizsgaremek.dto.user.message.MessageDTO;
import asz.vizsgaremek.dto.user.message.MessageRequest;
import asz.vizsgaremek.dto.message.SocketMessage;
import asz.vizsgaremek.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/message")
@Tag(name = "Message functions", description = "Here you can manage the messages")
@SecurityScheme(
        name = "bearerAuth",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER
)
@CrossOrigin("http://localhost:5173")
public class MessageController {

    @Autowired
    WebSocketController controller;

    private final MessageService messageService;
    private final JwtUtil jwtUtil;
    private final SimpMessagingTemplate template;
    private final UserService userService;

    public MessageController(MessageService messageService, JwtUtil jwtUtil, SimpMessagingTemplate template, UserService userService) {
        this.messageService = messageService;
        this.jwtUtil = jwtUtil;
        this.template = template;
        this.userService = userService;
    }

    @Operation(summary = "Get messages from a chat")
    @GetMapping
    public ResponseEntity<List<MessageDTO>> getMessages(
            @RequestParam int chatId,
            @RequestHeader("Authorization") String token) {

        String username = jwtUtil.extractUsername(extractToken(token));
        List<MessageDTO> messages = messageService.getMessagesForChat(chatId, username);
        return ResponseEntity.ok(messages);
    }

    @Operation(summary = "Send private message")
    @PostMapping
    public ResponseEntity<MessageDTO> sendMessage(
            @RequestBody MessageRequest messageRequest,
            @RequestHeader("Authorization") String token) {

        String senderUsername = jwtUtil.extractUsername(extractToken(token));
        MessageDTO responseMessage = messageService.sendMessage(
                messageRequest.getChatId(), senderUsername, messageRequest.getMessage());

        sendPrivateSocketMessage(messageRequest.getChatId(), senderUsername, messageRequest.getMessage());



        controller.sendMessage("Fetch messages");

        return ResponseEntity.ok(responseMessage);
    }

    private void sendPrivateSocketMessage(int chatId, String senderUsername, String content) {
        String receiverUsername = messageService.getChatPartner(chatId, senderUsername);

        if (receiverUsername == null) {
            System.out.println("Receiver username is null");
            return;
        }

        SocketMessage socketMessage = new SocketMessage();
        socketMessage.setSender(senderUsername);
        socketMessage.setContent(content);
        socketMessage.setChatId(chatId);
        socketMessage.setTimestamp(LocalDateTime.now());

        System.out.println("SOCKET MESSAGE : "+socketMessage.getTimestamp()+" "+socketMessage.getContent()+" "+socketMessage.getSender()+" "+socketMessage.getChatId());
        System.out.println(receiverUsername);

        System.out.println("🧠 Sending to receiverUsername: " + receiverUsername);
        System.out.println("🧠 Sending to senderUsername: " + senderUsername);

        // Küldés a címzettnek
        template.convertAndSendToUser(receiverUsername, "/queue/messages", socketMessage);

        // Küldés a küldőnek is (UI frissítés)
        template.convertAndSendToUser(senderUsername, "/queue/messages", socketMessage);
    }

    private String extractToken(String token) {
        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        throw new RuntimeException("Invalid token format");
    }

    @PostMapping(value = "/chat/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadChatFile(@RequestParam("file") MultipartFile file,
                                                 @RequestParam("senderId") Integer senderId,
                                                 @RequestParam("receiverId") Integer receiverId,
                                                 @RequestParam("chatId") Integer chatId) {
        String filePath = messageService.storeChatFile(file, senderId, receiverId, chatId);
        System.out.println("SENDER : "+" "+userService.findById(senderId).getUsername());
        System.out.println("CHAT ID : "+" "+chatId);
        System.out.println("FILE PATH : "+filePath);
        String senderUsername = userService.findById(senderId).getUsername();
        sendPrivateSocketMessage(chatId,senderUsername,filePath);
        controller.sendMessage("Fetch messages");
        //System.out.println(filePath);
        return ResponseEntity.ok(filePath);
    }
}

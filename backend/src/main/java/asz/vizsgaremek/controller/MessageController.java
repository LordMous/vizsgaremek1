package asz.vizsgaremek.controller;

import asz.vizsgaremek.auth.JwtUtil;
import asz.vizsgaremek.auth.websocket.ChatWebSocketHandler;
import asz.vizsgaremek.auth.websocket.WebSocketController;
import asz.vizsgaremek.dto.user.message.MessageDTO;
import asz.vizsgaremek.dto.user.message.MessageRequest;
import asz.vizsgaremek.dto.user.message.SocketMessage;
import asz.vizsgaremek.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

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

    public MessageController(MessageService messageService, JwtUtil jwtUtil, SimpMessagingTemplate template) {
        this.messageService = messageService;
        this.jwtUtil = jwtUtil;
        this.template = template;
    }

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;



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
        socketMessage.setSenderUsername(senderUsername);
        socketMessage.setContent(content);
        socketMessage.setChatId(chatId);
        socketMessage.setTimestamp(LocalDateTime.now());


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
}

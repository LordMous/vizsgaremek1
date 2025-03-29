package asz.vizsgaremek.controller;

import asz.vizsgaremek.auth.JwtUtil;
import asz.vizsgaremek.dto.user.chat.ChatDTO;
import asz.vizsgaremek.model.Chat;
import asz.vizsgaremek.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@Tag(name = "Chat functions", description = "Manage private chats")
@SecurityScheme(
        name = "bearerAuth",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER
)
@CrossOrigin("http://localhost:5173")
public class ChatController {

    private final ChatService chatService;
    private final JwtUtil jwtUtil;

    public ChatController(ChatService chatService, JwtUtil jwtUtil) {
        this.chatService = chatService;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "Get chats for the logged-in user")
    @GetMapping
    public ResponseEntity<List<Chat>> getChats(@RequestHeader("Authorization") String token) {
        String username = jwtUtil.extractUsername(extractToken(token));
        return ResponseEntity.ok(chatService.getChatsForUser(username));
    }

    @Operation(summary = "Get chat details from chatID")
    @GetMapping("/{chatId}")
    public ResponseEntity<ChatDTO> getChatById(@PathVariable Integer chatId) {

        Chat chat = chatService.getChatById(chatId);

        ChatDTO d = new ChatDTO(chat.getId(),chat.getUser1().getUserName(),chat.getUser2().getUserName());

        return ResponseEntity.ok(d);
    }

    @Operation(summary = "Start a new chat or return an existing one")
    @PostMapping("/start")
    public ResponseEntity<ChatDTO> startChat(
            @RequestParam Integer userId,
            @RequestParam Integer contactUserId) {
        Chat chat = chatService.startChat(userId, contactUserId);
        ChatDTO chatDTO = new ChatDTO(chat.getId(), chat.getUser1().getUserName(), chat.getUser2().getUserName());
        return ResponseEntity.ok(chatDTO);
    }

    @Operation(summary = "Delete a chat by ID")
    @DeleteMapping("/{chatId}")
    public ResponseEntity<Void> deleteChat(@PathVariable Integer chatId) {
        chatService.deleteChat(chatId);
        return ResponseEntity.noContent().build(); // 204 No Content válasz
    }


    private String extractToken(String token) {
        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        throw new RuntimeException("Invalid token format");
    }
}

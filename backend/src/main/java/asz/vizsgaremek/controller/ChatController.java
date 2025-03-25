package asz.vizsgaremek.controller;

import asz.vizsgaremek.auth.JwtUtil;
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

    private String extractToken(String token) {
        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        throw new RuntimeException("Invalid token format");
    }
}

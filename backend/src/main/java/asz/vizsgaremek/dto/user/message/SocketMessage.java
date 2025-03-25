package asz.vizsgaremek.dto.user.message;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SocketMessage {
    private String senderUsername;
    private String content;
    private Integer chatId;
    private LocalDateTime timestamp; // Új mező

}

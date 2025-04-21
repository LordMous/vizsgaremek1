package asz.vizsgaremek.dto.message;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SocketMessage {
    private String sender;
    private String content;
    private Integer chatId;
    private LocalDateTime timestamp; // Új mező

}

package asz.vizsgaremek.dto.user.message;

import lombok.Getter;

import java.sql.Timestamp;

@Getter
public class MessageDTO {
    private String sender;
    private String message;
    private Timestamp createdAt;
    private String type;

    public MessageDTO(String sender, String message, Timestamp createdAt, String type) {
        this.sender = sender;
        this.message = message;
        this.createdAt = createdAt;
        this.type = type;
    }

    // Getters
}

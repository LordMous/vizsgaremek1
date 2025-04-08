package asz.vizsgaremek.dto.user.message;

import lombok.Getter;

import java.sql.Timestamp;

@Getter
public class MessageDTO {
    private String sender;
    private String message;
    private Timestamp createdAt;

    public MessageDTO(String sender, String message, Timestamp createdAt) {
        this.sender = sender;
        this.message = message;
        this.createdAt = createdAt;
    }

    // Getters
}

package asz.vizsgaremek.dto.announcement;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AnnouncementResponse {
    private Integer id;
    private String message;
    private LocalDateTime createdAt;
    private String senderUsername;

    public AnnouncementResponse(Integer id, String message, LocalDateTime createdAt, String senderUsername) {
        this.id = id;
        this.message = message;
        this.createdAt = createdAt;
        this.senderUsername = senderUsername;
    }

}

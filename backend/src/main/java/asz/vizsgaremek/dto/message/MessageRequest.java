package asz.vizsgaremek.dto.user.message;

import lombok.Getter;

@Getter
public class MessageRequest {
    private int chatId;
    private String message;
}

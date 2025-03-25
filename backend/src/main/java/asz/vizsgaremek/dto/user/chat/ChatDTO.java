package asz.vizsgaremek.dto.user.chat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatDTO {
    private Integer id;
    private String user1Name;
    private String user2Name;

    public ChatDTO(Integer id, String user1Name, String user2Name) {
        this.id = id;
        this.user1Name = user1Name;
        this.user2Name = user2Name;
    }
}

package asz.vizsgaremek.controller;

import asz.vizsgaremek.dto.user.message.SocketMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import java.time.LocalDateTime;

@Component
@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate template;

    @MessageMapping("/chat")
    @SendTo("/user/queue/messages") // Ezzel jelezheted, hogy a privát csatornára küldöd
    public String sendMessage(String message) {
        System.out.println("Sending message: " + message);
        return message; // Visszaküldjük az üzenetet a klienseknek
    }

}

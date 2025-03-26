package asz.vizsgaremek.auth.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

@Component
@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate template;

    @MessageMapping("/chat")
    @SendTo("/user/queue/messages") // Ezzel jelezheted, hogy a privát csatornára küldöd
    public String sendMessage(String message) {
        return message; // Visszaküldjük az üzenetet a klienseknek
    }

}

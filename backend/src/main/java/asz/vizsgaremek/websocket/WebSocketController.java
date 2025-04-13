package asz.vizsgaremek.websocket;

import asz.vizsgaremek.dto.announcement.AnnouncementResponse;
import asz.vizsgaremek.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

@Component
@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate template;

    private MessageService messageService;

    @MessageMapping("/chat")
    @SendTo("/user/queue/messages") // Ezzel jelezheted, hogy a privát csatornára küldöd
    public String sendMessage(String message) {
        //messageService.sendMessage(message.getChatId(), message.getSenderUsername(), message.getContent());

        return message; // Visszaküldjük az üzenetet a klienseknek
    }

    public void sendContactUpdate(String username, String message) {
        // Privát értesítés a kontaktok frissítéséről
        template.convertAndSendToUser(username, "/queue/contacts", message);
    }

    public void broadcastAnnouncement(AnnouncementResponse response) {
        template.convertAndSend("/topic/announcements", response);
    }

}

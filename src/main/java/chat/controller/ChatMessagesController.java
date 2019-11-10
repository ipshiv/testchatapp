package chat.controller;

import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import chat.model.Message;
import chat.model.misc.MessageType;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;

@Controller
public class ChatMessagesController {
	
	
	@MessageMapping("/chat")
    @SendTo("/topic/public")
	public Message chatMessage(@Payload Message incomeMessage) {
		incomeMessage.setId("someId");
		return incomeMessage;
		
	}
	

}

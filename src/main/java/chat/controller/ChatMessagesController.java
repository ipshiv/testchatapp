package chat.controller;

import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import chat.dbhandler.DbHandler;
import chat.dbhandler.JsonDbHandler;
import chat.model.Message;


import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;

@Controller
public class ChatMessagesController {
	
	private DbHandler dbHandler = new JsonDbHandler();
	
	@MessageMapping("/chat")
    @SendTo("/topic/public")
	public Message chatMessage(@Payload Message incomeMessage,
			SimpMessageHeaderAccessor headerAccessor) {
		
		incomeMessage.setId(headerAccessor.getMessageHeaders().get("message-id").toString());
		dbHandler.saveData(incomeMessage);
		return incomeMessage;
		
	}
	

}

package chat.controller;

import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import chat.model.Message;
import chat.model.misc.MessageType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;

@Controller
public class ChatMessagesController {
	
	@Autowired
    private SimpMessageSendingOperations messagingTemplate;
	
	// private DbController dbController;
	
	@MessageMapping("/system")
	public void sysMessage(@Payload Message incomeMessage,
			SimpMessageHeaderAccessor headerAccessor) {
		
		String sessionId = headerAccessor.getSessionId();
		headerAccessor.setLeaveMutable(true);
		logger.info(sessionId);
		// logger.info(incomeMessage.getType().name());
		if (incomeMessage.getType() == MessageType.JOIN) {
			logger.info("Join " + sessionId);
			headerAccessor.getSessionAttributes().put("username", incomeMessage.getUser());
            messagingTemplate.convertAndSend("/topic/public", incomeMessage, headerAccessor.getMessageHeaders());
			/*
			if (incomeMessage.getUser() != "") {
				// logger.info(sessionId + "\t" + incomeMessage.getType());
			//if (dbController.checkUser(incomeMessage.getUser())) {
				headerAccessor.getSessionAttributes().put("username", incomeMessage.getUser());
	            messagingTemplate.convertAndSend("/topic/public", incomeMessage);
				
			} else {
				
				incomeMessage.setType(MessageType.ERROR);
				incomeMessage.setContent("User already in chat!");
				messagingTemplate.convertAndSendToUser(sessionId, "/topic/system", incomeMessage);
				
			}
			*/
		// return incomeMessage;
		} else if (incomeMessage.getType() == MessageType.HISTORY) {
			incomeMessage.setContent("TEST HISTORY!&7&TEST HISTORY!&7&TEST HISTORY!&7&TEST HISTORY!&7&");
			//incomeMessage.setContent(dbController.getHistoryString(incomeMessage.getContent()));
			messagingTemplate.convertAndSendToUser(sessionId, "/topic/system", incomeMessage, headerAccessor.getMessageHeaders());
		}
		
		// return incomeMessage;
		
	}
	
	@MessageMapping("/chat")
    @SendTo("/topic/public")
	public Message chatMessage(@Payload Message incomeMessage) {
		incomeMessage.setId("someId");
		return incomeMessage;
		
	}
	

}

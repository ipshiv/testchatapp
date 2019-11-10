package chat.controller;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import chat.dbhandler.DbHandler;
import chat.dbhandler.JsonDbHandler;
import chat.model.Message;
import chat.model.User;
import chat.model.misc.MessageType;

@Controller
public class SysMessagesController {
	
	private static final Logger logger = LoggerFactory.getLogger(SysMessagesController.class);
	private DbHandler dbHandler = new JsonDbHandler();
	private ArrayList<User> connectedUsers = new ArrayList<User>();
	private static final int HISTORY_LENGTH = 10;
	
	
	@Autowired
    private SimpMessageSendingOperations messagingTemplate;
	
	private String convertUsersToString() {
		String converted = "";
		for (User connectedUser : connectedUsers) {
			converted += connectedUser.getName() + "&\\&" + connectedUser.getStatus() + "&7&";
		}
		return converted;
	}
	
	private boolean checkUserNameUniq(String userName) {
		
		for (int i = 0; i < connectedUsers.size(); i++)
		{
			if (connectedUsers.get(i).getName() == userName) {
				return false;
			}
		}
		return true;
	}
	
	private String historyArrayToString (Message[] historyMessages) {
		return "String";
	}
	
	@MessageMapping("/system")
	public void sysMessage(@Payload Message incomeMessage,
			SimpMessageHeaderAccessor headerAccessor) {
		
		String sessionId = headerAccessor.getSessionId();
		headerAccessor.setLeaveMutable(true);
		
		if (incomeMessage.getType() == MessageType.JOIN) {
			
			if (checkUserNameUniq(incomeMessage.getUser())) {
			
				logger.info("Join " + sessionId);
				headerAccessor.getSessionAttributes().put("username", incomeMessage.getUser());
	            messagingTemplate.convertAndSend("/topic/public", incomeMessage, headerAccessor.getMessageHeaders());
	            
	            User user = new User();
	            user.setName(incomeMessage.getUser());
	            user.setStatus("Connected!");
	            connectedUsers.add(user);
	            
	            // send online users to frontend
	            incomeMessage.setContent(convertUsersToString());
	            incomeMessage.setType(MessageType.ROOM);
	            messagingTemplate.convertAndSend("/topic/system", incomeMessage, headerAccessor.getMessageHeaders());
	            
			
			} else {
				
				incomeMessage.setType(MessageType.ERROR);
				incomeMessage.setContent("User already in chat! Try other nickname.");
				messagingTemplate.convertAndSendToUser(sessionId, "/topic/system", incomeMessage, headerAccessor.getMessageHeaders());
				
			}
			
		} else if (incomeMessage.getType() == MessageType.HISTORY) {
			
			Message[] historyArray = dbHandler.readData(incomeMessage.getContent(), HISTORY_LENGTH);
			String historyContent = historyArrayToString(historyArray);
			incomeMessage.setContent(historyContent);
			messagingTemplate.convertAndSendToUser(sessionId, "/topic/system", incomeMessage, headerAccessor.getMessageHeaders());
		}
		
	}
	
}

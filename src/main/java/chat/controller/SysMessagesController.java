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
		for (User connectedUser : this.connectedUsers) {
			converted += connectedUser.getName() + "&\\&" + connectedUser.getStatus() + "&7&";
		}
		return converted;
	}
	
	private int findUser(String userName) {
		
		for (int i = 0; i < connectedUsers.size(); i++)
		{
			User user = connectedUsers.get(i);
			logger.info(user.getName());
			logger.info("Compare result: " + userName.contentEquals(user.getName()));
			if (userName.contentEquals(user.getName())) {
				logger.info("Found " + i);
				return i;
			}
		}
		return -1;
	}
	
	private String historyArrayToString (Message[] historyMessages) {
		return "String&7&";
	}
	
	@MessageMapping("/system")
	public void sysMessage(@Payload Message incomeMessage,
			SimpMessageHeaderAccessor headerAccessor) {
		
		String sessionId = headerAccessor.getSessionId();
		headerAccessor.setLeaveMutable(true);
		logger.info("User " + incomeMessage.getUser());
		if (incomeMessage.getType() == MessageType.JOIN) {
			
			if (findUser(incomeMessage.getUser()) == -1) {
			
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
			
		} else if (incomeMessage.getType() == MessageType.STATUS) {
			
			int index = findUser(incomeMessage.getUser());
			connectedUsers.get(index).setStatus(incomeMessage.getContent());
			messagingTemplate.convertAndSend("/topic/public", incomeMessage, headerAccessor.getMessageHeaders());
			
		} else if (incomeMessage.getType() == MessageType.LEAVE) {
			logger.info("Leave " + incomeMessage.getUser());
			int index = findUser(incomeMessage.getUser());
			connectedUsers.remove(index);
		}
		
	}
	
}

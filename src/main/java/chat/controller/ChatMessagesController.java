package chat.controller;

import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import chat.dbhandler.DbHandler;
import chat.dbhandler.InSessionDbHandler;
import chat.dbhandler.JsonDbHandler;
import chat.model.Message;
import chat.model.User;
import chat.model.misc.MessageType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;

@Controller
public class ChatMessagesController {
	
	private static final Logger logger = LoggerFactory.getLogger(ChatMessagesController.class);
	private DbHandler dbHandler = new InSessionDbHandler();
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
			// logger.info(user.getName());
			// logger.info("Compare result: " + userName.contentEquals(user.getName()));
			if (userName.contentEquals(user.getName())) {
				logger.info("Found " + i);
				return i;
			}
		}
		return -1;
	}
	
	private String generateMessageId(String sessionId) {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String id = sessionId + format.format( new Date() );
		return id;
	}
	
	private String historyArrayToString (Message[] historyMessages) {
		JSONArray MessagesJson = new JSONArray();
		for (Message message: historyMessages) {
			JSONObject obj = new JSONObject();
			obj.put("id", message.getId());
			obj.put("user", message.getUser());
			obj.put("content", message.getContent());
			obj.put("type", message.getType().toString());
			MessagesJson.add(obj);
		}
		return MessagesJson.toJSONString();
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
			logger.info(historyContent);
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

	
	@MessageMapping("/chat")
    @SendTo("/topic/public")
	public Message chatMessage(@Payload Message incomeMessage,
			SimpMessageHeaderAccessor headerAccessor) {
		
		String sessionId = headerAccessor.getSessionId();
		incomeMessage.setId(generateMessageId(sessionId));
		logger.info(incomeMessage.getId());
		dbHandler.saveData(incomeMessage);
		return incomeMessage;
		
	}
	

}

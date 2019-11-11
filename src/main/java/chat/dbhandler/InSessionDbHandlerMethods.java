package chat.dbhandler;

import java.util.ArrayList;

import chat.model.Message;

public class InSessionDbHandlerMethods implements DbMethods {

	private ArrayList<Message> messageContainer = new ArrayList<Message>();
	
	private int findIndexOfId (String startId) {
		for (int i=0; i<messageContainer.size(); i++) {
			if (startId.contentEquals(messageContainer.get(i).getId())) {
				
				return i;
			}
		}
		return -1;
	}
	
	public void init() {
		// TODO Auto-generated method stub
		
	}


	public void saveData(Message[] messages) {
		for (Message message: messages) {
			saveData(message);
		}
		
	}


	public void saveData(Message message) {
		messageContainer.add(message);
		
	}


	public Message[] readData(String startId, int shift) {
		int endIndex = findIndexOfId(startId);
		if (endIndex != -1) {
			int startIndex = endIndex - shift;
			if (startIndex < 0) {
				startIndex = 0;
			}
			Message[] retArray = messageContainer.subList(startIndex, endIndex).toArray(new Message[0]);
			return retArray;
		} else {
			return null;
		}
	}

}

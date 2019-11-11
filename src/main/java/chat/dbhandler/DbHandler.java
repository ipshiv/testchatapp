package chat.dbhandler;

import chat.model.Message;

public abstract class DbHandler {
	
	DbMethods dbMethod;

	public DbHandler() {
	}
	
	public void init() {
		dbMethod.init();
	}
	
	public Message[] readData(String startId, int shift) {
		return dbMethod.readData(startId, shift);
	}
	
	public void saveData(Message[] messages) {
		dbMethod.saveData(messages);
	}
	
	public void saveData(Message message) {
		dbMethod.saveData(message);
	}
	

}

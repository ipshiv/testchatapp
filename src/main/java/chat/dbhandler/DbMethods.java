package chat.dbhandler;

import chat.model.Message;

public interface DbMethods {
	public void init();
	public void saveData(Message messages[]);
	public void saveData(Message message);
	public Message[] readData(String startId, int shift);
}

package chat.dbhandler;

import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONObject;

import chat.model.Message;

public class JsonDbHandlerMethods implements DbMethods {

	private JSONObject convertObjToJson(Message message) {
		
		JSONObject obj = new JSONObject();
		obj.put("id", message.getId());
		obj.put("user", message.getUser());
		obj.put("content", message.getContent());
		obj.put("type", message.getType());
		
		return obj;
		
	}
	
	
	public void init() {
		// TODO Auto-generated method stub
		
	}

	public void saveData(Message[] messages) {
		for (Message message: messages) {
			saveData(message);
		}
		
	}

	public Message[] readData(String startId, int shift) {
		// TODO Auto-generated method stub
		return null;
	}

	public void saveData(Message message) {
		
		// TODO implement method!!
		JSONObject objTosave = convertObjToJson(message);
        try (FileWriter file = new FileWriter("./db.json")) {
            file.write(objTosave.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
		
	}
}

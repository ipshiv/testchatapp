package chat.dbhandler;

public class JsonDbHandler extends DbHandler {
	
	public JsonDbHandler() {
		dbMethod = new JsonDbHandlerMethods();
	}

}

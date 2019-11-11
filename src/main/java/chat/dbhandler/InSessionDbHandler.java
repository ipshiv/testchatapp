package chat.dbhandler;

public class InSessionDbHandler extends DbHandler {
	
	public InSessionDbHandler() {
		dbMethod = new InSessionDbHandlerMethods();
	}

}

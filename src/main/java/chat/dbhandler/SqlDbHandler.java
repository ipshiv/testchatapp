package chat.dbhandler;

public class SqlDbHandler extends DbHandler {
	
	public SqlDbHandler() {
		dbMethod = new SqlDbHandlerMethods();
	}

}

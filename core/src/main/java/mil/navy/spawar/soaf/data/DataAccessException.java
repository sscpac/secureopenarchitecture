package mil.navy.spawar.soaf.data;

public class DataAccessException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public DataAccessException() {
		super();
	}

	public DataAccessException(String msg) {
		super(msg);
	}

	public DataAccessException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
 
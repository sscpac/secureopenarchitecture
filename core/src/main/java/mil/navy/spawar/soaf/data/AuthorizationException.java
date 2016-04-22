package mil.navy.spawar.soaf.data;

public class AuthorizationException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public AuthorizationException() {
		super();
	}

	public AuthorizationException(String msg) {
		super(msg);
	}

	public AuthorizationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
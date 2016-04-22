package mil.navy.spawar.soaf.data;

public class RecordNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public RecordNotFoundException() {
		super();
	}

	public RecordNotFoundException(String msg) {
		super(msg);
	}

	public RecordNotFoundException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
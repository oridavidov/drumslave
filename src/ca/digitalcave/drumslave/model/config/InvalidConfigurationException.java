package ca.digitalcave.drumslave.model.config;

public class InvalidConfigurationException extends Exception {
	public static final long serialVersionUID = 0;

	public InvalidConfigurationException() {
		super();
	}
	
	public InvalidConfigurationException(String message) {
		super(message);
	}
	
	public InvalidConfigurationException(Throwable throwable) {
		super(throwable);
	}
	
	public InvalidConfigurationException(String message, Throwable throwable) {
		super(message, throwable);
	}
}

/**
 * 
 */
package ca.digitalcave.drumslave.model;

public class InvalidModelException extends Exception {
	public static final long serialVersionUID = 0l;
	
	public InvalidModelException() {
		super();
	}
	public InvalidModelException(String message) {
		super(message);
	}
	public InvalidModelException(Throwable throwable) {
		super(throwable);
	}
	public InvalidModelException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
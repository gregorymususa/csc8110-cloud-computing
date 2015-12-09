package customservicebusexceptions;

/**
 * Exception thrown, to show that this Topic already exists
 * @author Gregory Mususa (081587717)
 *
 */
public class TopicExistsException extends RuntimeException {

	public TopicExistsException() {
		super("Topic Exists!");
	}
}

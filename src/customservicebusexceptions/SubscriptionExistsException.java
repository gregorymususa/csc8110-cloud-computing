package customservicebusexceptions;

/**
 * Exception thrown, to show that this Subscription already exists
 * @author Gregory Mususa (081587717)
 *
 */
public class SubscriptionExistsException extends RuntimeException {

	public SubscriptionExistsException() {
		super("Subscription exists for this topic!");
	}
}

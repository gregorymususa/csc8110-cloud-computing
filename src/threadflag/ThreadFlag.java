package threadflag;

/**
 * Implements a Flag that Signals for Threads to know when to finish executing
 * 
 * When the UI starts up, this is set to 1; While this flag is 1, the Threads continue running.
 * When User chooses to quit, this is set to 0; When this flag is 0, the Threads stop running.
 * @author Gregory Mususa (081587717)
 *
 */
public class ThreadFlag {
	private static int io = 0;
	
	public static final boolean isRunning() {
		if(io == 1) {
			return true;
		}
		return false;
	}
	
	public static final void run() {
		io = 1;
	}
	
	public static final void stop() {
		io = 0;
	}
}

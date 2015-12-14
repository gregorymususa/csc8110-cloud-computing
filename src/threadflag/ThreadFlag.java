package threadflag;

/**
 * Implements a Flag io that Signals for Threads to know when to finish executing
 * When the UI starts up, this is set to 1; While this flag is 1, the Threads continue running.
 * When User chooses to quit, this is set to 0; When this flag is 0, the Threads stop running.
 * This is aimed at Threads which run continously in the background — and is used as a means to stop them safely
 * 
 * Busy flag, is used for Threads which print to console — this flag was created, to make sure that only 1 thread is printing to the console at any given time
 * @author Gregory Mususa (081587717)
 *
 */
public class ThreadFlag {
	private static int io = 0;
	private static int busy = 0;
	
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
	
	public static final boolean isBusy() {
		if(busy == 1) {
			return true;
		}
		return false;
	}
	
	public static final void setBusy() {
		busy = 1;
	}
	
	public static final void unsetBusy() {
		busy = 0;
	}
}

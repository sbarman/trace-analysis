/**
 * 
 */
package entanglement.util;

/**
 * A simply utility class that models a stop watch.
 * 
 * @author etorlak
 */
public final class StopWatch {
	private long start = 0;
	private long end = 0;
	private long lapsed = 0;
	
	/**
	 * Constructs a new stop watch, with no lapsed time.
	 */
	public StopWatch() { }
	
	/**
	 * Resets the start, end and total times of this watch to zero.
	 */
	public void reset() {
		start = end = lapsed = 0;
	}
	
	/**
	 * Sets the start time of this stop watch to the current time.
	 */
	public void start() {
		start = System.currentTimeMillis();
		end = start;
	}
	
	/**
	 * Stops the watch and adds the interval between this stop and
	 * the most recent started to the total time.
	 */
	public void stop() {
		end = System.currentTimeMillis();
		lapsed += end - start;
	}
	
	/**
	 * Returns the duration of the last recorded interval in miliseconds.
	 * @return duration of the last recorded interval in miliseconds
	 */
	public long lastMilis() { 
		return end - start;
	}
	
	/**
	 * Returns the duration of the last recorded interval in seconds.
	 * @return duration of the last recorded interval in seconds
	 */
	public long lastSecs() { 
		return lastMilis() / 1000;
	}
	 
	/** 
	 * Returns the total duration of all recorded stop/start intervals in miliseconds.
	 * @return total duration of all recorded stop/start intervals in miliseconds.
	 **/
	public long totalMilis() {
		return lapsed;
	}
	
	/** 
	 * Returns the total duration of all recorded stop/start intervals in miliseconds.
	 * @return total duration of all recorded stop/start intervals in miliseconds.
	 **/
	public long totalSecs() {
		return lapsed / 1000;
	}
	
}

/**
 * TimerStrategy.java is a part of King of the Hill. 
 */
package com.valygard.KotH.time;

/**
 * TimerStrategy interface creates hooks for different points in the timer.
 * 
 * @author Anand
 * 
 */
public interface TimerStrategy {

	/**
	 * Listens for timer start
	 */
	public void onStart();

	/**
	 * Listens for timer finish.
	 */
	public void onFinish();

	/**
	 * Listens for timer tick
	 */
	public void onTick();

	/**
	 * Listens for premature timer stop.
	 */
	public void onStop();

	/**
	 * Listens for various points in the timer to perform a given action
	 * 
	 * @param remaining
	 *            the checkpoint time remaining
	 */
	public void onCheckpoint(int remaining);
}

/**
 * TimerCallback.java is a part of King of the Hill. 
 */
package com.valygard.KotH.time;

/**
 * TimerCallback interface creates hooks for different points in the timer.
 * <p>
 * Event-driven programming models of other languages are able to pass function
 * pointers which invoke (i.e, call back) various points in an event. The
 * object-oriented model Java uses does not support this, but interfaces are a
 * fairly robust substitution.
 * 
 * @author Anand
 * 
 */
public interface TimerCallback {

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
}

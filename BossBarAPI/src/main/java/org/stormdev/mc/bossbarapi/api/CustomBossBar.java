package org.stormdev.mc.bossbarapi.api;

import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitTask;

/**
 * Represents a Boss Bar being controlled by BossBarAPI
 *
 */
public class CustomBossBar {
	private BossBar bossBar;
	private BukkitTask timer;
	
	/**
	 * Create a CustomBossBar wrapping Bukkit's
	 * @param bar the bar to wrap
	 */
	public CustomBossBar(BossBar bar){
		this.bossBar = bar;
	}
	
	/**
	 * Get the BossBar from Bukkit
	 * @return The Bukkit Boss Bar
	 */
	public BossBar getBar(){
		return this.bossBar;
	}
	
	/**
	 * Set the task counting down health, used for setting bar for X seconds, etc...
	 * @param timer The timer
	 */
	public void setHealthTimerTask(BukkitTask timer){
		this.timer = timer;
	}
	
	/**
	 * Get the task, if any, being used for ticking down health. If no task, returns null.
	 * @return The task
	 */
	public BukkitTask getHealthTimerTask(){
		return this.timer;
	}
	
	/**
	 * Do any cleanup when the bar is done with, eg. cancel health timer task.
	 */
	public void cleanup(){
		cancelTimerTask();
	}
	
	/**
	 * Check if this bar has a health timer task ticking down the health of the bar
	 * @return True if has timer task
	 */
	public boolean hasTimerTask(){
		return this.timer != null;
	}
	
	/**
	 * Cancels the health timer task for this bar, will stop health ticking down if it is.
	 */
	public void cancelTimerTask(){
		if(this.timer != null){
			this.timer.cancel();
			this.timer = null;
		}
	}
}

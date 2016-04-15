package org.stormdev.mc.bossbarapi.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.stormdev.mc.bossbarapi.plugin.BossBarAPIPlugin;

/**
 * The API
 *
 */
public class BossBarAPI {	
	protected static HashMap<UUID, List<CustomBossBar>> players = new HashMap<UUID, List<CustomBossBar>>();
	
	protected static Object playerRemoveMonitor = new Object();
	
	protected static BossBarAPI instance = null;
	
	protected BossBarAPI(){
		
	}
	
	/**
	 * Get the instance of this API so that you can then use it.
	 * @return The instance of the API
	 */
	public static BossBarAPI getAPI(){
		if(instance == null){
			instance = new BossBarAPI();
		}
		return instance;
	}
	
	/**
	 * Handle a player quitting the server, eg. remove all boss bars
	 * @param player The player who has quit
	 */
	public void handleQuit(Player player){
		synchronized(playerRemoveMonitor){ //Synchronized so if you join then quit immediately it's guaranteed that all bars are actually removed
			removeAllBars(player);
		}
	}
	
	/**
	 * Change the message on an existing Boss Bar
	 * 
	 * @param player
	 *            The player who should see the given message.
	 * @param message
	 *            The message shown to the player.<br>
	 *            
	 */
	public void setMessage(Player player, CustomBossBar bar, String message) {
		bar.getBar().setTitle(cleanMessage(message));
	}
	
	/**
	 * Set a message for all players.<br>
	 * It will remain there for each player until the player logs off.<br>
	 * This method will show a health bar using the given percentage value.
	 * 
	 * @param message
	 *            The message shown to the player.<br>
	 * @param percent
	 *            The percentage of the health bar filled.<br>
	 *            This value must be between 0F (inclusive) and 100F (inclusive).
	 */
	public Map<UUID, CustomBossBar> addGlobalBarToOnline(String message, float percent) {
		Map<UUID, CustomBossBar> res = new HashMap<UUID, CustomBossBar>();
		for (Player player : Bukkit.getOnlinePlayers()) {
			if(!player.isOnline()){
				continue;
			}
			res.put(player.getUniqueId(), addBarWithProgress(player, message, percent/100.0d, BarColor.BLUE, BarStyle.SOLID));
		}
		return res;
	}
	
	/**
	 * Edit the message and percent of a bar already shown to a player<br>
	 * It will remain there until the player logs off or another plugin overrides it.<br>
	 * This method will show a health bar using the given percentage value.
	 * 
	 * @param player
	 *            The player who should see the given message.
	 * @param message
	 *            The message shown to the player.<br>
	 * @param percent
	 *            The percentage of the health bar filled.<br>
	 *            This value must be between 0F (inclusive) and 100F (inclusive).
	 * 
	 */
	public void setMessage(Player player, CustomBossBar cbb, String message, float percent) {
		if(!player.isOnline()){
			return;
		}
		Validate.isTrue(0F <= percent && percent <= 100F, "Percent must be between 0F and 100F, but was: ", percent);
		
		cbb.getBar().setTitle(cleanMessage(message));
		cbb.getBar().setProgress(percent/100.0F);
	}
	
	/**
	 * Set a message for the given player.<br>
	 * It will remain there until the player logs off or another plugin overrides it.<br>
	 * This method will use the health bar as a decreasing timer.<br>
	 * The timer starts with a full bar.<br>
	 * The health bar will be removed automatically when it hits zero.
	 * 
	 * @param player
	 *            The player who should see the given timer/message.
	 * @param message
	 *            The message shown to the player.<br>
	 * @param seconds
	 *            The amount of seconds displayed by the timer.<br>
	 *            Supports values above 1 (inclusive).
	 * @throws IllegalArgumentException
	 *             If seconds is zero or below.
	 */
	public void setMessage(final Player player, CustomBossBar cbb, String message, int seconds) {
		if(!player.isOnline()){
			return;
		}
		Validate.isTrue(seconds > 0, "Seconds must be above 1 but was: ", seconds);
		cbb.getBar().setTitle(cleanMessage(message));
		Runnable run = getSecondsProgressTickDownTask(player, cbb, seconds, -1);
		run.run();
		long delay = 2l;
		BukkitTask t = Bukkit.getScheduler().runTaskTimer(BossBarAPIPlugin.plugin, run, delay, delay);
		cbb.cancelTimerTask();
		cbb.setHealthTimerTask(t);
	}
	
	/**
	 * Gets the list of bossbars a player has
	 * @param player The player
	 * @return the list of bars
	 */
	public List<CustomBossBar> getBars(Player player){
		return new ArrayList<CustomBossBar>(getBarList(player));		
	}
	
	/**
	 * Adds a bar to a player
	 * 
	 * @param player The player to add the bar to
	 * @param message The message to be displayed
	 * @param color The color of the bar
	 * @param style The style of the bar
	 * @param flags The flags to set
	 * @return The bar added
	 */
	public CustomBossBar addBar(final Player player, String message, BarColor color, BarStyle style, BarFlag... flags){
		return addBar(player, message, 1, 0, color, style, flags);
	}
	
	/**
	 * Adds a bar to a player
	 * 
	 * @param player The player to add the bar to
	 * @param message The message to be displayed
	 * @param progress The progress between 0.0 (empty) and 1.0 (full). Make -1 for the bar to tick down as time passes
	 * @param color The color of the bar
	 * @param style The style of the bar
	 * @param flags The flags to set
	 * @return The bar added
	 */
	public CustomBossBar addBarWithProgress(final Player player, String message, final double progress, BarColor color, BarStyle style, BarFlag... flags){
		return addBar(player, message, progress, 0, color, style, flags);
	}
	
	/**
	 * Adds a bar to a player
	 * 
	 * @param player The player to add the bar to
	 * @param message The message to be displayed
	 * @param seconds The time, in seconds, for the bar to be visible. Set to 0 for the bar to last forever
	 * @param color The color of the bar
	 * @param style The style of the bar
	 * @param flags The flags to set
	 * @return The bar added
	 */
	public CustomBossBar addBarForSeconds(final Player player, String message, final double seconds, BarColor color, BarStyle style, BarFlag... flags){
		return addBar(player, message, -1, seconds, color, style, flags);
	}
	
	private Runnable getSecondsProgressTickDownTask(final Player player, final CustomBossBar cbb, final double seconds, final double fixedBarProgress){
		final long startTime = System.currentTimeMillis();
		return new Runnable(){
			
			@Override
			public void run() {
				long length = (long) (seconds*1000L);
				long elapsed = System.currentTimeMillis()-startTime;
				double prog = ((double)elapsed)/((double)length);
				if(prog < 0){
					prog = 0;
				}
				if(prog > 1){
					prog = 1;
				}
				prog = 1-prog;
				if(fixedBarProgress == -1){
					cbb.getBar().setProgress(prog);
				}
				if(prog <= 0){
					removeBar(player, cbb);
				}
			}};
	}
	
	/**
	 * Adds a bar to a player
	 * 
	 * @param player The player to add the bar to
	 * @param message The message to be displayed
	 * @param progress The progress between 0.0 (empty) and 1.0 (full). Make -1 for the bar to tick down as time passes
	 * @param seconds The time, in seconds, for the bar to be visible. Set to 0 for the bar to last forever
	 * @param color The color of the bar
	 * @param style The style of the bar
	 * @param flags The flags to set
	 * @return The bar added
	 */
	public CustomBossBar addBar(final Player player, String message, final double progress, final double seconds, BarColor color, BarStyle style, BarFlag... flags){
		synchronized(players){
			BossBar b = Bukkit.createBossBar(cleanMessage(message), color, style, flags);
			b.setProgress(progress);
			final CustomBossBar cbb = new CustomBossBar(b);
			getBarList(player).add(cbb);
			b.addPlayer(player);
			if(seconds == 0){
				return cbb;
			}
			Runnable run = getSecondsProgressTickDownTask(player, cbb, seconds, progress);
			run.run();
			long delay = progress == -1 ? 2l : 40l;
			BukkitTask t = Bukkit.getScheduler().runTaskTimer(BossBarAPIPlugin.plugin, run, delay, delay);
			Bukkit.getScheduler().runTaskLater(BossBarAPIPlugin.plugin, new Runnable(){

				@Override
				public void run() {
					BossBar b = cbb.getBar();
					if(b.getPlayers().contains(player)){
						removeBar(player, cbb);
					}
					return;
				}}, (long) (seconds*20l));
			cbb.setHealthTimerTask(t);
			return cbb;
		}
	}
	
	private List<CustomBossBar> getBarList(Player player){
		List<CustomBossBar> bars = players.get(player.getUniqueId());
		if(bars == null){
			bars = new ArrayList<CustomBossBar>();
			synchronized(playerRemoveMonitor){
				if(player.isOnline()){
					players.put(player.getUniqueId(), bars);
				}
			}
		}
		return bars;	
	}
	
	/**
	 * Checks whether the given player has a bar.
	 * 
	 * @param player
	 *            The player who should be checked.
	 * @return True, if the player has a bar, False otherwise.
	 */
	public boolean hasBar(Player player) {
		List<CustomBossBar> bars = players.get(player.getUniqueId());
		return bars != null && bars.size() > 0;
	}
	
	/**
	 * Removes a BossBar from the player
	 * @param player The player
	 * @param bar The bar to remove
	 */
	public void removeBar(Player player, CustomBossBar bar){
		synchronized(players){
			getBarList(player).remove(bar);
			bar.getBar().removePlayer(player);
			bar.cleanup();
			if(!player.isOnline()){
				players.remove(player.getUniqueId());
			}
		}
	}
	
	/**
	 * Removes the bars from the given player.<br>
	 * If the player has no bar, this method does nothing.
	 * 
	 * @param player
	 *            The player whose bar should be removed.
	 */
	public void removeAllBars(Player player) {
		List<CustomBossBar> bars = getBars(player);
		for(CustomBossBar cbb:bars){
			removeBar(player, cbb);
		}
		
		if(!player.isOnline()){
			players.remove(player.getUniqueId());
		}
	}
	
	/**
	 * Modifies the health of an existing bar.<br>
	 * If the player has no bar, this method does nothing.
	 * 
	 * @param player
	 *            The player whose bar should be modified.
	 * @param percent
	 *            The percentage of the health bar filled.<br>
	 *            This value must be between 0F and 100F (inclusive).
	 */
	public void setHealth(Player player, CustomBossBar bar, float percent) {
		bar.cancelTimerTask();
		bar.getBar().setProgress(percent/100.0f);
	}
	
	/**
	 * Get the health of an existing bar.
	 * 
	 * @param player
	 *            The player whose bar's health should be returned.
	 * @return The current absolute health of the bar.<br>
	 *         If the player has no bar, this method returns -1.
	 * 
	 */
	public float getHealth(Player player, CustomBossBar bar) {
		if (bar == null){
			return -1;
		}
		
		return (float) (bar.getBar().getProgress()*100.0f);
	}
	
	/**
	 * Get the message of an existing bar.
	 * 
	 * @param player
	 *            The player whose bar's message should be returned.
	 * @return The current message displayed to the player.<br>
	 *         If the player has no bar, this method returns an empty string.
	 */
	public String getMessage(Player player, CustomBossBar bar) {
		if (bar == null)
			return "";

		return bar.getBar().getTitle();
	}
	
	private String cleanMessage(String message) {
		return message;
	}
}

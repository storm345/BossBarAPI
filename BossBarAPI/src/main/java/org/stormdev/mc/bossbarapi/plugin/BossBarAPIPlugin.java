package org.stormdev.mc.bossbarapi.plugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.stormdev.mc.bossbarapi.api.BossBarAPI;

public class BossBarAPIPlugin extends JavaPlugin {
	public static BossBarAPIPlugin plugin;
	
	@Override
	public void onEnable(){
		plugin = this;
		
		Bukkit.getPluginManager().registerEvents(new BossBarAPIListener(), this);
	}
	
	@Override
	public void onDisable(){
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			BossBarAPI.getAPI().handleQuit(player);
		}
	}
}

package org.stormdev.mc.bossbarapi.plugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.stormdev.mc.bossbarapi.api.BossBarAPI;

public class BossBarAPIListener implements Listener {
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void PlayerLoggout(PlayerQuitEvent event) {
		BossBarAPI.getAPI().handleQuit(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerKick(PlayerKickEvent event) {
		BossBarAPI.getAPI().handleQuit(event.getPlayer());
	}
}

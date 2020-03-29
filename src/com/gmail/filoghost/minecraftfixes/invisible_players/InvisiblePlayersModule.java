/*
 * Copyright (c) 2020, Wild Adventure
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 * 4. Redistribution of this software in source or binary forms shall be free
 *    of all charges or fees to the recipient of this software.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gmail.filoghost.minecraftfixes.invisible_players;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.gmail.filoghost.minecraftfixes.MinecraftFixes;
import com.gmail.filoghost.minecraftfixes.Module;

import wild.core.utils.MathUtils;

public class InvisiblePlayersModule extends Module implements Listener {

	private final int viewDistanceSquared;
	
	public InvisiblePlayersModule() {
		super("FixInvisiblePlayers", true);
		viewDistanceSquared = MathUtils.square(64); // Tracker Spigot Entity range
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onTeleport(PlayerTeleportEvent event) {
		refreshPlayer(event.getPlayer());
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onRespawn(PlayerRespawnEvent event) {
		refreshPlayer(event.getPlayer());
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onJoin(PlayerJoinEvent event) {
		refreshPlayer(event.getPlayer());
	}

	private void refreshPlayer(Player player) {
		Bukkit.getScheduler().runTaskLater(MinecraftFixes.plugin, () -> {
			
			if (!player.isOnline()) {
				return;
			}
			
			Location currentLocation = player.getLocation();
			List<Player> showLater = new ArrayList<>();
			
			for (Player other : Bukkit.getOnlinePlayers()) {
				if (player != other && player.getWorld() == other.getWorld() && other.canSee(player) && distanceSquaredXZ(currentLocation, other.getLocation()) < viewDistanceSquared) {
					other.hidePlayer(player);
					showLater.add(other);
				}
			}
			
			if (showLater.isEmpty()) {
				return;
			}
			
			Bukkit.getScheduler().runTaskLater(MinecraftFixes.plugin, () -> {
				if (!player.isOnline()) {
					return;
				}
				
				for (Player other : showLater) {
					if (other.isOnline()) {
						other.showPlayer(player);
					}
				}
			}, 1);

		}, 10);
	}
	
	private double distanceSquaredXZ(Location loc1, Location loc2) {
		return MathUtils.square(loc1.getX() - loc2.getX()) + MathUtils.square(loc1.getZ() - loc2.getZ());
	}
	
	
	
}

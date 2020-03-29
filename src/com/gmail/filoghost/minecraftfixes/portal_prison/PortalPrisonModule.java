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
package com.gmail.filoghost.minecraftfixes.portal_prison;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import wild.api.WildCommons;

import com.gmail.filoghost.minecraftfixes.MinecraftFixes;
import com.gmail.filoghost.minecraftfixes.Module;

public class PortalPrisonModule extends Module implements Listener {
	
	private static final long STUCK_TIME_PROMPT = 		6000;
	private static final long PROMPT_INTERVAL = 		30000;
	private static final long MIN_EMERGENCY_COOLDOWN = 	1000;
	
	// Memorizza quando il giocatore è stato possibilmente imprigionato
	private Map<Player, Long> possiblyStuck;
	private Map<Player, Long> lastPromptMessage;
	private Map<Player, Long> lastEmergencyCommand;

	public PortalPrisonModule() {
		super("PortalPrisonFix", false);
		
		possiblyStuck = new HashMap<>();
		lastPromptMessage = new HashMap<>();
		lastEmergencyCommand = new HashMap<>();
		
		// Long poll task
		new BukkitRunnable() {
			
			@Override
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					if (isInPortal(player) && !possiblyStuck.containsKey(player)) {
						possiblyStuck.put(player, System.currentTimeMillis());
					}
				}
				
			}
		}.runTaskTimer(MinecraftFixes.plugin, 0, 10);
		
		
		// Short poll task
		new BukkitRunnable() {
			
			@Override
			public void run() {
				long now = System.currentTimeMillis();
				Iterator<Entry<Player, Long>> iter = possiblyStuck.entrySet().iterator();
				
				while (iter.hasNext()) {
					Entry<Player, Long> entry = iter.next();
					
					if (isInPortal(entry.getKey())) {
						if (now - entry.getValue() >= STUCK_TIME_PROMPT) {
							tryPrompt(entry.getKey(), now);
						}
					} else {
						// Via, non controlliamo più per ora
						iter.remove();
					}
				}
			}
		}.runTaskTimer(MinecraftFixes.plugin, 5, 5);
	}
	
	
	private void tryPrompt(Player player, long now) {
		Long lastMessage = lastPromptMessage.get(player);
		if (lastMessage == null || now - lastMessage > PROMPT_INTERVAL) {
			player.sendMessage("");
			player.sendMessage(" " + ChatColor.RED + ChatColor.BOLD + "Sei bloccato nel portale? " + ChatColor.YELLOW + "Puoi tornare allo spawn tenendo premuto SHIFT e cliccando MOUSE DESTRO.");
			player.sendMessage("");
			lastPromptMessage.put(player, now);
		}
	}
	
	
	private boolean isInPortal(Player player) {
		return WildCommons.getPortalCooldown(player) > 0;
	}
	
	@EventHandler
	public void onRightClick(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
			Player player = event.getPlayer();
			if (player.isSneaking() && possiblyStuck.containsKey(player)) {
			
				Long lastCommand = lastEmergencyCommand.get(player);
				long now = System.currentTimeMillis();
				
				if (lastCommand == null || now - lastCommand > MIN_EMERGENCY_COOLDOWN) {
					lastEmergencyCommand.put(player, now);
					player.chat("/spawn");
				}
			}
		}
	}
	
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		possiblyStuck.remove(event.getPlayer());
		lastPromptMessage.remove(event.getPlayer());
		lastEmergencyCommand.remove(event.getPlayer());
	}
	
}

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
package com.gmail.filoghost.minecraftfixes.nether_roof;

import org.bukkit.ChatColor;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.gmail.filoghost.minecraftfixes.Module;

public class NetherRoofModule extends Module implements Listener {

	public NetherRoofModule() {
		super("NoNetherRoof", false);
	}

	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onTeleport(PlayerTeleportEvent event) {
		if (event.getTo().getWorld().getEnvironment() == Environment.NETHER) {
				
			if (event.getTo().getY() > 125) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED + "Non puoi andare così in alto nel Nether.");
			}
		}
	}
	
	private void checkBlockEvent(Player player, Cancellable event, Block block) {
		if (block.getWorld().getEnvironment() == Environment.NETHER && block.getY() >= 127) {
			event.setCancelled(true);
			player.sendMessage(ChatColor.RED + "Non puoi costruire così in alto nel Nether.");
		}
	}
	
	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlace(BlockPlaceEvent event) {
		checkBlockEvent(event.getPlayer(), event, event.getBlock());
	}
	
	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlace(BlockBreakEvent event) {
		checkBlockEvent(event.getPlayer(), event, event.getBlock());
	}
	
	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBucketEmpty(PlayerBucketEmptyEvent event) {
		Block involvedBlock = event.getBlockClicked().getRelative(event.getBlockFace());
		checkBlockEvent(event.getPlayer(), event, involvedBlock);
	}
	
	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBucketFill(PlayerBucketFillEvent event) {
		checkBlockEvent(event.getPlayer(), event, event.getBlockClicked());
	}
	
}

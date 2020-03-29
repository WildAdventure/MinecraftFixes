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
package com.gmail.filoghost.minecraftfixes.tnt_duplication;

import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.filoghost.minecraftfixes.MinecraftFixes;
import com.gmail.filoghost.minecraftfixes.Module;
import com.google.common.collect.Sets;

public class TntDuplicationModule extends Module implements Listener {
	
	private Set<World> worldsToCheck = Sets.newHashSet();
	
	public TntDuplicationModule() {
		super("FixTntDuplication", true);
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				if (!worldsToCheck.isEmpty()) {
					for (World world : worldsToCheck) {
						for (TNTPrimed tnt : world.getEntitiesByClass(TNTPrimed.class)) {
							if (tnt.getTicksLived() <= 6) {
								Block block = tnt.getLocation().add(0, 0.5, 0).getBlock(); // Get block in center
								if (block.getType() == Material.TNT) {
									block.setType(Material.AIR);
								}
							}
						}
					}
					
					worldsToCheck.clear();
				}
			}
			
		}.runTaskTimer(MinecraftFixes.plugin, 1, 1);
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPistonExtend(BlockPistonExtendEvent event) {
		checkPistonEvent(event.getBlocks());
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPistonRetract(BlockPistonRetractEvent event) {
		checkPistonEvent(event.getBlocks());
	}
	
	private void checkPistonEvent(List<Block> blocks) {
		for (final Block block : blocks) {
			if (block.getType() == Material.TNT) {
				new BukkitRunnable() {
					
					@Override
					public void run() {
						worldsToCheck.add(block.getWorld());
					}
					
				}.runTaskLater(MinecraftFixes.plugin, 3);
				return;
			}
		}
	}
	
}

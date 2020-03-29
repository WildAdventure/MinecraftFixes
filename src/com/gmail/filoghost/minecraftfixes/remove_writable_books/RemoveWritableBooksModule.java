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
package com.gmail.filoghost.minecraftfixes.remove_writable_books;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.gmail.filoghost.minecraftfixes.MinecraftFixes;
import com.gmail.filoghost.minecraftfixes.Module;
import com.google.common.collect.Lists;

import wild.api.util.FileLogger;

public class RemoveWritableBooksModule extends Module implements Listener {
	
	private FileLogger logger;
	
	public RemoveWritableBooksModule() {
		super("RemoveWritableBooks", false);
		logger = new FileLogger(MinecraftFixes.plugin, "removed-books.log");
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onCraft(CraftItemEvent event) {
		if (isWrittenBook(event.getRecipe().getResult())) {
			event.setCancelled(true);
			event.getWhoClicked().sendMessage(ChatColor.RED + "I libri scrivibili sono stati disabilitati a causa di un bug.");
		}
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent event) {
		clearInventory(event.getPlayer().getInventory());
		clearInventory(event.getPlayer().getEnderChest(), "enderchest di " + event.getPlayer().getName());
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onInventoryOpen(InventoryOpenEvent event) {
		clearInventory(event.getInventory());
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onInventoryClick(InventoryClickEvent event) {
		if (isWrittenBook(event.getCurrentItem()) || isWrittenBook(event.getCursor())) {
			event.setCancelled(true);
			
			Bukkit.getScheduler().runTaskLater(MinecraftFixes.plugin, () -> {
				clearInventory(event.getView().getTopInventory());
				clearInventory(event.getView().getBottomInventory());
			}, 1L);
		}
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onItemPickup(PlayerPickupItemEvent event) {
		if (isWrittenBook(event.getItem().getItemStack())) {
			event.setCancelled(true);
			
			Bukkit.getScheduler().runTaskLater(MinecraftFixes.plugin, () -> {
				event.getItem().remove();
			}, 1L);
		}
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onItemDrop(PlayerDropItemEvent event) {
		if (isWrittenBook(event.getItemDrop().getItemStack())) {
			event.setCancelled(true);
			
			Bukkit.getScheduler().runTaskLater(MinecraftFixes.plugin, () -> {
				clearInventory(event.getPlayer().getInventory());
			}, 1L);
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onChunkLoad(ChunkLoadEvent event) {
		for (BlockState tileEntity : event.getChunk().getTileEntities()) {
			if (tileEntity instanceof Inventory) {
				clearInventory((Inventory) tileEntity);
			}
		}
		
		for (Entity entity : event.getChunk().getEntities()) {
			if (entity instanceof InventoryHolder) {
				clearInventory(((InventoryHolder) entity).getInventory());
			}
		}
	}
	
	private void clearInventory(Inventory inventory) {
		clearInventory(inventory, null);
	}
	
	private void clearInventory(Inventory inventory, String inventoryName) {
		if (inventory == null) {
			return;
		}
		
		int size = inventory.getSize();
		List<String> logLines = null;
		
		for (int i = 0; i < size; i++) {
			if (isWrittenBook(inventory.getItem(i))) {
				BookMeta book = (BookMeta) inventory.getItem(i).getItemMeta();
				String firstPage = book.getPageCount() > 0 ? book.getPage(1) : "<pagina vuota>";
				
				inventory.setItem(i, null);
				
				if (logLines == null) {
					logLines = Lists.newArrayList();
				}

				if (inventoryName == null) {
					if (inventory.getHolder() instanceof BlockState) {
						Block block = ((BlockState) inventory.getHolder()).getBlock();
						inventoryName = "blocco " + block.getType() + " in " + block.getWorld().getName() + " x= " + block.getX() + " y=" + block.getY() + " z=" + block.getZ();
					} else if (inventory.getHolder() instanceof DoubleChest) {
						Block block = ((DoubleChest) inventory.getHolder()).getLocation().getBlock();
						inventoryName = "doppia cassa in " + block.getWorld().getName() + " x= " + block.getX() + " y=" + block.getY() + " z=" + block.getZ();
					} else if (inventory.getHolder() instanceof Player) {
						inventoryName = "giocatore " + ((Player) inventory.getHolder()).getName();
					} else if (inventory.getHolder() instanceof Entity) {
						Entity entity = (Entity) inventory.getHolder();
						Location loc = entity.getLocation();
						inventoryName = "entità " + entity.getType() + " in " + loc.getWorld().getName() + " x= " + loc.getX() + " y=" + loc.getY() + " z=" + loc.getZ();
					} else {
						inventoryName = "" + inventory + " (holder: " + inventory.getHolder() + ", tipo: " + inventory.getType() + ")";
					}
				}
				
				logLines.add("Rimosso libro dall'inventario del " + inventoryName + ", la prima pagina era: " + firstPage);
			}
		}
		
		if (logLines != null) {
			List<String> logLinesFinal = logLines;
			Bukkit.getScheduler().runTaskAsynchronously(MinecraftFixes.plugin, () -> {
				for (String line : logLinesFinal) {
					logger.log(line);
				}
			});
		}
	}

	private boolean isWrittenBook(ItemStack item) {
		return item != null && isWrittenBook(item.getType());
	}
	
	private boolean isWrittenBook(Material mat) {
		return mat == Material.BOOK_AND_QUILL || mat == Material.WRITTEN_BOOK;
	}
	
}

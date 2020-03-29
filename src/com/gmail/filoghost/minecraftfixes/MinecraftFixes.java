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
package com.gmail.filoghost.minecraftfixes;

import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.filoghost.minecraftfixes.book_colors.BookColorsModule;
import com.gmail.filoghost.minecraftfixes.dispenser_gateway.NoDispenserGatewayModule;
import com.gmail.filoghost.minecraftfixes.enderpearls_phase.EnderpearlsPhaseModule;
import com.gmail.filoghost.minecraftfixes.enderpearls_timeout.EnderpearlsTimeoutModule;
import com.gmail.filoghost.minecraftfixes.invisible_players.InvisiblePlayersModule;
import com.gmail.filoghost.minecraftfixes.nether_roof.NetherRoofModule;
import com.gmail.filoghost.minecraftfixes.no_entities_through_portals.NoEntitiesThroughPortalsModule;
import com.gmail.filoghost.minecraftfixes.no_portals_from_nether.PortalsModule;
import com.gmail.filoghost.minecraftfixes.piston_dupe.PistonDupeModule;
import com.gmail.filoghost.minecraftfixes.portal_prison.PortalPrisonModule;
import com.gmail.filoghost.minecraftfixes.remove_writable_books.RemoveWritableBooksModule;
import com.gmail.filoghost.minecraftfixes.tnt_duplication.TntDuplicationModule;
import com.google.common.collect.Lists;

public class MinecraftFixes extends JavaPlugin implements Listener {
	
	public static MinecraftFixes plugin;
	
	@Override
	public void onEnable() {
		plugin = this;
		
		List<Module> modules = Lists.newArrayList();
		modules.add(new PortalsModule());
		modules.add(new PortalPrisonModule());
		modules.add(new NetherRoofModule());
		modules.add(new EnderpearlsPhaseModule());
		modules.add(new BookColorsModule());
		modules.add(new TntDuplicationModule());
		modules.add(new NoDispenserGatewayModule());
		modules.add(new InvisiblePlayersModule());
		modules.add(new RemoveWritableBooksModule());
		modules.add(new EnderpearlsTimeoutModule());
		modules.add(new PistonDupeModule());
		modules.add(new NoEntitiesThroughPortalsModule());
		
		
		saveDefaultConfig();
		FileConfiguration config = getConfig();
		
		boolean save = false;
		for (Module m : modules) {
			if (!config.isSet(m.getId())) {
				config.set(m.getId(), m.isEnabledByDefault());
				save = true;
			}
		}
		
		if (save) {
			saveConfig();
		}
		
		for (Module m : modules) {
			if (config.getBoolean(m.getId())) {
				m.enable(this);
			}
		}
	}
}

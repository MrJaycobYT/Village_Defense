/*
 * Village Defense - Protect villagers from hordes of zombies
 * Copyright (C) 2019  Plajer's Lair - maintained by Plajer and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.plajer.villagedefense.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import pl.plajer.villagedefense.ConfigPreferences;
import pl.plajer.villagedefense.Main;
import pl.plajer.villagedefense.api.StatsStorage;
import pl.plajer.villagedefense.arena.ArenaRegistry;
import pl.plajer.villagedefense.handlers.PermissionsManager;
import pl.plajer.villagedefense.utils.UpdateChecker;
import pl.plajerlair.commonsbox.minecraft.serialization.InventorySerializer;

/**
 * Created by Tom on 10/07/2015.
 */
public class JoinEvent implements Listener {

  private Main plugin;

  public JoinEvent(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
      plugin.getUserManager().loadStatistic(plugin.getUserManager().getUser(event.getPlayer()), stat);
    }
    //load player inventory in case of server crash, file is deleted once loaded so if file was already
    //deleted player won't receive his backup, in case of crash he will get it back
    if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
      InventorySerializer.loadInventory(plugin, event.getPlayer());
    }

    int amount = plugin.getModuleLoader().getNotLoadedModulesAmount();
    if(event.getPlayer().hasPermission("villagedefense.admin.modules") && amount > 0) {
      Bukkit.getScheduler().runTaskLater(plugin, () -> {
        event.getPlayer().sendMessage("");
        event.getPlayer().sendMessage(plugin.getChatManager().colorRawMessage("&6&lVillage Defense Module Notifier"));
        event.getPlayer().sendMessage("");
        event.getPlayer().sendMessage(plugin.getChatManager().colorRawMessage("&eThere are &4&l" + amount + " modules &ethat failed to load! " +
            "Check them out via &6/vda modules"));
      }, 30);
    }
  }

  @EventHandler
  public void onJoinCheckVersion(final PlayerJoinEvent event) {
    //we want to be the first :)
    if (!plugin.getConfig().getBoolean("Update-Notifier.Enabled", true) || !event.getPlayer().hasPermission("villagedefense.updatenotify")) {
      return;
    }
    Bukkit.getScheduler().runTaskLater(plugin, () -> UpdateChecker.init(plugin, 41869).requestUpdateCheck().whenComplete((result, exception) -> {
      if (!result.requiresUpdate()) {
        return;
      }
      if (result.getNewestVersion().contains("b")) {
        event.getPlayer().sendMessage("");
        event.getPlayer().sendMessage(ChatColor.BOLD + "VILLAGE DEFENSE UPDATE NOTIFY");
        event.getPlayer().sendMessage(ChatColor.RED + "BETA version of software is ready for update! Proceed with caution.");
        event.getPlayer().sendMessage(ChatColor.YELLOW + "Current version: " + ChatColor.RED + plugin.getDescription().getVersion() + ChatColor.YELLOW + " Latest version: " + ChatColor.GREEN + result.getNewestVersion());
      } else {
        event.getPlayer().sendMessage("");
        event.getPlayer().sendMessage(ChatColor.BOLD + "VILLAGE DEFENSE UPDATE NOTIFY");
        event.getPlayer().sendMessage(ChatColor.GREEN + "Software is ready for update! Download it to keep with latest changes and fixes.");
        event.getPlayer().sendMessage(ChatColor.YELLOW + "Current version: " + ChatColor.RED + plugin.getDescription().getVersion() + ChatColor.YELLOW + " Latest version: " + ChatColor.GREEN + result.getNewestVersion());
      }
    }), 25);
  }
}

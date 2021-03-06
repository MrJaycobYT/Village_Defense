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

package pl.plajer.villagedefense.arena.states;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import pl.plajer.villagedefense.ConfigPreferences;
import pl.plajer.villagedefense.Main;
import pl.plajer.villagedefense.arena.Arena;
import pl.plajer.villagedefense.arena.ArenaManager;
import pl.plajer.villagedefense.arena.ArenaState;
import pl.plajer.villagedefense.handlers.language.Messages;
import pl.plajer.villagedefense.utils.constants.Constants;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;

/**
 * @author Plajer
 * <p>
 * Created at 03.06.2019
 */
public class RestartingState implements ArenaStateHandler {

  private Main plugin;

  @Override
  public void init(Main plugin) {
    this.plugin = plugin;
  }

  @Override
  public void handleCall(Arena arena) {
    arena.getMapRestorerManager().fullyRestoreArena();
    arena.getPlayers().clear();
    arena.setArenaState(ArenaState.WAITING_FOR_PLAYERS);

    arena.resetOptionValues();
    arena.getDroppedFleshes().stream().filter(Objects::nonNull).forEach(Entity::remove);
    arena.getDroppedFleshes().clear();
    if (plugin.getModuleLoader().isModulePresent("Bungee Cord")) {
      if (ConfigUtils.getConfig(plugin, "bungee").getBoolean("Shutdown-When-Game-Ends", false)) {
        plugin.getServer().shutdown();
      }
      for(Player player : Bukkit.getOnlinePlayers()) {
        ArenaManager.joinAttempt(player, arena);
      }
    }
    arena.getGameBar().setTitle(plugin.getChatManager().colorMessage(Messages.BOSSBAR_WAITING_FOR_PLAYERS));
  }

}

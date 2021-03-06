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

package pl.plajer.villagedefense.commands.arguments.admin;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import pl.plajer.villagedefense.commands.arguments.ArgumentsRegistry;
import pl.plajer.villagedefense.commands.arguments.data.CommandArgument;
import pl.plajer.villagedefense.commands.arguments.data.LabelData;
import pl.plajer.villagedefense.commands.arguments.data.LabeledCommandArgument;

/**
 * @author Plajer
 * <p>
 * Created at 24.11.2018
 */
public class SpyChatArgument {

  private Set<Player> spyChatters = new HashSet<>();

  public SpyChatArgument(ArgumentsRegistry registry) {
    registry.mapArgument("villagedefenseadmin", new LabeledCommandArgument("spychat", "villagedefense.admin.spychat", CommandArgument.ExecutorType.PLAYER,
        new LabelData("/vda spychat", "/vda spychat", "&7Toggles spy chat for all available arenas\n"
            + "&7You will see all messages from these games\n&6Permission: &7villagedefense.admin.spychat")) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (spyChatters.contains(player)) {
          spyChatters.remove(player);
        } else {
          spyChatters.add(player);
        }
        sender.sendMessage(registry.getPlugin().getChatManager().getPrefix() + ChatColor.GREEN + "Game spy chat toggled to " + spyChatters.contains(player));
      }
    });
  }

  public boolean isSpyChatEnabled(Player player) {
    return spyChatters.contains(player);
  }

  public void disableSpyChat(Player player) {
    spyChatters.remove(player);
  }

}

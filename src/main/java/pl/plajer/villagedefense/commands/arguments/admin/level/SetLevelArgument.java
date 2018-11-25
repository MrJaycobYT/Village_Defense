/*
 * Village Defense 4 - Protect villagers from hordes of zombies
 * Copyright (C) 2018  Plajer's Lair - maintained by Plajer and Tigerpanzer
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

package pl.plajer.villagedefense.commands.arguments.admin.level;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import pl.plajer.villagedefense.api.StatsStorage;
import pl.plajer.villagedefense.commands.arguments.ArgumentsRegistry;
import pl.plajer.villagedefense.commands.arguments.data.CommandArgument;
import pl.plajer.villagedefense.commands.arguments.data.LabelData;
import pl.plajer.villagedefense.commands.arguments.data.LabeledCommandArgument;
import pl.plajer.villagedefense.handlers.ChatManager;
import pl.plajer.villagedefense.user.User;
import pl.plajer.villagedefense.user.UserManager;
import pl.plajer.villagedefense.utils.Utils;

/**
 * @author Plajer
 * <p>
 * Created at 24.11.2018
 */
public class SetLevelArgument {

  public SetLevelArgument(ArgumentsRegistry registry) {
    registry.mapArgument("villagedefenseadmin", new LabeledCommandArgument("setlevel", Arrays.asList("villagedefense.admin.setlevel", "villagedefense.admin.setlevel.others"),
        CommandArgument.ExecutorType.BOTH, new LabelData("/vda setlevel  &6<amount> &c[player]", "/vda setlevel <amount>",
        "&7Set level to yourself or target player\n&7Can be used from console too\n&6Permission: &7villagedefense.admin.setlevel (for yourself)\n" +
            "&6Permission: &7villagedefense.admin.setlevel.others (for others)")) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        if (args.length == 1) {
          sender.sendMessage(ChatManager.PLUGIN_PREFIX + ChatColor.RED + "Please type number of levels to set!");
          return;
        }
        Player target;
        if (args.length == 2) {
          target = (Player) sender;
        } else {
          target = Bukkit.getPlayerExact(args[2]);
        }

        if (target == null) {
          sender.sendMessage(ChatManager.colorMessage("Commands.Target-Player-Not-Found"));
          return;
        }
        if (!(sender.equals(target) && Utils.hasPermission(sender, "villagedefense.admin.setlevel.others"))) {
          return;
        }

        if (Utils.isInteger(args[1])) {
          User user = UserManager.getUser(target.getUniqueId());
          user.setStat(StatsStorage.StatisticType.LEVEL, Integer.parseInt(args[1]));
          sender.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("Commands.Admin-Commands.Added-Level"));
        } else {
          sender.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("Commands.Wrong-Usage").replace("%correct%", "/vda setlevel <amount> [player]"));
        }
      }
    });
  }

}

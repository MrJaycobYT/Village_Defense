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

package pl.plajer.villagedefense.utils;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.bukkit.Bukkit;

import pl.plajer.villagedefense.Main;
import pl.plajer.villagedefense.utils.services.exception.ReportedException;

/**
 * @author Plajer
 * <p>
 * Created at 14.02.2019
 */
public class ExceptionLogHandler extends Handler {

  //these classes if found in stacktraces won't be reported
  //to the Error Service
  private List<String> blacklistedClasses = Arrays.asList("pl.plajer.villagedefense.user.data.MysqlManager", "pl.plajer.villagedefense.plajerlair.commonsbox.database.MysqlDatabase");
  private Main plugin;

  public ExceptionLogHandler(Main plugin) {
    this.plugin = plugin;
    Bukkit.getLogger().addHandler(this);
  }

  @Override
  public void close() {
    //unused in this handler
  }

  @Override
  public void flush() {
    //unused in this handler
  }

  @Override
  public void publish(LogRecord record) {
    Throwable throwable = record.getThrown();
    if (!(throwable instanceof Exception) || !throwable.getClass().getSimpleName().contains("Exception")) {
      return;
    }
    if (throwable.getStackTrace().length == 0
        || throwable.getCause() != null ? !throwable.getCause().getStackTrace()[0].getClassName().contains("pl.plajer.villagedefense")
        : !throwable.getStackTrace()[0].getClassName().contains("pl.plajer.villagedefense")) {
      return;
    }
    if (containsBlacklistedClass(throwable)) {
      return;
    }
    new ReportedException(plugin, (Exception) throwable);
    record.setThrown(null);
    record.setMessage("[VillageDefense] We have found a bug in the code. Contact us at our official discord server (Invite link: https://discordapp.com/invite/UXzUdTP) with the following error given above!");
  }

  private boolean containsBlacklistedClass(Throwable throwable) {
    for (StackTraceElement element : throwable.getStackTrace()) {
      for (String blacklist : blacklistedClasses) {
        if (element.getClassName().contains(blacklist)) {
          return true;
        }
      }
    }
    return false;
  }

}

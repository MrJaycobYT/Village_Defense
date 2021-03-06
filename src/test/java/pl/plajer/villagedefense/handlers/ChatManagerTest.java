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

package pl.plajer.villagedefense.handlers;

import org.bukkit.ChatColor;
import org.junit.Assert;
import org.junit.Test;

import pl.plajer.villagedefense.MainMock;
import pl.plajer.villagedefense.MockUtils;
import pl.plajer.villagedefense.handlers.language.Messages;

/**
 * @author Plajer
 * <p>
 * Created at 02.06.2019
 */
public class ChatManagerTest {

  @Test
  public void colorRawMessage() {
    String rawColored = "&6Test";
    String chatColored = ChatColor.GOLD + "Test";
    Assert.assertEquals(chatColored, MockUtils.getPluginMockSafe().getChatManager().colorRawMessage(rawColored));
  }

  @Test
  public void colorMessage() {
    MainMock plugin = MockUtils.getPluginMockSafe();
    //in case if message has been changed replace with other
    Assert.assertEquals(plugin.getChatManager().colorRawMessage("&lInactive..."), plugin.getChatManager().colorMessage(Messages.SIGNS_GAME_STATES_INACTIVE));
  }

}
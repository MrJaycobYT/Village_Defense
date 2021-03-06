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

package pl.plajer.villagedefense.arena;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.TestOnly;

import pl.plajer.villagedefense.ConfigPreferences;
import pl.plajer.villagedefense.Main;
import pl.plajer.villagedefense.api.event.game.VillageGameStateChangeEvent;
import pl.plajer.villagedefense.arena.managers.MapRestorerManager;
import pl.plajer.villagedefense.arena.managers.ScoreboardManager;
import pl.plajer.villagedefense.arena.managers.ShopManager;
import pl.plajer.villagedefense.arena.managers.ZombieSpawnManager;
import pl.plajer.villagedefense.arena.options.ArenaOption;
import pl.plajer.villagedefense.arena.states.ArenaStateHandler;
import pl.plajer.villagedefense.arena.states.EndingState;
import pl.plajer.villagedefense.arena.states.InGameState;
import pl.plajer.villagedefense.arena.states.RestartingState;
import pl.plajer.villagedefense.arena.states.StartingState;
import pl.plajer.villagedefense.arena.states.WaitingState;
import pl.plajer.villagedefense.handlers.language.Messages;
import pl.plajer.villagedefense.user.User;
import pl.plajer.villagedefense.utils.Debugger;

/**
 * Created by Tom on 12/08/2014.
 */
public abstract class Arena extends BukkitRunnable {

  private static final Random random = new Random();
  private static Main plugin;
  private final String id;

  private Set<Player> players = new HashSet<>();
  private List<Zombie> zombies = new ArrayList<>();
  private List<Wolf> wolves = new ArrayList<>();
  private List<Villager> villagers = new ArrayList<>();
  private List<IronGolem> ironGolems = new ArrayList<>();
  private List<Item> droppedFleshes = new ArrayList<>();

  //all arena values that are integers, contains constant and floating values
  private Map<ArenaOption, Integer> arenaOptions = new EnumMap<>(ArenaOption.class);
  //instead of 3 location fields we use map with GameLocation enum
  private Map<GameLocation, Location> gameLocations = new EnumMap<>(GameLocation.class);
  private Map<SpawnPoint, List<Location>> spawnPoints = new EnumMap<>(SpawnPoint.class);
  //all handlers for all game states, we don't include them all in one runnable because it would be too big
  private Map<ArenaState, ArenaStateHandler> gameStateHandlers = new EnumMap<>(ArenaState.class);

  private ScoreboardManager scoreboardManager;
  private MapRestorerManager mapRestorerManager;
  private ShopManager shopManager;
  private ZombieSpawnManager zombieSpawnManager;

  private ArenaState arenaState = ArenaState.WAITING_FOR_PLAYERS;
  private BossBar gameBar;
  private String mapName = "";
  private boolean fighting = false;
  private boolean forceStart = false;
  private boolean ready = true;

  @TestOnly
  protected Arena(String id, String mapName) {
    this.id = id;
    this.mapName = mapName;
    gameStateHandlers.put(ArenaState.WAITING_FOR_PLAYERS, new WaitingState());
    gameStateHandlers.put(ArenaState.STARTING, new StartingState());
    gameStateHandlers.put(ArenaState.IN_GAME, new InGameState());
    gameStateHandlers.put(ArenaState.ENDING, new EndingState());
    gameStateHandlers.put(ArenaState.RESTARTING, new RestartingState());
    for (ArenaStateHandler handler : gameStateHandlers.values()) {
      handler.init(plugin);
    }
  }

  public Arena(String id) {
    this.id = id;
    gameBar = Bukkit.createBossBar(plugin.getChatManager().colorMessage(Messages.BOSSBAR_MAIN_TITLE), BarColor.BLUE, BarStyle.SOLID);
    shopManager = new ShopManager(this);
    zombieSpawnManager = new ZombieSpawnManager(this);
    scoreboardManager = new ScoreboardManager(this);
    mapRestorerManager = new MapRestorerManager(this);
    setDefaultValues();
    gameStateHandlers.put(ArenaState.WAITING_FOR_PLAYERS, new WaitingState());
    gameStateHandlers.put(ArenaState.STARTING, new StartingState());
    gameStateHandlers.put(ArenaState.IN_GAME, new InGameState());
    gameStateHandlers.put(ArenaState.ENDING, new EndingState());
    gameStateHandlers.put(ArenaState.RESTARTING, new RestartingState());
    for (ArenaStateHandler handler : gameStateHandlers.values()) {
      handler.init(plugin);
    }
  }

  public static void init(Main plugin) {
    Arena.plugin = plugin;
  }

  private void setDefaultValues() {
    for (ArenaOption option : ArenaOption.values()) {
      arenaOptions.put(option, option.getDefaultValue());
    }
    for (GameLocation location : GameLocation.values()) {
      gameLocations.put(location, Bukkit.getWorlds().get(0).getSpawnLocation());
    }
    for (SpawnPoint point : SpawnPoint.values()) {
      spawnPoints.put(point, new ArrayList<>());
    }
  }

  public boolean isReady() {
    return ready;
  }

  public void setReady(boolean ready) {
    this.ready = ready;
  }

  public ShopManager getShopManager() {
    return shopManager;
  }

  public ZombieSpawnManager getZombieSpawnManager() {
    return zombieSpawnManager;
  }

  /**
   * Executes boss bar action for arena
   *
   * @param action add or remove a player from boss bar
   * @param p      player
   */
  public void doBarAction(BarAction action, Player p) {
    if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED)) {
      return;
    }
    switch (action) {
      case ADD:
        gameBar.addPlayer(p);
        break;
      case REMOVE:
        gameBar.removePlayer(p);
        break;
      default:
        break;
    }
  }

  @Override
  public void run() {
    //idle task
    if (getPlayers().isEmpty() && getArenaState() == ArenaState.WAITING_FOR_PLAYERS) {
      return;
    }
    Debugger.performance("ArenaTask", "[PerformanceMonitor] [{0}] Running game task", getId());
    long start = System.currentTimeMillis();

    gameStateHandlers.get(getArenaState()).handleCall(this);
    Debugger.performance("ArenaTask", "[PerformanceMonitor] [{0}] Game task finished took {1}ms", getId(), System.currentTimeMillis() - start);
  }

  public void spawnVillagers() {
    if (getVillagers().size() > 10) {
      return;
    }
    if (getVillagerSpawns().isEmpty()) {
      Debugger.debug(Level.WARNING, "No villager spawns set for {0} game won't start", id);
      return;
    }
    for (Location location : getVillagerSpawns()) {
      spawnVillager(location);
    }
    if (getVillagers().isEmpty()) {
      Debugger.debug(Level.WARNING, "Spawning villagers for {0} failed! Are villager spawns set in safe and valid locations?", id);
      return;
    }
    spawnVillagers();
  }

  public boolean isForceStart() {
    return forceStart;
  }

  public void setForceStart(boolean forceStart) {
    this.forceStart = forceStart;
  }

  public boolean isFighting() {
    return fighting;
  }

  public void setFighting(boolean fighting) {
    this.fighting = fighting;
  }

  public Random getRandom() {
    return random;
  }

  /**
   * Returns boss bar of the game.
   * Please use doBarAction if possible
   *
   * @return game boss bar
   * @see Arena#doBarAction(BarAction, Player)
   */
  public BossBar getGameBar() {
    return gameBar;
  }

  /**
   * Get arena identifier used to get arenas by string.
   *
   * @return arena name
   * @see ArenaRegistry#getArena(String)
   */
  public String getId() {
    return id;
  }

  public MapRestorerManager getMapRestorerManager() {
    return mapRestorerManager;
  }

  public int getMinimumPlayers() {
    return getOption(ArenaOption.MINIMUM_PLAYERS);
  }

  public void setMinimumPlayers(int minimumPlayers) {
    setOptionValue(ArenaOption.MINIMUM_PLAYERS, minimumPlayers);
  }

  /**
   * Get arena map name.
   *
   * @return arena map name, <b>it's not arena id</b>
   * @see #getId()
   */
  public String getMapName() {
    return mapName;
  }

  /**
   * Set arena map name.
   *
   * @param mapname new map name, [b]it's not arena id[/b]
   */
  public void setMapName(String mapname) {
    this.mapName = mapname;
  }

  /**
   * Get timer of arena.
   *
   * @return timer of lobby time / time to next wave
   */
  public int getTimer() {
    return getOption(ArenaOption.TIMER);
  }

  /**
   * Modify game timer.
   *
   * @param timer timer of lobby / time to next wave
   */
  public void setTimer(int timer) {
    setOptionValue(ArenaOption.TIMER, timer);
  }

  public int getMaximumPlayers() {
    return getOption(ArenaOption.MAXIMUM_PLAYERS);
  }

  public void setMaximumPlayers(int maximumPlayers) {
    setOptionValue(ArenaOption.MAXIMUM_PLAYERS, maximumPlayers);
  }

  public ArenaState getArenaState() {
    return arenaState;
  }

  /**
   * Set game state of arena.
   * Calls VillageGameStateChangeEvent
   *
   * @param arenaState new game state of arena
   * @see ArenaState
   * @see VillageGameStateChangeEvent
   */
  public void setArenaState(ArenaState arenaState) {
    this.arenaState = arenaState;
    Bukkit.getPluginManager().callEvent(new VillageGameStateChangeEvent(this, getArenaState()));
  }

  public Set<Player> getPlayers() {
    return players;
  }

  public Location getLobbyLocation() {
    return gameLocations.get(GameLocation.LOBBY);
  }

  public void setLobbyLocation(Location loc) {
    gameLocations.put(GameLocation.LOBBY, loc);
  }

  public Location getStartLocation() {
    return gameLocations.get(GameLocation.START);
  }

  public void setStartLocation(Location location) {
    gameLocations.put(GameLocation.START, location);
  }

  public void teleportToEndLocation(Player player) {
    player.teleport(getEndLocation());
  }

  public Location getEndLocation() {
    return gameLocations.get(GameLocation.END);
  }

  public void setEndLocation(Location endLoc) {
    gameLocations.put(GameLocation.END, endLoc);
  }

  public void start() {
    Debugger.debug(Level.INFO, "[{0}] Instance started", this.getId());
    this.runTaskTimer(plugin, 20L, 20L);
    this.setArenaState(ArenaState.WAITING_FOR_PLAYERS);
  }

  public ScoreboardManager getScoreboardManager() {
    return scoreboardManager;
  }

  /**
   * Get list of already spawned zombies.
   * This will only return alive zombies not total zombies in current wave.
   *
   * @return list of spawned zombies in arena
   */
  public List<Zombie> getZombies() {
    return zombies;
  }

  public void removeZombie(Zombie zombie) {
    zombies.remove(zombie);
  }

  public List<Location> getVillagerSpawns() {
    return spawnPoints.get(SpawnPoint.VILLAGER);
  }

  public void addVillagerSpawn(Location location) {
    spawnPoints.get(SpawnPoint.VILLAGER).add(location);
  }

  public void addZombieSpawn(Location location) {
    spawnPoints.get(SpawnPoint.ZOMBIE).add(location);
  }

  public List<Item> getDroppedFleshes() {
    return droppedFleshes;
  }

  public void addDroppedFlesh(Item item) {
    droppedFleshes.add(item);
  }

  public void removeDroppedFlesh(Item item) {
    droppedFleshes.remove(item);
  }

  public int getZombiesLeft() {
    return getOption(ArenaOption.ZOMBIES_TO_SPAWN) + getZombies().size();
  }

  public int getWave() {
    return getOption(ArenaOption.WAVE);
  }

  /**
   * Should be used with endWave.
   *
   * @param wave new game wave
   * @see ArenaManager#endWave(Arena)
   */
  public void setWave(int wave) {
    setOptionValue(ArenaOption.WAVE, wave);
  }

  public abstract void spawnVillager(Location location);

  public abstract void spawnWolf(Location location, Player player);

  public abstract void spawnGolem(Location location, Player player);

  public abstract void spawnFastZombie(Random random);

  public abstract void spawnBabyZombie(Random random);

  public abstract void spawnHardZombie(Random random);

  public abstract void spawnPlayerBuster(Random random);

  public abstract void spawnGolemBuster(Random random);

  public abstract void spawnSoftHardZombie(Random random);

  public abstract void spawnHalfInvisibleZombie(Random random);

  public abstract void spawnKnockbackResistantZombies(Random random);

  public abstract void spawnVillagerSlayer(Random random);

  protected void addWolf(Wolf wolf) {
    wolves.add(wolf);
  }

  /**
   * Get alive wolves.
   *
   * @return alive wolves in game
   */
  public List<Wolf> getWolves() {
    return wolves;
  }

  /**
   * Get alive iron golems.
   *
   * @return alive iron golems in game
   */
  public List<IronGolem> getIronGolems() {
    return ironGolems;
  }

  /**
   * Get alive villagers.
   *
   * @return alive villagers in game
   */
  public List<Villager> getVillagers() {
    return villagers;
  }

  public boolean checkLevelUpRottenFlesh() {
    if (getOption(ArenaOption.ROTTEN_FLESH_LEVEL) == 0 && getOption(ArenaOption.ROTTEN_FLESH_AMOUNT) > 50) {
      setOptionValue(ArenaOption.ROTTEN_FLESH_LEVEL, 1);
      return true;
    }
    if (getOption(ArenaOption.ROTTEN_FLESH_LEVEL) * 10 * getPlayers().size() + 10 < getOption(ArenaOption.ROTTEN_FLESH_AMOUNT)) {
      addOptionValue(ArenaOption.ROTTEN_FLESH_LEVEL, 1);
      return true;
    }
    return false;
  }

  public List<Player> getPlayersLeft() {
    List<Player> playersLeft = new ArrayList<>();
    for (User user : plugin.getUserManager().getUsers(this)) {
      if (!user.isSpectator()) {
        playersLeft.add(user.getPlayer());
      }
    }
    return playersLeft;
  }

  public Main getPlugin() {
    return plugin;
  }

  protected void addZombie(Zombie zombie) {
    zombies.add(zombie);
  }

  protected void addVillager(Villager villager) {
    villagers.add(villager);
  }

  public void removeVillager(Villager villager) {
    villager.remove();
    villager.setHealth(0);
    villagers.remove(villager);
  }

  public List<Location> getZombieSpawns() {
    return spawnPoints.get(SpawnPoint.ZOMBIE);
  }

  protected void addIronGolem(IronGolem ironGolem) {
    ironGolems.add(ironGolem);
  }

  public void resetOptionValues() {
    setOptionValue(ArenaOption.WAVE, 1);
    setOptionValue(ArenaOption.TOTAL_KILLED_ZOMBIES, 0);
    setOptionValue(ArenaOption.TOTAL_ORBS_SPENT, 0);
    setOptionValue(ArenaOption.ZOMBIE_DIFFICULTY_MULTIPLIER, 0);
    setOptionValue(ArenaOption.ZOMBIE_IDLE_PROCESS, 0);
    zombieSpawnManager.applyIdle(0);
  }

  public int getOption(ArenaOption option) {
    return arenaOptions.get(option);
  }

  public void setOptionValue(ArenaOption option, int value) {
    arenaOptions.put(option, value);
  }

  public void addOptionValue(ArenaOption option, int value) {
    arenaOptions.put(option, arenaOptions.get(option) + value);
  }

  public enum BarAction {
    ADD, REMOVE
  }

  public enum GameLocation {
    START, LOBBY, END
  }

  public enum SpawnPoint {
    ZOMBIE, VILLAGER
  }

}
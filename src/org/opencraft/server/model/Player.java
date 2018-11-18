/*
 * Jacob_'s Capture the Flag for Minecraft Classic and ClassiCube
 * Copyright (c) 2010-2014 Jacob Morgan
 * Based on OpenCraft v0.2
 *
 * OpenCraft License
 * 
 * Copyright (c) 2009 Graham Edgecombe, S�ren Enevoldsen and Brett Russell.
 * All rights reserved.
 *
 * Distribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Distributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *       
 *     * Distributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *       
 *     * Neither the name of the OpenCraft nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.opencraft.server.model;


import org.opencraft.server.Constants;
import org.opencraft.server.Server;
import org.opencraft.server.game.impl.CTFGameMode;
import org.opencraft.server.game.impl.GameSettings;
import org.opencraft.server.net.ActionSender;
import org.opencraft.server.net.MinecraftSession;
import org.opencraft.server.net.PingList;
import org.opencraft.server.persistence.LoadPersistenceRequest;
import org.opencraft.server.persistence.SavePersistenceRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

/**
 * Represents a connected player.
 *
 * @author Graham Edgecombe
 */
public class Player extends Entity {

  public static final int maxFlamethrowerUnits = 100;
  public static short NAME_ID = 0;
  //public Mine mine = null;
  public final LinkedList<Mine> mines = new LinkedList<Mine>();
  /**
   * The player's session.
   */
  private final MinecraftSession session;
  /**
   * The player's name.
   */
  private final String name;
  /**
   * A map of attributes that can be attached to this player.
   */
  private final Map<String, Object> attributes = new HashMap<String, Object>();
  
  // There is literally no organization for these attributes, have fun!
  public short nameId;
  public boolean isNewPlayer = false;
  public boolean appendingChat = false;
  public String partialChatMessage = "";
  public String lastMessage;
  public long lastMessageTime;
  public long lastPacketTime;
  public int heldBlock = 0;
  public boolean joinedDuringTournamentMode;
  public boolean muted = false;
  public boolean frozen = false;
  public long moveTime = 0;
  public int team = -1;
  public int killstreak = 0;
  private long safeTime = 0;
  public boolean hasTNT = false;
  public int tntX;
  public int tntY;
  public int tntZ;
  public int tntRadius = 2;
  public boolean hasFlag = false;
  public int outOfBoundsBlockChanges = 0;
  public int placeBlock = -1;
  public boolean placeSolid = false;
  public boolean moveFlag = false;
  public boolean isVisible = true;
  public boolean setPayloadPath = false;
  public ArrayList<Position> payloadPathPositions = new ArrayList<>();
  public boolean brush = false;
  public boolean hasVoted = false;
  public boolean hasNominated = false;
  //STORE STUFF
  public int bigTNTRemaining = 0;
  public boolean hasGhost = false;
  public boolean hasShield = false;
  public boolean shield = false;
  public long lastBlockTimestamp;
  public int boxStartX = -1;
  public int boxStartY = -1;
  public int boxStartZ = -1;
  public int teleX1 = -1;
  public int teleY1 = -1;
  public int teleZ1 = -1;
  public int teleX2 = -1;
  public int teleY2 = -1;
  public int teleZ2 = -1;
  public int buildMode;
  public Position linePosition;
  public Rotation lineRotation;
  public boolean flamethrowerEnabled = false;
  public long flamethrowerTime = 0;
  public long flamethrowerNotify = 0;
  public float flamethrowerFuel = Constants.FLAME_THROWER_FUEL;
  public long rocketTime;
  public int headBlockType = 0;
  public Position headBlockPosition = null;
  public int accumulatedStorePoints = 0;
  public Player duelChallengedBy = null;
  public Player duelPlayer = null;
  public int duelKills = 0;
  public int bountySet = 0;
  public Player bountied = null;
  public Player bountiedBy = null;
  public int bountyKills = 0;
  public int bountyAmount = 0;
  public boolean bountyMode = false;
  public int lastAmount = 0;
  public boolean bountyActive = false;
  private HashSet<String> ignorePlayers = new HashSet<String>();
  private ActionSender actionSender = null;
  private Player instance;
  private Thread followThread;
  public ChatMode chatMode = ChatMode.DEFAULT;
  public Player chatPlayer;
  public boolean sendCommandLog = false;
  public final PingList pingList = new PingList();

  public Player(MinecraftSession session, String name) {
    this.session = session;
    this.name = name;
    instance = this;
    nameId = NAME_ID;
    NAME_ID++;
    if (NAME_ID == 256) {
      NAME_ID = 0;
    }
  }

  public static Position getSpawnPos() {
    Level l = World.getWorld().getLevel();
    boolean done = false;
    int x = 0;
    int z = l.ceiling - 4;
    int y = 0;
    for (int i = 0; i < 1000; i++) {
      x = (int) (Math.random() * l.getWidth());
      y = (int) (Math.random() * l.getDepth());
      int block = 0;

      for (z = l.ceiling - 4; z > 0; z--) {
        block = l.getBlock(x, y, z);
        if (block != 0)
          break;
      }
      if (block != 0) {
        done = true;
        z += 2;
        break;
      }
    }
    return new Position(x * 32 + 16, y * 32 + 16, z * 32 + 16);
  }

  public static Player getPlayer(String name, ActionSender source) {
    Player player = null;
    for (Player p : World.getWorld().getPlayerList().getPlayers()) {
      if (p.getName().toLowerCase().contains(name.toLowerCase())) {
        if (player == null)
          player = p;
        else {
          player = null;
          if (source != null)
            source.sendChatMessage("- &e\"" + name + "\" matches multiple players.");
          break;
        }
      }
    }
    return player;
  }

  public static Player setAttributeFor(String name, String k, String v, ActionSender source) {
    Player player = getPlayer(name, source);
    if (player != null) {
      player.setAttribute(k, v);
      return player;
    } else {
      Player p = new Player(null, name);
      try {
        new LoadPersistenceRequest(p).perform();
        p.setAttribute(k, v);
        new SavePersistenceRequest(p).perform();
      } catch (Exception e) {
        source.sendChatMessage("- &eError setting attribute: " + e.toString());
        Server.log(e);
      }
      return null;
    }
  }

  public static String getAttributeFor(String name, String k, ActionSender source) {
    Player player = getPlayer(name, source);
    if (player != null) {
      if (player.getAttribute(k) == null)
        return null;
      else
        return player.getAttribute(k).toString();
    } else {
      Player p = new Player(null, name);
      try {
        new LoadPersistenceRequest(p).perform();
        if (p.getAttribute(k) == null)
          return null;
        else
          return p.getAttribute(k).toString();
      } catch (Exception e) {
        source.sendChatMessage("- &eError getting attribute: " + e.toString());
        Server.log(e);
      }
      return null;
    }
  }

  public void removeMine(Mine m) {
    synchronized (mines) {
      mines.remove(m);
    }
  }

  public void clearMines() {
    synchronized (mines) {
      for (Mine m : mines) {
        World.getWorld().removeMine(m);
        World.getWorld().getLevel().setBlock((m.x - 16) / 32, (m.y - 16) / 32, (m.z - 16) / 32, 0);
      }
      mines.clear();
    }
  }

  public void ignore(Player p) {
    if (p.isOp()) {
      getActionSender().sendChatMessage("- &eYou can't ignore operators.");
    } else {
      String name = p.name;
      if (!ignorePlayers.contains(name)) {
        ignorePlayers.add(name);
        getActionSender().sendChatMessage("- &eNow ignoring " + p.parseName() + ". Use this " +
            "command again to stop");
      } else {
        ignorePlayers.remove(name);
        getActionSender().sendChatMessage("- &eNo longer ignoring " + p.getColoredName());
      }
    }
  }

  public boolean isIgnored(Player p) {
    return ignorePlayers.contains(p.name);
  }

  public void sendFuelPercent() {
    getActionSender().sendChatMessage("- &eFuel: &c" +
            Math.round(flamethrowerFuel / Constants.FLAME_THROWER_FUEL * 100) + "%");
    flamethrowerNotify = System.currentTimeMillis();
  }

  public void gotKill(Player defender) {
    if (defender.team == -1 || defender.team == team)
      return;
    else if (World.getWorld().getGameMode().getMode() == Level.TDM) {
      if (team == 0)
        World.getWorld().getGameMode().redCaptures++;
      else
        World.getWorld().getGameMode().blueCaptures++;
      World.getWorld().getGameMode().updateStatusMessage();
    }

    killstreak++;
    Killstats.kill(this, defender);
    if (killstreak % 5 == 0)
      World.getWorld().broadcast("- " + getColoredName() + " &bhas a killstreak of " + killstreak
          + "!");
    setIfMax("maxKillstreak", killstreak);
    if (duelPlayer == defender) {
      duelKills++;
      if (duelKills == 3) {
        World.getWorld().broadcast("- " + getColoredName() + " &bhas defeated " + duelPlayer
            .getColoredName() + " &bin a duel!");
        incStat("duelWins");
        duelPlayer.incStat("duelLosses");

        duelChallengedBy = null;

        duelPlayer.duelChallengedBy = null;

        duelPlayer.duelPlayer = null;
        duelPlayer = null;

        sendToTeamSpawn();
      }
    }
    KillLog.getInstance().logKill(this, defender);
  }

  public void follow(final Player p) {
    if (p == null && followThread != null)
      followThread.stop();
    else {
      if (followThread != null)
        followThread.stop();
      followThread = new Thread(new Runnable() {
        public void run() {
          while (true) {
            Position pos = p.getPosition();
            Rotation r = p.getRotation();
            try {
              Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
            getActionSender().sendTeleport(pos, r);
          }
        }
      });
      followThread.start();
    }
  }

  public void died(Player attacker) {
    if (killstreak >= 10)
      World.getWorld().broadcast("- " + attacker.getColoredName() + " &bended " + getColoredName
          () + "&b's killstreak of " + killstreak);
    killstreak = 0;
    attacker.setIfMax("maxKillstreakEnded", killstreak);
    incStat("deaths");
    World.getWorld().getGameMode().checkForUnbalance(this);
    if (this.bountyMode == true) {
      if (this.team == -1) {
        this.bountiedBy.addStorePoints(this.bountyAmount);
        this.bountied = null;
        this.bountiedBy = null;
        this.bountyMode = false;
      } else {
        if (this == attacker) {
          //nothing
        } else {
          if (attacker == this.bountiedBy) {
            // nothing
          } else {
            if (this.bountied == this) {
              attacker.bountyKills++;
              if (attacker.bountyKills == attacker.lastAmount + 5) {
                World.getWorld().broadcast("- " + attacker.getColoredName() + " &bhas collected " +
                    "the bounty of " + this.bountyAmount + "on " + this.getColoredName() + "!");
                attacker.addStorePoints(this.bountyAmount);
                this.bountied = null;
                this.bountiedBy = null;
                this.bountyMode = false;
                this.bountyAmount = 0;
                attacker.lastAmount = attacker.bountyKills;
              }
            }
          }
        }
      }
    }
  }

  public String getNameChar() {
    if (isOp()) {
      if (team == 0)
        return "&4";
      else if (team == 1)
        return "&1";
      else
        return "&8";
    } else {
      if (team == 0)
        return "&c";
      else if (team == 1)
        return "&9";
      else
        return "&7";
    }

  }

  public boolean isVIP() {
    return (!GameSettings.getBoolean("Tournament")
        && getAttribute("VIP") != null && getAttribute("VIP").equals("true")) || isOp();
  }

  public boolean isOp() {
    return (getAttribute("IsOperator") != null && getAttribute("IsOperator").equals("true"));
  }

  public boolean isOwner() {
    return name.equals("Jacob_");
  }

  public String parseName() {
    return getNameChar() + name + "&e";
  }

  public void makeInvisible() {
    for (Player p : World.getWorld().getPlayerList().getPlayers()) {
      p.getActionSender().sendRemovePlayer(instance);
    }
  }

  public void makeVisible() {
    for (Player p : World.getWorld().getPlayerList().getPlayers()) {
      p.getActionSender().sendAddPlayer(instance, p == instance);
    }
  }

  public void activateGhost() {
    new Thread(new Runnable() {
      public void run() {
        makeInvisible();
        isVisible = false;
        hasGhost = false;
        try {
          getActionSender().sendChatMessage("- &eYou activated ghost mode!");
          Thread.sleep(10 * 1000);
          getActionSender().sendChatMessage("- &eGhost mode expires in 5 seconds!");
          Thread.sleep(5 * 1000);
        } catch (InterruptedException ex) {

        }
        makeVisible();
        getActionSender().sendChatMessage("- &eYou are no longer a ghost!");
        isVisible = true;
      }
    }).start();
  }

  public void autoJoinTeam() {
    CTFGameMode ctf = (CTFGameMode) World.getWorld().getGameMode();
    String team;
    if (ctf.redPlayers > ctf.bluePlayers)
      team = "blue";
    else if (ctf.bluePlayers > ctf.redPlayers)
      team = "red";
    else {
      if (Math.random() < 0.5)
        team = "red";
      else
        team = "blue";
    }
    joinTeam(team);
  }

  public void joinTeam(String team) {
    joinTeam(team, true);
  }

  public void joinTeam(String team, boolean sendMessage) {
    if (this.team == -1 && !team.equals("spec")) {
      getActionSender().sendChatMessage("- &aThis map was contributed by: " + World.getWorld()
          .getLevel().getCreator());
    }
    if (!isVisible && !team.equals("spec")) {
      Server.log(getName() + " is now unhidden");
      makeVisible();
      getActionSender().sendChatMessage("- &eYou are now visible");
      isVisible = true;
    }
    Level l = World.getWorld().getLevel();
    CTFGameMode ctf = (CTFGameMode) World.getWorld().getGameMode();
    ctf.updateLeaderboard();
    if (ctf.voting == true)
      return;
    if (this.team == 0)
      ctf.redPlayers--;
    else if (this.team == 1)
      ctf.bluePlayers--;
    int diff = ctf.redPlayers - ctf.bluePlayers;
    boolean unbalanced = false;
    if (!GameSettings.getBoolean("Tournament")) {
      if (diff >= 1 && team.equals("red"))
        unbalanced = true;
      else if (diff <= -1 && team.equals("blue"))
        unbalanced = true;
    }
    for (Player p : World.getWorld().getPlayerList().getPlayers()) {
      if (p != this) {
        p.getActionSender().sendRemovePlayer(this);
      }
    }
    boolean bad = false;
    if (hasFlag) {
      if (this.team == 0) {
        ctf.blueFlagTaken = false;
        ctf.placeBlueFlag();
      } else {
        ctf.redFlagTaken = false;
        ctf.placeRedFlag();
      }
      hasFlag = false;
      World.getWorld().broadcast("- " + parseName() + " dropped the flag!");
    }
    if (team.equals("red")) {
      if (unbalanced && ctf.redPlayers > ctf.bluePlayers) {
        ctf.bluePlayers++;
        this.team = 1;
        team = "blue";
        getActionSender().sendChatMessage("- Red team is full.");
      } else {
        ctf.redPlayers++;
        this.team = 0;
      }
    } else if (team.equals("blue")) {
      if (unbalanced && ctf.bluePlayers > ctf.redPlayers) {
        ctf.redPlayers++;
        this.team = 0;
        team = "red";
        this.getActionSender().sendChatMessage("- Blue team is full.");
      } else {
        ctf.bluePlayers++;
        this.team = 1;
      }
    } else if (team.equals("spec")) {
      this.team = -1;
      if (duelPlayer != null) {
        duelPlayer.duelPlayer = null;
        duelPlayer = null;
      }
    } else {
      bad = true;
      getActionSender().sendChatMessage("- Unrecognized team!");
    }
    clearMines();
    if (isVisible) {
      for (Player p : World.getWorld().getPlayerList().getPlayers()) {
        p.getActionSender().sendAddPlayer(this, p == this);
      }
    }
    if (!bad) {
      if (sendMessage)
        World.getWorld().broadcast("- " + parseName() + " joined the " + team + " team");
      Position spawn = l.getTeamSpawn(team);
      getActionSender().sendTeleport(spawn, new Rotation(this.team == 0 ? 64 : 192, 0));
      setPosition(spawn);
      session.getActionSender().sendHackControl(this.team == -1);
    }
    if (isNewPlayer) {
      setAttribute("rules", "true");
      isNewPlayer = false;
    }
  }

  public void sendToOtherPlayers() {
    if (isVisible) {
      for (Player p : World.getWorld().getPlayerList().getPlayers()) {
        p.getActionSender().sendAddPlayer(this, p == this);
      }
    }
  }

  public void setInt(String a, int value) {
    setAttribute(a, (Integer) value);
  }

  public void setIfMax(String a, int value) {
    if (value > getInt(a)) {
      setInt(a, value);
    }
  }

  public int getInt(String a) {
    return (Integer) getAttribute(a);
  }

  public void incStat(String a) {
    if (getAttribute(a) == null) {
      setAttribute(a, 0);
    }
    setAttribute(a, (Integer) getAttribute(a) + 1);
  }

  public void addStorePoints(int n) {
    if (getAttribute("storepoints") == null) {
      setAttribute("storepoints", 0);
    }
    accumulatedStorePoints += n;
    setAttribute("storepoints", (Integer) getAttribute("storepoints") + n);
    World.getWorld().getGameMode().updateLeaderboard();
  }

  public int getStorePoints() {
    return (Integer) getAttribute("storepoints");
  }

  public void setStorePoints(int n) {
    if (getAttribute("storepoints") == null) {
      setAttribute("storepoints", 0);
    }
    setAttribute("storepoints", n);
  }

  public void subtractStorePoints(int n) {
    if (getAttribute("storepoints") == null) {
      setAttribute("storepoints", 0);
    }
    setAttribute("storepoints", (Integer) getAttribute("storepoints") - n);
  }

  public void kickForHacking() {
    getActionSender().sendLoginFailure("You were kicked for hacking!");
    getSession().close();
    World.getWorld().broadcast("- &e" + getName() + " was kicked for hacking!");
  }

  public void sendToTeamSpawn(final boolean delay) {
    final String teamname;
    if (team == 0)
      teamname = "red";
    else if (team == 1)
      teamname = "blue";
    else
      teamname = "spec";
            /*new Thread(new Runnable(){
                public void run() {
                    safe = true;
                    if(delay) {
                        getActionSender().sendTeleport(World.getWorld().getLevel()
                        .getSpawnPosition(), new Rotation(0, 0));
                        getActionSender().sendChatMessage("- &bRespawning in 5 seconds...");
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ex) {
                        }
                    }
                    getActionSender().sendTeleport(World.getWorld().getLevel().getTeamSpawn
                    (teamname), new Rotation(team == 0 ? 64 : 192, 0));
                    new Thread(new PlayerUntagger(Player.this)).start();
                }
            }).start();*/
    getActionSender().sendTeleport(World.getWorld().getLevel().getTeamSpawn(teamname), new
        Rotation(team == 0 ? 64 : 192, 0));
    System.out.println(getName() + " is respawning");
  }

  public void sendToTeamSpawn() {
    sendToTeamSpawn(true);
  }

  /**
   * Sets an attribute of this player.
   *
   * @param name  The name of the attribute.
   * @param value The value of the attribute.
   * @return The old value of the attribute, or <code>null</code> if there was no previous attribute
   * with that name.
   */
  public Object setAttribute(String name, Object value) {
    return attributes.put(name, value);
  }

  /**
   * Gets an attribute.
   *
   * @param name The name of the attribute.
   * @return The attribute, or <code>null</code> if there is not an attribute with that name.
   */
  public Object getAttribute(String name) {
    return attributes.get(name);
  }

  /**
   * Checks if an attribute is set.
   *
   * @param name The name of the attribute.
   * @return <code>true</code> if set, <code>false</code> if not.
   */
  public boolean isAttributeSet(String name) {
    return attributes.containsKey(name);
  }

  /**
   * Removes an attribute.
   *
   * @param name The name of the attribute.
   * @return The old value of the attribute, or <code>null</code> if an attribute with that name did
   * not exist.
   */
  public Object removeAttribute(String name) {
    return attributes.remove(name);
  }

  @Override
  public String getName() {
    return name;
  }

  public String getColoredName() {
    return getNameChar() + name;
  }

  public String getTeamName() {
    if (team == 0) {
      return "&cRed";
    } else if (team == 1) {
      return "&9Blue";
    } else {
      return "&7Spectators";
    }
  }

  /**
   * Gets the player's session.
   *
   * @return The session.
   */
  public MinecraftSession getSession() {
    return session;
  }

  /**
   * Gets this player's action sender.
   *
   * @return The action sender.
   */
  public ActionSender getActionSender() {
    if (session != null)
      return session.getActionSender();
    else
      return actionSender;
  }

  public void setActionSender(ActionSender actionSender) {
    this.actionSender = actionSender;
  }

  /**
   * Gets the attributes map.
   *
   * @return The attributes map.
   */
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public boolean canKill(Player p, boolean sendMessage) {
    if (duelPlayer != null && p != duelPlayer) {
      if (sendMessage)
        getActionSender().sendChatMessage("- &eYou can't kill " + p.parseName() + " since you are" +
            " dueling " + duelPlayer.parseName() + ". Only they can hurt you right now.");
      return false;
    } else if (duelPlayer == null && p.duelPlayer != null) {
      if (sendMessage)
        getActionSender().sendChatMessage("- &eYou can't kill " + p.parseName() + " since they " +
            "are dueling " + p.duelPlayer.parseName() + ". They can't capture your flag or kill " +
            "anyone else right now.");
      return false;
    } else if(p.team == -1) {
      if (sendMessage) {
        getActionSender().sendChatMessage("- &eYou can't kill " + p.parseName() + " since they " +
            "are spectating.");
      }
      return false;
    } else {
      return true;
    }
  }

  public void markSafe() {
    safeTime = System.currentTimeMillis();
  }

  public boolean isSafe() {
    return System.currentTimeMillis() - safeTime < Constants.SAFE_TIME;
  }
}
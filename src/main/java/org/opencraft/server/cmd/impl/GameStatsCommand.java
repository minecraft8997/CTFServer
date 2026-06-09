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
package org.opencraft.server.cmd.impl;

import org.opencraft.server.cmd.Command;
import org.opencraft.server.cmd.CommandParameters;
import org.opencraft.server.model.Player;
import tf.jacobsc.utils.RatingKt;
import tf.jacobsc.utils.RatingType;

public class GameStatsCommand implements Command {
  private static final GameStatsCommand INSTANCE = new GameStatsCommand();

  /**
   * Gets the singleton instance of this command.
   *
   * @return The singleton instance of this command.
   */
  public static GameStatsCommand getCommand() {
    return INSTANCE;
  }

  public void execute(Player player, CommandParameters params) {
    player.getActionSender().sendChatMessage("- &eDef kills: " + player.defKills + ", mid kills: " + player.midKills + ", atk kills: " + player.atkKills);
    player.getActionSender().sendChatMessage("- &eTotal kills: " + player.kills + " vs Total deaths: " + player.deaths + " (" + player.kills / player.deaths + ")");
    player.getActionSender().sendChatMessage("- &eTNT kills: " + player.tntKills + " vs TNT deaths " + player.tntDeaths + " (" + player.tntKills / player.tntDeaths + ")");
    player.getActionSender().sendChatMessage("- &eTag kills: " + player.tagKills + " vs Tag deaths " + player.tagDeaths + " (" + player.tagKills / player.tagDeaths + ")");
    player.getActionSender().sendChatMessage("- &eMine kills: " + player.mineKills + " vs Mine deaths " + player.mineDeaths + " (" + player.mineKills / player.mineDeaths + ")");
    player.getActionSender().sendChatMessage("- &eGrenades thrown: " + player.grenadesThrown + " -> Grenades kills " + player.grenadeKills + " (" + player.grenadeKills / player.grenadesThrown + "%)");
    player.getActionSender().sendChatMessage("- &eGrenade deaths: " + player.grenadeDeaths + " vs Rocket deaths: " + player.rocketDeaths);
    player.getActionSender().sendChatMessage("- &eRockets shot: " + player.rocketsShot + " -> Rocket kills: " + player.rocketKills + " (" + player.rocketKills / player.rocketsShot + "%)");
    player.getActionSender().sendChatMessage("- &eLines used: " + player.linesUsed);
    player.getActionSender().sendChatMessage("- &ePoints earned: " + player.currentRoundPointsEarned + " -> Points spent" + player.pointsSpent + " (0.8)");
    player.getActionSender().sendChatMessage("- &eFlags taken: " + player.flagsTaken + " -> Flags captured: " + player.captures + " (" + player.captures / player.flagsTaken + ")");
    player.getActionSender().sendChatMessage("- &eHighest kill streak: " + player.highestKillStreak + " vs Highest death streak: " + player.highestDeathStreak);
    //player.getActionSender().sendChatMessage("- &eOverall performance: " + performanceRating);
    player.getActionSender().sendChatMessage("&a* You may need to scroll up to see all stats *");
  }
}

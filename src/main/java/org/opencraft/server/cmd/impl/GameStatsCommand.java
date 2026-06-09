package org.opencraft.server.cmd.impl;

import org.opencraft.server.cmd.Command;
import org.opencraft.server.cmd.CommandParameters;
import org.opencraft.server.model.Player;

public class GameStatsCommand implements Command {

  private static final GameStatsCommand INSTANCE = new GameStatsCommand();

  public static GameStatsCommand getCommand() {
    return INSTANCE;
  }

  @Override
  public void execute(Player player, CommandParameters params) {
    double kd = ratio(player.kills, player.deaths);
    double tntKd = ratio(player.tntKills, player.tntDeaths);
    double tagKd = ratio(player.tagKills, player.tagDeaths);
    double mineKd = ratio(player.mineKills, player.mineDeaths);
    double grenadeAccuracy = percentage(player.grenadeKills, player.grenadesThrown);
    double rocketAccuracy = percentage(player.rocketKills, player.rocketsShot);
    double captures = ratio(player.captures, player.flagsLost);

    player.getActionSender().sendChatMessage(
        "- &eTotal &akills&e: " + player.kills +
            " vs &cdeaths&e: " + player.deaths +
            " (&f" + format(kd) +
            "&e, " + getRatioTier(kd) + "&e)"
    );

    player.getActionSender().sendChatMessage(
        "- &aDef &ekills: " + player.defKills +
            ", &bmid &ekills: " + player.midKills +
            ", &catk &ekills: " + player.atkKills
    );

    player.getActionSender().sendChatMessage(
        "- &eTNT &akills&e: " + player.tntKills +
            " vs &cdeaths&e: " + player.tntDeaths +
            " (&f" + format(tntKd) +
            "&e, " + getRatioTier(tntKd) + "&e)"
    );

    player.getActionSender().sendChatMessage(
        "- &eTag &akills&e: " + player.tagKills +
            " vs &cdeaths&e: " + player.tagDeaths +
            " (&f" + format(tagKd) +
            "&e, " + getRatioTier(tagKd) + "&e)"
    );

    player.getActionSender().sendChatMessage(
        "- &eMine &akills&e: " + player.mineKills +
            " vs &cdeaths&e: " + player.mineDeaths +
            " (&f" + format(mineKd) +
            "&e, " + getRatioTier(mineKd) + "&e)"
    );

    player.getActionSender().sendChatMessage(
        "- &eGrenades thrown: " + player.grenadesThrown +
            " -> &akills&e: " + player.grenadeKills +
            " (&f" + format(grenadeAccuracy) + "%" +
            "&e, " + getPercentTier(grenadeAccuracy) + "&e)"
    );

    player.getActionSender().sendChatMessage(
        "- &eGrenade &cdeaths&e: " + player.grenadeDeaths +
            " vs Rocket &cdeaths&e: " + player.rocketDeaths
    );

    player.getActionSender().sendChatMessage(
        "- &eRockets shot: " + player.rocketsShot +
            " -> &akills&e: " + player.rocketKills +
            " (&f" + format(rocketAccuracy) + "%" +
            "&e, " + getPercentTier(rocketAccuracy) + "&e)"
    );

    player.getActionSender().sendChatMessage(
        "- &eLines used: " + player.linesUsed +
            "&e, Points spent: " + player.pointsSpent +
            " / " + player.currentRoundPointsEarned
    );

    player.getActionSender().sendChatMessage(
        "- &eFlags captured: " + player.captures +
            " vs lost: " + player.flagsLost +
            " (&f" + format(captures) + "%" +
            "&e, " + getRatioTier(captures) + "&e)"
    );

    player.getActionSender().sendChatMessage(
        "- &eHighest kill streak: " + player.highestKillStreak +
            " vs Highest death streak: " + player.highestDeathStreak
    );

    double overallPerformance = calculateOverallPerformance(
        kd,
        captures
    );

    if (overallPerformance < 0) {
      player.getActionSender().sendChatMessage(
          "- &eOverall performance: &7N/A"
      );
    } else {
      player.getActionSender().sendChatMessage(
          "- &eOverall performance: &f" +
              format(overallPerformance) +
              "/100 &e(" +
              getPerformanceTier(overallPerformance) +
              "&e)"
      );
    }

    player.getActionSender().sendChatMessage(
        "&a* You may need to scroll up to see all stats *"
    );
  }

  private static double ratio(int numerator, int denominator) {
    if (numerator == 0 && denominator == 0) {
      return -1.0;
    }
    if (denominator <= 0) {
      return numerator;
    }
    return (double) numerator / denominator;
  }

  private static double percentage(int value, int total) {
    if (value == 0 && total == 0) {
      return -1.0;
    }
    if (total <= 0) {
      return value > 0 ? 100.0 : -1.0;
    }
    return ((double) value / total) * 100.0;
  }

  private static String format(double value) {
    return String.format("%.2f", value);
  }

  private static String getRatioTier(double value) {
    if (value < 0) {
      return "&7N/A";
    }

    if (value >= 1.5) {
      return "&dS";
    } else if (value >= 1.0) {
      return "&aA";
    } else if (value >= 0.75) {
      return "&2B";
    } else if (value >= 0.5) {
      return "&eC";
    } else if (value >= 0.25) {
      return "&6D";
    } else if (value >= 0.125) {
      return "&cE";
    } else {
      return "&4F";
    }
  }

  private static String getPercentTier(double percent) {
    if (percent < 0) {
      return "&7N/A";
    }
    if (percent >= 100) {
      return "&dS";
    } else if (percent >= 80) {
      return "&aA";
    } else if (percent >= 60) {
      return "&2B";
    } else if (percent >= 40) {
      return "&eC";
    } else if (percent >= 20) {
      return "&6D";
    } else if (percent >= 10) {
      return "&cE";
    } else {
      return "&4F";
    }
  }

  private static double calculateOverallPerformance(
      double kd,
      double captureRate
  ) {
    double total = 0.0;
    double weightSum = 0.0;

    if (kd >= 0) {
      total += (Math.min(kd, 2.0) / 2.0 * 100.0) * 0.40;
      weightSum += 0.40;
    }

    if (captureRate >= 0) {
      total += Math.min(captureRate, 100.0) * 0.10;
      weightSum += 0.50;
    }

    if (weightSum == 0) {
      return -1.0;
    }

    return total / weightSum;
  }

  private static String getPerformanceTier(double score) {
    if (score >= 90) {
      return "&dS";
    } else if (score >= 80) {
      return "&aA";
    } else if (score >= 60) {
      return "&2B";
    } else if (score >= 40) {
      return "&eC";
    } else if (score >= 20) {
      return "&6D";
    } else if (score >= 10) {
      return "&cE";
    } else {
      return "&4F";
    }
  }
}
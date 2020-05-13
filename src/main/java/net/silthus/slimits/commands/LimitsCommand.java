package net.silthus.slimits.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import lombok.Getter;
import net.silthus.slimits.LimitsManager;
import net.silthus.slimits.limits.PlayerBlockPlacementLimit;
import net.silthus.slimits.ui.LimitsGUI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@CommandAlias("slimits|limits|limit|lim")
public class LimitsCommand extends BaseCommand {

    @Getter
    private final LimitsManager limitsManager;
    @Getter
    private final LimitsGUI gui;

    public LimitsCommand(LimitsManager limitsManager, LimitsGUI gui) {
        this.limitsManager = limitsManager;
        this.gui = gui;
    }

    @Default
    @Subcommand("list")
    @Description("Lists all of your or another players limits.")
    public void listLimits(Player player) {

        player.sendMessage(ChatColor.BOLD + "" + ChatColor.DARK_PURPLE + "---=== " + ChatColor.YELLOW + "Your Block Placement Limits " + ChatColor.DARK_PURPLE + "===---");

        PlayerBlockPlacementLimit playerLimit = getLimitsManager().getPlayerLimit(player);
        List<String> messages = new ArrayList<>();

        playerLimit.getLimits().forEach((material, limit) -> {
            StringBuilder sb = new StringBuilder();
            messages.add(sb.append(ChatColor.BOLD).append(ChatColor.GREEN).append(material.name()).append(": ")
                    .append(ChatColor.RESET).append(ChatColor.AQUA)
                    .append(playerLimit.getCount(material)).append(ChatColor.GREEN).append("/").append(ChatColor.AQUA).append(limit)
                    .append(ChatColor.YELLOW).append(" blocks placed.")
                    .toString());
        });

        player.sendMessage(messages.toArray(new String[0]));
    }

    @Subcommand("show")
    @Description("Shows your limits inside a chest GUI.")
    public void showLimitsGui(Player player) {

        getGui().showLimits(player);
    }

    @Subcommand("reload")
    @Description("Reloads the plugin fetching updated configs from the disk.")
    @CommandPermission("slimits.admin.reload")
    public void reload() {

        getLimitsManager().reload();
        getCurrentCommandIssuer().sendMessage(ChatColor.YELLOW + "Reloaded all limit configs.");
    }
}

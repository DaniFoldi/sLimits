package net.silthus.slimits.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import com.danifoldi.messagelib.core.MessageBuilder;
import lombok.Getter;
import net.silthus.slimits.LimitsManager;
import net.silthus.slimits.limits.PlayerBlockPlacementLimit;
import net.silthus.slimits.ui.LimitsGUI;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@CommandAlias("slimits|limits|limit|lim")
public class LimitsCommand extends BaseCommand {

    @Getter
    private final LimitsManager limitsManager;
    @Getter
    private final LimitsGUI gui;
    private final MessageBuilder<String> messageBuilder;

    public LimitsCommand(LimitsManager limitsManager, LimitsGUI gui, MessageBuilder<String> messageBuilder) {
        this.limitsManager = limitsManager;
        this.gui = gui;
        this.messageBuilder = messageBuilder;
    }

    @Default
    @Subcommand("list")
    @Description("Lists all of your or another players limits.")
    public void listLimits(Player player) {

        player.sendMessage(messageBuilder.getBase("limits.list").usingTemplate("player", player.getName()).execute());
        player.sendMessage(ChatColor.BOLD + "" + ChatColor.DARK_PURPLE + "---=== " + ChatColor.YELLOW + "Your Block Placement Limits " + ChatColor.DARK_PURPLE + "===---");

        PlayerBlockPlacementLimit playerLimit = getLimitsManager().getPlayerLimit(player);
        List<String> messages = new ArrayList<>();

        playerLimit.getLimits().forEach((material, limit) -> {
            messages.add(messageBuilder.getBase("limits.listEntry")
                    .usingTemplate("materialName", material.name())
                    .usingTemplate("placed", String.valueOf(playerLimit.getCount(material)))
                    .usingTemplate("limit", String.valueOf(limit))
                    .execute());
        });

        player.sendMessage(messages.toArray(new String[0]));
    }

    @Subcommand("loc")
    @Description("Lists all placed blocks of a certain type.")
    public void listLocations(Material material) {

        if (!getCurrentCommandIssuer().isPlayer()) {
            throw new CommandException(messageBuilder.getBase("command.playerOnly").execute());
        }

        getCurrentCommandIssuer().sendMessage(messageBuilder.getBase("limits.locate").usingTemplate("material", material.name()).execute());
        List<String> messages = new ArrayList<>();

        List<Location> locations = getLimitsManager().getPlayerLimit(getCurrentCommandIssuer().getIssuer()).getLocations(material);
        locations.forEach(location -> {
            messages.add(messageBuilder.getBase("limits.locateBlock")
                    .usingTemplate("x", String.valueOf(location.getBlockX()))
                    .usingTemplate("y", String.valueOf(location.getBlockY()))
                    .usingTemplate("z", String.valueOf(location.getBlockZ()))
                    .usingTemplate("world", location.getWorld().getName())
                    .execute());
        });

        ((Player) getCurrentCommandIssuer().getIssuer()).sendMessage(messages.toArray(new String[0]));
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
        getCurrentCommandIssuer().sendMessage(messageBuilder.getBase("command.reload").execute());
    }
}

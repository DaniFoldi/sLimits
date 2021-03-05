package net.silthus.slimits.limits;

import com.danifoldi.messagelib.core.MessageBuilder;
import lombok.Getter;
import net.silthus.slimits.LimitsConfig;
import net.silthus.slimits.LimitsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Optional;
import java.util.UUID;

public class BlockPlacementLimit implements Listener {

    @Getter
    private final LimitsManager limitsManager;
    private final MessageBuilder<String, String> messageBuilder;

    public BlockPlacementLimit(LimitsManager limitsManager, MessageBuilder<String, String> messageBuilder) {
        this.limitsManager = limitsManager;
        this.messageBuilder = messageBuilder;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerPlaceBlock(BlockPlaceEvent event) {

        PlayerBlockPlacementLimit playerLimit = getLimitsManager().getPlayerLimit(event.getPlayer());

        if (!playerLimit.isApplicable(event.getPlayer(), event.getBlock())) return;

        Material blockType = event.getBlock().getType();
        if (playerLimit.hasReachedLimit(blockType)) {
            event.getPlayer().sendMessage(messageBuilder.getBase("event.limitReached")
                    .usingTemplate("limit", String.valueOf(playerLimit.getLimit(blockType).orElse(0)))
                    .usingTemplate("material", messageBuilder.getBase("materialNames." + blockType.name()).execute())
            .execute());
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void afterBlockPlaceEvent(BlockPlaceEvent event) {

        PlayerBlockPlacementLimit playerLimit = getLimitsManager().getPlayerLimit(event.getPlayer());

        if (!playerLimit.isApplicable(event.getPlayer(), event.getBlock())) return;

        playerLimit.getLimit(event.getBlock().getType()).ifPresent(limit -> {
            Block block = event.getBlock();

            Player player = event.getPlayer();
            Material blockType = block.getType();

            int placedBlockAmount = playerLimit.addBlock(block);
            getLimitsManager().savePlayerLimits(player);

            double usage = placedBlockAmount * 100.0 / (limit * 100.0);
            ChatColor color = ChatColor.GREEN;
            if (usage >= 95) {
                color = ChatColor.RED;
            } else if (usage >= 80) {
                color = ChatColor.GOLD;
            } else if (usage >= 50) {
                color = ChatColor.YELLOW;
            }

            player.sendMessage(messageBuilder.getBase("event.blockPlaced")
                .usingTemplate("color", color.toString())
                .usingTemplate("count", String.valueOf(placedBlockAmount))
                .usingTemplate("limit", String.valueOf(limit))
                .usingTemplate("material", messageBuilder.getBase("materialNames." + blockType.name()).execute())
                .execute());
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        Optional<UUID> blockOwner = getLimitsManager().getBlockOwner(event.getBlock());

        if (!blockOwner.isPresent() || blockOwner.get().equals(event.getPlayer().getUniqueId())) return;

        OfflinePlayer owner = Bukkit.getOfflinePlayer(blockOwner.get());

        LimitsConfig.BlockPlacementConfig blockConfig = getLimitsManager().getPluginConfig().getBlockConfig();
        if (blockConfig.isBlockLimitedBlockDestruction()) {
            event.getPlayer().sendMessage(messageBuilder.getBase("event.removeOtherFail").usingTemplate("player", owner.getName()).execute());
            event.setCancelled(true);
            return;
        } else if (blockConfig.isDeleteBlocksDestroyedByOthers()) {
            PlayerBlockPlacementLimit playerLimit = getLimitsManager().getPlayerLimit(owner);
            playerLimit.removeBlock(event.getBlock());
            getLimitsManager().savePlayerLimits(owner);
            event.getPlayer().sendMessage(messageBuilder.getBase("event.removeOtherSuccess").usingTemplate("player", owner.getName()).execute());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void afterBlockBreak(BlockBreakEvent event) {

        PlayerBlockPlacementLimit playerLimit = getLimitsManager().getPlayerLimit(event.getPlayer());

        if (!playerLimit.isApplicable(event.getPlayer(), event.getBlock())) return;

        if (!playerLimit.hasPlacedBlock(event.getBlock())) return;

        playerLimit.getLimit(event.getBlock().getType()).ifPresent(limit -> {
            int newCount = playerLimit.removeBlock(event.getBlock());
            getLimitsManager().savePlayerLimits(event.getPlayer());

            double usage = newCount * 100.0 / (limit * 100.0);
            ChatColor color = ChatColor.GREEN;
            if (usage >= 95) {
                color = ChatColor.RED;
            } else if (usage >= 80) {
                color = ChatColor.GOLD;
            } else if (usage >= 50) {
                color = ChatColor.YELLOW;
            }

            event.getPlayer().sendMessage(messageBuilder.getBase("event.blockRemoved")
                .usingTemplate("color", color.toString())
                .usingTemplate("count", String.valueOf(newCount))
                .usingTemplate("limit", String.valueOf(limit))
                .usingTemplate("material", messageBuilder.getBase("materialNames." + event.getBlock().getType().name()).execute())
                .execute());
        });
    }
}

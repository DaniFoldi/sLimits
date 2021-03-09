package net.silthus.slimits.limits;

import lombok.Getter;
import lombok.Setter;
import net.silthus.slib.config.converter.MaterialMapConverter;
import net.silthus.slib.config.converter.MaterialMapLocationListConverter;
import net.silthus.slib.config.converter.UUIDConverter;
import net.silthus.slib.configlib.annotation.ConfigurationElement;
import net.silthus.slib.configlib.annotation.Convert;
import net.silthus.slib.configlib.annotation.Ignore;
import net.silthus.slimits.Constants;
import net.silthus.slimits.LimitMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@ConfigurationElement
public class PlayerBlockPlacementLimit {

    @Convert(UUIDConverter.class)
    private UUID playerUUID = UUID.randomUUID();
    private String playerName = "UNDEFINED";
    @Ignore
    private Map<String, LimitMode> limitConfigs = new HashMap<>();
    @Ignore
    private Map<Material, Integer> limits = new HashMap<>();
    @Convert(MaterialMapConverter.class)
    private Map<Material, Integer> counts = new HashMap<>();
    @Convert(MaterialMapLocationListConverter.class)
    private Map<Material, List<Location>> blockLocations = new HashMap<>();
    @Ignore
    private Map<Material, Set<String>> blockTypePermissions = new HashMap<>();

    public PlayerBlockPlacementLimit(OfflinePlayer player) {
        this.playerUUID = player.getUniqueId();
        this.playerName = player.getName();
    }

    public PlayerBlockPlacementLimit() {
    }

    public void registerLimitConfig(BlockPlacementLimitConfig config) {
        if (limitConfigs.containsKey(config.getIdentifier())) {
            return;
        }

        switch (config.getMode()) {
            case ABSOLUTE:
                // override any limits that are set with the absolute values
                limits.putAll(config.getBlocks());
                break;
            case SUBTRACT:
                // we don't want to modify the absolute configs
                if (limitConfigs.containsValue(LimitMode.ABSOLUTE)) return;

                config.getBlocks().forEach((key, value) -> {
                    int newLimit = limits.getOrDefault(key, 0) - value;
                    if (newLimit < 0) newLimit = 0;
                    limits.put(key, newLimit);
                });
                break;
            case ADD:
            default:
                // we don't want to modify the absolute configs
                if (limitConfigs.containsValue(LimitMode.ABSOLUTE)) return;

                config.getBlocks().forEach((key, value) -> limits.put(key, limits.getOrDefault(key, 0) + value));
                break;
        }

        limitConfigs.put(config.getIdentifier(), config.getMode());
        addPermissions(config);
    }

    public void unregisterLimitConfig(BlockPlacementLimitConfig config) {
        LimitMode limitMode = limitConfigs.remove(config.getIdentifier());
        if (limitMode == null) return;

        // invert the limit change
        switch (limitMode) {
            case ABSOLUTE:
                limitConfigs.clear();
                limits.clear();
                break;
            case SUBTRACT:
                config.getBlocks().forEach((key, value) -> limits.put(key, limits.getOrDefault(key, 0) + value));
                break;
            default:
            case ADD:
                config.getBlocks().forEach((key, value) -> {
                    int newLimit = limits.getOrDefault(key, 0) - value;
                    if (newLimit < 0) newLimit = 0;
                    limits.put(key, newLimit);
                });
                break;
        }

        removePermissions(config);
    }

    public Optional<Integer> getLimit(Material blockType) {
        Material b = blockType;
        if (blockType.equals(Material.PLAYER_WALL_HEAD)) {
            b = Material.PLAYER_HEAD;
        }

        return Optional.ofNullable(getLimits().get(b));
    }

    public int addBlock(Block block) {
        Material b = block.getType();
        if (b.equals(Material.PLAYER_WALL_HEAD)) {
            b = Material.PLAYER_HEAD;
        }

        if (hasPlacedBlock(block)) {
            return getCount(b);
        }

        int currentCount = getCount(b);
        currentCount++;

        counts.put(b, currentCount);
        addBlockLocation(block);

        return currentCount;
    }

    public int getCount(Material blockType) {
        Material b = blockType;
        if (blockType.equals(Material.PLAYER_WALL_HEAD)) {
            b = Material.PLAYER_HEAD;
        }

        if (!getCounts().containsKey(b)) {
            getCounts().put(b, 0);
        }
        return counts.getOrDefault(b, 0);
    }

    public boolean hasPlacedBlock(Block block) {
        Material b = block.getType();
        if (b.equals(Material.PLAYER_WALL_HEAD)) {
            b = Material.PLAYER_HEAD;
        }

        return getLocations(b).contains(block.getLocation());
    }

    public List<Location> getLocations(Material material) {
        Material b = material;
        if (material.equals(Material.PLAYER_WALL_HEAD)) {
            b = Material.PLAYER_HEAD;
        }

        return blockLocations.getOrDefault(b, new ArrayList<>());
    }

    public int removeBlock(Block block) {

        Material b = block.getType();
        if (b.equals(Material.PLAYER_WALL_HEAD)) {
            b = Material.PLAYER_HEAD;
        }

        int count = getCount(b);

        if (removeBlockLocation(block)) {
            count--;
            getCounts().put(b, count);
        }

        if (count < 0) {
            count = 0;
            getCounts().remove(b);
        }

        return count;
    }

    public boolean isApplicable(Player player, Block block) {
        Material b = block.getType();
        if (b.equals(Material.PLAYER_WALL_HEAD)) {
            b = Material.PLAYER_HEAD;
        }

        boolean isLimitedBlock = getLimits().containsKey(b);
        boolean hasPermission = getBlockTypePermissions()
                .getOrDefault(b, new HashSet<>()).stream()
                .anyMatch(player::hasPermission);
        boolean isExcluded = player.hasPermission(Constants.PERMISSION_EXCLUDE_FROM_LIMITS);

        return (isLimitedBlock && hasPermission) && !isExcluded;
    }

    public boolean hasReachedLimit(Material blockType) {
        Material b = blockType;
        if (blockType.equals(Material.PLAYER_WALL_HEAD)) {
            b = Material.PLAYER_HEAD;
        }

        Optional<Integer> limit = getLimit(b);

        final Material finalB = b;
        return limit.filter(integer -> getCount(finalB) >= integer).isPresent();
    }

    private void addPermissions(BlockPlacementLimitConfig config) {
        for (Material material : config.getBlocks().keySet()) {
            if (!blockTypePermissions.containsKey(material)) {
                blockTypePermissions.put(material, new HashSet<>());
            }
            blockTypePermissions.get(material).add(config.getPermission());
        }
    }

    private void removePermissions(BlockPlacementLimitConfig config) {
        for (Material material : config.getBlocks().keySet()) {
            if (!blockTypePermissions.containsKey(material)) {
                blockTypePermissions.put(material, new HashSet<>());
            }
            blockTypePermissions.get(material).remove(config.getPermission());
        }
    }

    private void addBlockLocation(Block block) {
        Material b = block.getType();
        if (b.equals(Material.PLAYER_WALL_HEAD)) {
            b = Material.PLAYER_HEAD;
        }

        if (!blockLocations.containsKey(b)) {
            blockLocations.put(b, new ArrayList<>());
        }
        blockLocations.get(b).add(block.getLocation());
    }

    private boolean removeBlockLocation(Block block) {
        Material b = block.getType();
        if (b.equals(Material.PLAYER_WALL_HEAD)) {
            b = Material.PLAYER_HEAD;
        }
        return blockLocations.getOrDefault(b, new ArrayList<>()).remove(b);
    }
}

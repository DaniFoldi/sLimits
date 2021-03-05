package net.silthus.slimits.ui;

import com.danifoldi.messagelib.core.MessageBuilder;
import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import net.silthus.slimits.LimitsManager;
import net.silthus.slimits.LimitsPlugin;
import net.silthus.slimits.limits.PlayerBlockPlacementLimit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LimitsGUI {

    private final LimitsPlugin plugin;
    private final LimitsManager limitsManager;
    private final MessageBuilder<String, String> messageBuilder;

    public LimitsGUI(LimitsPlugin plugin, LimitsManager limitsManager, MessageBuilder<String, String> messageBuilder) {
        this.plugin = plugin;
        this.limitsManager = limitsManager;
        this.messageBuilder = messageBuilder;
    }

    public Gui showLimits(Player player) {

        Gui gui = new Gui(plugin, 6, messageBuilder.getBase("gui.title").execute());
        PaginatedPane page = new PaginatedPane(0, 0, 9, 5);

        PlayerBlockPlacementLimit playerLimit = limitsManager.getPlayerLimit(player);
        Map<Material, Integer> limits = playerLimit.getLimits();

        List<GuiItem> itemList = new ArrayList<>();

        limits.forEach((material, limit) -> {
            StringBuilder sb = new StringBuilder();
            ItemStack item = new ItemStack(material);

            ItemMeta itemMeta = item.getItemMeta();
            int count = playerLimit.getCount(material);
            double usage = count * 100.0 / limit * 100.0;

            ChatColor color = ChatColor.GREEN;
            if (usage >= 95) {
                color = ChatColor.RED;
            } else if (usage >= 80) {
                color = ChatColor.GOLD;
            } else if (usage >= 50) {
                color = ChatColor.YELLOW;
            }

            itemMeta.setDisplayName(messageBuilder.getBase("gui.limitItem")
                    .usingTemplate("material", messageBuilder.getBase("materialNames." + material.name()).execute())
                    .usingTemplate("color", color.toString())
                    .usingTemplate("count", String.valueOf(count))
                    .usingTemplate("limit", String.valueOf(limit))
                    .execute());
            itemMeta.setLore(Arrays.asList(messageBuilder.getBase("gui.showLocations").execute()));
            item.setItemMeta(itemMeta);

            GuiItem guiItem = new GuiItem(item, click -> {
                click.setCancelled(true);

                PaginatedPane locationsPane = new PaginatedPane(0, 0, 9, 5, Pane.Priority.HIGH);
                locationsPane.addPane(0, getBackButton(gui, locationsPane, page));

                ArrayList<ItemStack> locationItems = new ArrayList<>();

                for (Location location : playerLimit.getLocations(material)) {
                    ItemStack locationItem = new ItemStack(material, 1);
                    ItemMeta locationItemMeta = locationItem.getItemMeta();

                    locationItemMeta.setDisplayName(messageBuilder.getBase("gui.locationItemName").usingTemplate("material", messageBuilder.getBase("materialNames." + material.name()).execute()).execute());
                    locationItemMeta.setLore(Collections.singletonList(messageBuilder.getBase("gui.locationItemLore")
                            .usingTemplate("x", String.valueOf(location.getBlockX()))
                            .usingTemplate("y", String.valueOf(location.getBlockY()))
                            .usingTemplate("z", String.valueOf(location.getBlockZ()))
                            .usingTemplate("world", messageBuilder.getBase("worldNames." + location.getWorld().getName()).execute())
                            .execute()));

                    locationItem.setItemMeta(locationItemMeta);
                    locationItems.add(locationItem);
                }

                List<GuiItem> guiItems = locationItems.stream()
                        .map(itemStack -> new GuiItem(itemStack, inventoryClickEvent -> inventoryClickEvent.setCancelled(true)))
                        .collect(Collectors.toList());

                locationsPane.populateWithGuiItems(guiItems);
                page.setVisible(false);
                locationsPane.setVisible(true);
                gui.addPane(locationsPane);
                gui.update();
            });

            itemList.add(guiItem);
        });

        List<GuiItem> guiItems = itemList.stream()
                .sorted(Comparator.comparing(o -> o.getItem().getType()))
                .collect(Collectors.toList());

        page.populateWithGuiItems(guiItems);

        gui.addPane(page);

        gui.show(player);

        return gui;
    }

    public StaticPane getBackButton(Gui gui, Pane current, Pane parent) {
        StaticPane back = new StaticPane(0, 5, 1, 1);
        ItemStack itemStack = new ItemStack(Material.BARRIER);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(messageBuilder.getBase("gui.back").execute());
        itemStack.setItemMeta(itemMeta);
        back.addItem(new GuiItem(itemStack, click -> {
            click.setCancelled(true);
            current.setVisible(false);
            parent.setVisible(true);
            gui.update();
        }), 0, 0);

        return back;
    }
}

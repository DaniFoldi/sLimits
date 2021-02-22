package net.silthus.slimits;

import co.aikar.commands.PaperCommandManager;
import com.danifoldi.messagelib.core.MessageBuilder;
import com.danifoldi.messagelib.messageprovider.MessageProvider;
import com.danifoldi.messagelib.templateprocessor.TemplateProcessor;
import com.danifoldi.messagelib.yaml.YamlMessageProvider;
import kr.entree.spigradle.annotations.Plugin;
import lombok.Getter;
import net.silthus.slib.bukkit.BasePlugin;
import net.silthus.slimits.commands.LimitsCommand;
import net.silthus.slimits.ui.LimitsGUI;
import org.bstats.bukkit.Metrics;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

@Plugin
public class LimitsPlugin extends BasePlugin implements Listener {

    private static final int BSTATS_ID = 7979;

    public static String PLUGIN_PATH;

    @Getter
    private LimitsManager limitsManager;
    @Getter
    private LimitsGUI gui;
    private PaperCommandManager commandManager;
    private Metrics metrics;
    private MessageBuilder<String> messageBuilder;

    public LimitsPlugin() {
        metrics = new Metrics(this, BSTATS_ID);
    }

    public LimitsPlugin(
            JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    public Optional<Metrics> getMetrics() {

        return Optional.ofNullable(metrics);
    }

    @Override
    public void enable() {

        PLUGIN_PATH = getDataFolder().getAbsolutePath();

        try {
            MessageProvider<String> messageProvider = new YamlMessageProvider(Paths.get(PLUGIN_PATH, "messages.yaml"));
            this.messageBuilder = new MessageBuilder<>(messageProvider, TemplateProcessor.bracket());
        } catch (IOException e) {
            System.out.println("File not found:" + e.getMessage());
            e.printStackTrace();
        }

        this.limitsManager = new LimitsManager(this, new LimitsConfig(new File(getDataFolder(), "config.yaml").toPath()), messageBuilder);
        this.gui = new LimitsGUI(this, limitsManager, messageBuilder);
        this.commandManager = new PaperCommandManager(this);

        this.commandManager.registerCommand(new LimitsCommand(getLimitsManager(), getGui(), messageBuilder));

        getLimitsManager().load();
    }

    @Override
    public void disable() {
        getLimitsManager().unload();
    }
}

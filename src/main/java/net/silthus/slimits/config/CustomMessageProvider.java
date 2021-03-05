package net.silthus.slimits.config;

import com.danifoldi.messagelib.yaml.YamlMessageProvider;
import org.bukkit.ChatColor;

import java.io.IOException;
import java.nio.file.Path;

public class CustomMessageProvider extends YamlMessageProvider {

    public CustomMessageProvider(Path messageFile) throws IOException {
        super(messageFile);
    }

    @Override
    public String getMessageBase(String id) {
        return ChatColor.translateAlternateColorCodes('&', super.getMessageBase(id));
    }
}

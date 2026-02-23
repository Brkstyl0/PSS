package me.brkstyl.pss.config;

import me.brkstyl.pss.PluginMain;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LanguageManager {

    private final PluginMain plugin;
    private FileConfiguration langConfig;

    public LanguageManager(PluginMain plugin) {
        this.plugin = plugin;
        loadLanguage();
    }

    public void loadLanguage() {
        String lang = plugin.getConfig().getString("language", "en");
        File langFile = new File(plugin.getDataFolder(), "messages_" + lang + ".yml");

        if (!langFile.exists()) {
            plugin.saveResource("messages_" + lang + ".yml", false);
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public String getMessage(String path) {
        return langConfig.getString(path, "Message path not found: " + path);
    }
}
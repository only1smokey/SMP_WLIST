package pro.jabo.jlist;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

import java.util.List;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;


public class WhitelistManager {
    private final Set<String> whitelist;
    private final FileConfiguration config;
    private final File whitelistFile;

    public WhitelistManager(File dataFolder) {
        this.whitelist = new HashSet<>();
        this.whitelistFile = new File(dataFolder, "whitelist.yml");
        this.config = YamlConfiguration.loadConfiguration(whitelistFile);
        loadWhitelist();
    }

    private void loadWhitelist() {
        List<String> whitelistList = config.getStringList("whitelist");
        whitelist.addAll(whitelistList);
    }

    public synchronized boolean isWhitelisted(String playerName) {
        return whitelist.contains(playerName.toLowerCase());
    }

    public synchronized void addToWhitelist(String playerName) {
        whitelist.add(playerName.toLowerCase());
        saveWhitelist();
    }

    public synchronized void removeFromWhitelist(String playerName) {
        whitelist.remove(playerName.toLowerCase());
        saveWhitelist();
    }

    private void saveWhitelist() {
        config.set("whitelist", new ArrayList<>(whitelist));
        try {
            config.save(whitelistFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
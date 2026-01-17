package ru.stepanyaa.economyGUI.utils;

import java.io.File;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

public class MessageUtil {
	private void loadMessages() {
        String messagesFileName = "messages_" + language + ".yml";
        messagesFile = new File(getDataFolder(), messagesFileName);
        try {
            if (!messagesFile.exists()) {
                if (getResource(messagesFileName) != null) {
                    saveResource(messagesFileName, false);
                    getLogger().info("Created messages file: " + messagesFileName);
                } else {
                    getLogger().warning("Messages file " + messagesFileName + " not found in plugin!");
                    messagesConfig = new YamlConfiguration();
                    return;
                }
            }
            messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
            String currentFileVersion = messagesConfig.getString("version", "0.0.0");
            if (!currentFileVersion.equals(CURRENT_VERSION)) {
                if (getResource(messagesFileName) != null) {
                    File backupFile = new File(getDataFolder(), messagesFileName + ".backup");
                    if (messagesFile.renameTo(backupFile)) {
                        getLogger().info("Backed up old messages file to: " + messagesFileName + ".backup");
                    }
                    saveResource(messagesFileName, true);
                    messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
                    messagesConfig.set("version", CURRENT_VERSION);
                    messagesConfig.save(messagesFile);
                    getLogger().info("Updated messages file " + messagesFileName + " to version " + CURRENT_VERSION);
                } else {
                    getLogger().warning("Resource " + messagesFileName + " not found in plugin!");
                }
            } else if (isFirstEnable) {
                getLogger().info("Messages file " + messagesFileName + " is up-to-date (version " + CURRENT_VERSION + ").");
            }
        } catch (Exception e) {
            getLogger().severe("Failed to load messages file: " + e.getMessage());
            messagesConfig = new YamlConfiguration();
        }
    }

    private void loadTransactions() {
        transactionsFile = new File(getDataFolder(), "transactions.yml");
        if (!transactionsFile.exists()) {
            try {
                transactionsFile.createNewFile();
                getLogger().info("Created transactions file: transactions.yml");
            } catch (IOException e) {
                getLogger().severe("Failed to create transactions.yml: " + e.getMessage());
            }
        }
        transactionsConfig = YamlConfiguration.loadConfiguration(transactionsFile);
        economySearchGUI.loadTransactionHistory(transactionsConfig);
    }
    public void saveTransactions() {
        economySearchGUI.cleanOldTransactions();
        economySearchGUI.saveTransactionHistory(transactionsConfig);
        try {
            transactionsConfig.save(transactionsFile);
        } catch (IOException e) {
            getLogger().severe("Failed to save transactions.yml: " + e.getMessage());
        }
    }
    public String getMessage(String key) {
        if (messagesConfig == null) {
            return ChatColor.translateAlternateColorCodes('&', key);
        }
        String msg = messagesConfig.getString(key, key);
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
    public String getMessage(String key, String def) {
        if (messagesConfig == null) {
            return ChatColor.translateAlternateColorCodes('&', def);
        }
        String msg = messagesConfig.getString(key, def);
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
    public String getMessage(String key, String def, Object... placeholders) {
        String msg = getMessage(key, def);
        if (placeholders != null && placeholders.length >= 2 && placeholders.length % 2 == 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                String placeholder = placeholders[i].toString();
                String value = placeholders[i + 1].toString();
                msg = msg.replace("%" + placeholder + "%", value);
            }
        }
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}

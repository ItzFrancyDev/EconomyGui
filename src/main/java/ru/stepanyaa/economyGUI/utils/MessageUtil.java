/**
 * MIT License
 *
 * EconomyGui
 * Copyright (c) 2025 Stepanyaa

 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ru.stepanyaa.economyGUI.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.stepanyaa.economyGUI.EconomyGUI;

import java.io.File;
import java.io.IOException;

public class MessageUtil {
    private FileConfiguration messagesConfig;
    private File messagesFile;

    private FileConfiguration transactionsConfig;
    private File transactionsFile;

    public boolean init() {
        loadMessages();
        if (messagesConfig == null) {
            EconomyGUI.getInstance().getLogger().severe("Failed to load messages configuration. Disabling plugin.");
            Bukkit.getServer().getPluginManager().disablePlugin(EconomyGUI.getInstance());
            return true;
        }
        return false;
    }

	public void loadMessages() {
        String messagesFileName = "messages_" + EconomyGUI.getInstance().getLanguage() + ".yml";
        messagesFile = new File(EconomyGUI.getInstance().getDataFolder(), messagesFileName);
        try {
            if (!messagesFile.exists()) {
                if (EconomyGUI.getInstance().getResource(messagesFileName) != null) {
                    EconomyGUI.getInstance().saveResource(messagesFileName, false);
                    EconomyGUI.getInstance().getLogger().info("Created messages file: " + messagesFileName);
                } else {
                    EconomyGUI.getInstance().getLogger().warning("Messages file " + messagesFileName + " not found in plugin!");
                    messagesConfig = new YamlConfiguration();
                    return;
                }
            }
            messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
            String currentFileVersion = messagesConfig.getString("version", "0.0.0");
            if (!currentFileVersion.equals(EconomyGUI.CURRENT_VERSION)) {
                if (EconomyGUI.getInstance().getResource(messagesFileName) != null) {
                    File backupFile = new File(EconomyGUI.getInstance().getDataFolder(), messagesFileName + ".backup");
                    if (messagesFile.renameTo(backupFile)) {
                        EconomyGUI.getInstance().getLogger().info("Backed up old messages file to: " + messagesFileName + ".backup");
                    }
                    EconomyGUI.getInstance().saveResource(messagesFileName, true);
                    messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
                    messagesConfig.set("version", EconomyGUI.CURRENT_VERSION);
                    messagesConfig.save(messagesFile);
                    EconomyGUI.getInstance().getLogger().info("Updated messages file " + messagesFileName + " to version " + EconomyGUI.CURRENT_VERSION);
                } else {
                    EconomyGUI.getInstance().getLogger().warning("Resource " + messagesFileName + " not found in plugin!");
                }
            } else if (EconomyGUI.getInstance().isFirstEnable) {
                EconomyGUI.getInstance().getLogger().info("Messages file " + messagesFileName + " is up-to-date (version " + EconomyGUI.CURRENT_VERSION + ").");
            }
        } catch (Exception e) {
            EconomyGUI.getInstance().getLogger().severe("Failed to load messages file: " + e.getMessage());
            messagesConfig = new YamlConfiguration();
        }
    }

    public void loadTransactions() {
        transactionsFile = new File(EconomyGUI.getInstance().getDataFolder(), "transactions.yml");
        if (!transactionsFile.exists()) {
            try {
                transactionsFile.createNewFile();
                EconomyGUI.getInstance().getLogger().info("Created transactions file: transactions.yml");
            } catch (IOException e) {
                EconomyGUI.getInstance().getLogger().severe("Failed to create transactions.yml: " + e.getMessage());
            }
        }
        transactionsConfig = YamlConfiguration.loadConfiguration(transactionsFile);
        EconomyGUI.getInstance().getEconomySearchGUI().loadTransactionHistory(transactionsConfig);
    }

    public void saveTransactions() {
        EconomyGUI.getInstance().getEconomySearchGUI().cleanOldTransactions();
        EconomyGUI.getInstance().getEconomySearchGUI().saveTransactionHistory(transactionsConfig);
        try {
            transactionsConfig.save(transactionsFile);
        } catch (IOException e) {
            EconomyGUI.getInstance().getLogger().severe("Failed to save transactions.yml: " + e.getMessage());
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

package ru.stepanyaa.economyGUI.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import ru.stepanyaa.economyGUI.EconomyGUI;

import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CommonUtil {
    private String latestVersion = null;

	public boolean isNewerVersion(String version1, String version2) {
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");
        for (int i = 0; i < Math.min(parts1.length, parts2.length); i++) {
            int num1 = Integer.parseInt(parts1[i]);
            int num2 = Integer.parseInt(parts2[i]);
            if (num1 > num2) return true;
            if (num1 < num2) return false;
        }
        return parts1.length > parts2.length;
    }
	
    public void updateConfigFile() {
        File configFile = new File(EconomyGUI.getInstance().getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            EconomyGUI.getInstance().saveResource("config.yml", false);
            EconomyGUI.getInstance().getLogger().info(EconomyGUI.getMessageUtil().getMessage("warning.config-file-create", "Created config file: config.yml"));
        }
        YamlConfiguration existingConfig = YamlConfiguration.loadConfiguration(configFile);
        String currentFileVersion = existingConfig.getString("config-version", "0.0.0");
        if (currentFileVersion.equals(EconomyGUI.CURRENT_VERSION)) {
            if (EconomyGUI.getInstance().isFirstEnable) {
                EconomyGUI.getInstance().getLogger().info(EconomyGUI.getMessageUtil().getMessage("warning.config-file-up-to-date", "Config file config.yml is up-to-date (version %version%).")
                        .replace("%version%", EconomyGUI.CURRENT_VERSION));
            }
            return;
        }
        if (EconomyGUI.getInstance().getResource("config.yml") != null) {
            try {
                EconomyGUI.getInstance().saveResource("config.yml", true);
                EconomyGUI.getInstance().getLogger().info(EconomyGUI.getMessageUtil().getMessage("warning.config-file-updated", "Updated config.yml to version %version%.")
                        .replace("%version%", EconomyGUI.CURRENT_VERSION));
                YamlConfiguration newConfig = YamlConfiguration.loadConfiguration(configFile);
                newConfig.set("config-version", EconomyGUI.CURRENT_VERSION);
                newConfig.save(configFile);
            } catch (Exception e) {
            	EconomyGUI.getInstance().getLogger().warning("Failed to update config.yml: " + e.getMessage());
            }
        } else {
        	EconomyGUI.getInstance().getLogger().warning(EconomyGUI.getMessageUtil().getMessage("warning.config-file-not-found", "Resource config.yml not found in plugin!"));
        }
    }
    
    public void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(EconomyGUI.getInstance(), () -> {
            try {
                URL url = new URL("https://api.modrinth.com/v2/project/economygui/version");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "EconomyGUI/" + EconomyGUI.CURRENT_VERSION);
                conn.connect();
                if (conn.getResponseCode() == 200) {
                    JsonArray versions = JsonParser.parseReader(new InputStreamReader(conn.getInputStream())).getAsJsonArray();
                    String highestVersion = null;
                    for (JsonElement element : versions) {
                        String versionNumber = element.getAsJsonObject().get("version_number").getAsString();
                        String versionType = element.getAsJsonObject().get("version_type").getAsString();
                        if (versionNumber.contains("-SNAPSHOT") && !versionType.equals("release")) {
                            continue;
                        }
                        if (highestVersion == null || isNewerVersion(versionNumber, highestVersion)) {
                            highestVersion = versionNumber;
                        }
                    }
                    if (highestVersion != null && isNewerVersion(highestVersion, EconomyGUI.CURRENT_VERSION)) {
                        String[] currentParts = EconomyGUI.CURRENT_VERSION.split("\\.");
                        String[] highestParts = highestVersion.split("\\.");
                        if (currentParts.length == 3 && highestParts.length == 3) {
                            int currentMajor = Integer.parseInt(currentParts[0]);
                            int currentMinor = Integer.parseInt(currentParts[1]);
                            int currentPatch = Integer.parseInt(currentParts[2]);
                            int highestMajor = Integer.parseInt(highestParts[0]);
                            int highestMinor = Integer.parseInt(highestParts[1]);
                            int highestPatch = Integer.parseInt(highestParts[2]);
                            if (currentMajor == highestMajor && currentMinor == highestMinor && highestPatch == currentPatch + 1) {
                                latestVersion = highestVersion;
                                EconomyGUI.getInstance().getLogger().warning("*** UPDATE AVAILABLE *** A new version of EconomyGUI (" + latestVersion + ") is available at:\nhttps://modrinth.com/plugin/economygui/versions");
                            }
                        }
                    }
                }
                conn.disconnect();
            } catch (Exception e) {
            	EconomyGUI.getInstance().getLogger().warning("Failed to check for updates: " + e.getMessage());
            }
        });
    }

    public void reloadPlugin(Player player) {
        FileConfiguration config = EconomyGUI.getInstance().getConfig();

        EconomyGUI.getInstance().reloadConfig();
        EconomyGUI.getInstance().setLanguage(config.getString("language", "en"));
        EconomyGUI.getMessageUtil().loadMessages();
        EconomyGUI.getMessageUtil().loadTransactions();
        EconomyGUI.getInstance().getCommon().updateConfigFile();
        EconomyGUI.getInstance().setTransactionRetentionDays(config.getInt("features.transaction-retention-days", 30));
        EconomyGUI.getInstance().setPlayerSelectionEnabled(config.getBoolean("features.player-selection", true));
        EconomyGUI.getInstance().setMassOperationsEnabled(config.getBoolean("features.mass-operations", true));
        EconomyGUI.getInstance().setQuickActionsEnabled(config.getBoolean("features.quick-actions", true));
        EconomyGUI.getInstance().setFullManagementEnabled(config.getBoolean("features.full-management", true));
        EconomyGUI.getInstance().getEconomySearchGUI().refreshOpenGUIs();
        player.sendMessage(ChatColor.GREEN + EconomyGUI.getMessageUtil().getMessage("action.config-reloaded", "Configuration reloaded."));
    }
}

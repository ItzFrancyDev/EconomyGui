/**
 * MIT License
 *
 * EconomyGui
 * Copyright (c) 2025 Stepanyaa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ru.stepanyaa.economyGUI;

import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import ru.stepanyaa.economyGUI.commands.EcoGuiCommand;
import ru.stepanyaa.economyGUI.utils.CommonUtil;
import ru.stepanyaa.economyGUI.utils.MessageUtil;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EconomyGUI extends JavaPlugin {
	@Getter
	private static EconomyGUI instance;
    @Getter
    private static MessageUtil messageUtil;
	@Getter
	private CommonUtil common;
    @Getter
    private EconomySearchGUI economySearchGUI;
	
    public static String CURRENT_VERSION;
    public boolean isFirstEnable = true;

	@Getter
    private Economy econ = null;
    @Getter
    private final Set<String> adminUUIDs = ConcurrentHashMap.newKeySet();

    @Getter @Setter
    private String language;

    @Getter @Setter
    private boolean playerSelectionEnabled;
    @Getter @Setter
    private boolean massOperationsEnabled;
    @Getter @Setter
    private boolean quickActionsEnabled;
    @Getter @Setter
    private boolean fullManagementEnabled;
    @Getter @Setter
    private int transactionRetentionDays;

    @Override
    public void onEnable() {
    	instance = this;
    	CURRENT_VERSION = getDescription().getVersion();
        if (!setupEconomy()) {
            getLogger().severe(messageUtil.getMessage("warning.no-economy", "Economy provider not found! Disabling plugin."));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        common = new CommonUtil();
        
        //CONFIG INIT
        saveDefaultConfig();
        common.updateConfigFile();
        reloadConfig();
        
        //CONFIG READ
        language = getConfig().getString("language", "en");
        playerSelectionEnabled = getConfig().getBoolean("features.player-selection", true);
        massOperationsEnabled = getConfig().getBoolean("features.mass-operations", true);
        quickActionsEnabled = getConfig().getBoolean("features.quick-actions", true);
        fullManagementEnabled = getConfig().getBoolean("features.full-management", true);
        transactionRetentionDays = getConfig().getInt("features.transaction-retention-days", 30);
        
        //MESSAGES
        messageUtil = new MessageUtil();
        if (messageUtil.init()) return;
        
        //WARNING: FUCTION LIMITED
        if (!playerSelectionEnabled && !massOperationsEnabled && !quickActionsEnabled && !fullManagementEnabled) {
            getLogger().warning(messageUtil.getMessage("error.all-features-disabled", "All features are disabled in config! Commands will be limited."));
        }
        
        //ECONOMY GUI
        economySearchGUI = new EconomySearchGUI(this);
        getServer().getPluginManager().registerEvents(economySearchGUI, this);
        
        messageUtil.loadTransactions();
        PluginCommand command = getCommand("economygui");
        if (command != null) {
            command.setExecutor(new EcoGuiCommand());
            command.setTabCompleter(new EcoGuiCommand());
        } else {
            getLogger().warning("Failed to register command 'economygui'!");
        }
        
        
        adminUUIDs.addAll(getConfig().getStringList("admin-uuids"));
        getLogger().info(messageUtil.getMessage("warning.plugin-enabled", "EconomyGUI enabled with language: %lang%", "lang", language));
        common.checkForUpdates();
        this.isFirstEnable = false;
    }
    
    @Override
    public void onDisable() {
        messageUtil.saveTransactions();
        getLogger().info("EconomyGUI disabled.");
    }
    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
}
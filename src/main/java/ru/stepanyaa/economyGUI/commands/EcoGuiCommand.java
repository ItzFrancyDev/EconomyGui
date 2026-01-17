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
package ru.stepanyaa.economyGUI.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.stepanyaa.economyGUI.EconomyGUI;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EcoGuiCommand implements CommandExecutor, TabCompleter {
	@Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + EconomyGUI.getMessageUtil().getMessage("error.player-only", "This command is for players only!"));
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("economygui.admin") && !player.hasPermission("economygui.gui")) {
            player.sendMessage(ChatColor.RED + EconomyGUI.getMessageUtil().getMessage("error.no-permission", "You don't have permission!"));
            return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("gui")) {
            if (args.length > 1) {
                player.sendMessage(ChatColor.RED + EconomyGUI.getMessageUtil().getMessage("command.usage-gui", "Usage: /economygui gui"));
                return true;
            }
            EconomyGUI.getInstance().getEconomySearchGUI().openLastGUIMenu(player);
            return true;
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("economygui.reload")) {
                player.sendMessage(ChatColor.RED + EconomyGUI.getMessageUtil().getMessage("error.no-permission", "You don't have permission!"));
                return true;
            }
            if (args.length != 1) {
                player.sendMessage(ChatColor.RED + EconomyGUI.getMessageUtil().getMessage("command.usage-reload", "Usage: /economygui reload"));
                return true;
            }
            EconomyGUI.getInstance().getCommon().reloadPlugin(player);
            return true;
        } else if (args[0].equalsIgnoreCase("reset")) {
            if (!player.hasPermission("economygui.reset")) {
                player.sendMessage(ChatColor.RED + EconomyGUI.getMessageUtil().getMessage("error.no-permission", "You don't have permission!"));
                return true;
            }
            if (args.length != 1) {
                player.sendMessage(ChatColor.RED + EconomyGUI.getMessageUtil().getMessage("command.usage", "Usage: /economygui reset"));
                return true;
            }
            EconomyGUI.getInstance().getEconomySearchGUI().resetSearch(player);
            return true;
        }
        player.sendMessage(ChatColor.RED + EconomyGUI.getMessageUtil().getMessage("command.usage", "Usage: /economygui <gui | reload | reset>"));
        return true;
    }
	
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("gui", "reload", "reset").stream()
                    .filter(cmd -> sender.hasPermission("economygui." + cmd) || sender.hasPermission("economygui.admin"))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

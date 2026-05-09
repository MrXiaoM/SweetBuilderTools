package top.mrxiaom.sweet.buildertools.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.buildertools.Messages;
import top.mrxiaom.sweet.buildertools.SweetBuilderTools;
import top.mrxiaom.sweet.buildertools.data.ToolConfig;
import top.mrxiaom.sweet.buildertools.func.AbstractModule;
import top.mrxiaom.sweet.buildertools.func.ToolsManager;

import java.util.*;

import static top.mrxiaom.pluginbase.utils.CollectionUtils.startsWith;

@AutoRegister
public class CommandMain extends AbstractModule implements CommandExecutor, TabCompleter, Listener {
    public CommandMain(SweetBuilderTools plugin) {
        super(plugin);
        registerCommand("sweetbuildertools", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length > 1 && "give".equalsIgnoreCase(args[0]) && sender.hasPermission("sweet.buildertools.give")) {
            ToolConfig tool = ToolsManager.inst().get(args[1]);
            if (tool == null) {
                return Messages.Command.give__not_found.tm(sender, Pair.of("%id%", args[1]));
            }
            Player target;
            if (args.length > 2) {
                target = Util.getOnlinePlayer(args[2]).orElse(null);
                if (target == null) {
                    return Messages.Command.player__not_online.tm(sender);
                }
            } else if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                return Messages.Command.player__only.tm(sender);
            }
            // 给予玩家物品
            ItemStack item = tool.createItem(target);
            ItemStackUtil.giveItemToPlayer(target, item);
            return Messages.Command.give__success.tm(sender,
                    Pair.of("%player%", target.getName()),
                    Pair.of("%id%", tool.id()));
        }
        if (args.length == 1 && "reload".equalsIgnoreCase(args[0]) && sender.hasPermission("sweet.buildertools.reload")) {
            plugin.reloadConfig();
            return Messages.Command.reload__success.tm(sender);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            if (sender.hasPermission("sweet.buildertools.give")) {
                list.add("give");
            }
            if (sender.isOp()) {
                list.add("reload");
            }
            return startsWith(args[0], list);
        }
        if (args.length == 2) {
            if ("give".equalsIgnoreCase(args[0]) && sender.hasPermission("sweet.buildertools.give")) {
                return startsWith(args[1], ToolsManager.inst().keys());
            }
        }
        if (args.length == 3) {
            if ("give".equalsIgnoreCase(args[0]) && sender.hasPermission("sweet.buildertools.give")) {
                return null;
            }
        }
        return Collections.emptyList();
    }
}

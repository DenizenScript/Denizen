package com.denizenscript.denizen.utilities.command;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.scripts.ScriptBuilder;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.queues.core.TimedQueue;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ExSustainedCommandHandler implements CommandExecutor, TabCompleter, Listener {

    public void enableFor(PluginCommand command) {
        command.setExecutor(this);
        command.setTabCompleter(this);
        Bukkit.getPluginManager().registerEvents(this, Denizen.getInstance());
    }

    public HashMap<UUID, TimedQueue> playerQueues = new HashMap<>();

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerQueues.remove(event.getPlayer().getUniqueId());
    }

    public TimedQueue getOrMakeQueue(final Player player, boolean quiet) {
        UUID id;
        if (player == null) {
            id = new UUID(0, 0);
        }
        else {
            id = player.getUniqueId();
        }
        TimedQueue queue = playerQueues.get(id);
        if (queue != null && !queue.isStopped) {
            return queue;
        }
        queue = new TimedQueue("EX_SUSTAINED_COMMAND", 0);
        queue.waitWhenEmpty = true;
        playerQueues.put(id, queue);
        return queue;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        List<Object> entries = new ArrayList<>();
        String entry = String.join(" ", args);
        boolean quiet = !Settings.showExDebug();
        if (entry.length() > 3 && entry.startsWith("-q ")) {
            quiet = !quiet;
            entry = entry.substring("-q ".length());
        }
        if (entry.length() < 2) {
            sender.sendMessage("/exs (-q) <denizen script command> (arguments)");
            return true;
        }
        TimedQueue queue = getOrMakeQueue(sender instanceof Player player ? player : null, quiet);
        if (!quiet && sender instanceof Player) {
            queue.debugOutput = s -> sender.spigot().sendMessage(FormattedTextHelper.parse(s.replace("<FORCE_ALIGN>", ""), net.md_5.bungee.api.ChatColor.WHITE));
        }
        else {
            queue.debugOutput = null;
        }
        if (queue.isPaused() || queue.isDelayed()) {
            sender.sendMessage(ChatColor.YELLOW + "Sustained queue is currently paused or waiting, adding command to queue for later execution.");
        }
        else if (Settings.showExHelp()) {
            if (CoreConfiguration.shouldShowDebug) {
                if (quiet) {
                    sender.sendMessage(ChatColor.YELLOW + "Executing Denizen script command... check the console for full debug output!");
                }
                else {
                    sender.sendMessage(ChatColor.YELLOW + "Executing Denizen script command...");
                }
            }
            else {
                sender.sendMessage(ChatColor.YELLOW + "Executing Denizen script command... to see debug, use /denizen debug");
            }
        }
        entries.add(entry);
        NPCTag npc = null;
        if (Depends.citizens != null && Depends.citizens.getNPCSelector().getSelected(sender) != null) {
            npc = new NPCTag(Depends.citizens.getNPCSelector().getSelected(sender));
        }
        List<ScriptEntry> scriptEntries = ScriptBuilder.buildScriptEntries(entries, null,
                new BukkitScriptEntryData(sender instanceof Player player ? new PlayerTag(player) : null, npc));
        queue.addEntries(scriptEntries);
        if (!queue.is_started) {
            queue.start();
        }
        else {
            queue.onStart();
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] rawArgs) {
        return Denizen.getInstance().exCommand.onTabComplete(sender, cmd, alias, rawArgs);
    }
}

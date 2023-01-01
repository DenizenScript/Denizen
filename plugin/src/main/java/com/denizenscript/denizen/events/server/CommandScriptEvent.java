package com.denizenscript.denizen.events.server;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.tags.core.EscapeTagUtil;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.Arrays;

public class CommandScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // command
    // <'command_name'> command
    //
    // @Group Server
    //
    // @Location true
    //
    // @Triggers when a player, console, or command block/minecart runs a Bukkit command. This happens before
    // any code of established commands, allowing scripts to 'override' existing commands.
    // Note that for the sake of the event line, escaping is used, so 'bukkit:plugins' becomes 'bukkit&coplugins'
    //
    // @Warning This event is to override existing commands, and should not be used to create new commands - use a command script instead.
    //
    // @Context
    // <context.command> returns the command name as an ElementTag.
    // <context.raw_args> returns any args used, unmodified as plaintext.
    // <context.args> returns a ListTag of the arguments.
    // <context.source_type> returns the source of the command. Can be: PLAYER, SERVER, COMMAND_BLOCK, or COMMAND_MINECART.
    // <context.command_block_location> returns the command block's location (if the command was run from one).
    // <context.command_minecart> returns the EntityTag of the command minecart (if the command was run from one).
    //
    // @Determine
    // "FULFILLED" to tell Bukkit the command was handled.
    //
    // @Player when source_type is player.
    //
    // -->

    public CommandScriptEvent() {
        registerCouldMatcher("command");
        registerCouldMatcher("<'command_name'> command");
    }

    public String commandName;
    public String fullMessage;
    public String sourceType;
    public Location location;
    public PlayerCommandPreprocessEvent playerEvent;
    public ServerCommandEvent serverEvent;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, playerEvent == null ? null : playerEvent.getPlayer().getLocation())) {
            return false;
        }
        if (!path.eventArgLowerAt(0).equals("command") && !runGenericCheck(path.eventArgLowerAt(0), EscapeTagUtil.escape(commandName))) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(playerEvent == null ? null : new PlayerTag(playerEvent.getPlayer()), null);
    }

    public String cleanMessageArgs() {
        return fullMessage.split(" ").length > 1 ? fullMessage.split(" ", 2)[1] : "";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            String determination = determinationObj.toString();
            String lower = CoreUtilities.toLowerCase(determination);
            if (lower.equals("fulfilled")) {
                cancelled = true;
                cancellationChanged();
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("command")) {
            return new ElementTag(commandName, true);
        }
        else if (name.equals("raw_args")) {
            return new ElementTag(cleanMessageArgs(), true);
        }
        else if (name.equals("args")) {
            return new ListTag(Arrays.asList(ArgumentHelper.buildArgs(cleanMessageArgs(), false)), true);
        }
        else if (name.equals("server")) {
            return new ElementTag(sourceType.equals("server"));
        }
        else if (name.equals("source_type")) {
            return new ElementTag(sourceType);
        }
        else if (name.equals("command_block_location") && serverEvent != null && serverEvent.getSender() instanceof BlockCommandSender) {
            return new LocationTag(((BlockCommandSender) serverEvent.getSender()).getBlock().getLocation());
        }
        else if (name.equals("command_minecart") && serverEvent != null && serverEvent.getSender() instanceof CommandMinecart) {
            return new EntityTag((CommandMinecart) serverEvent.getSender());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerEvent(PlayerCommandPreprocessEvent event) {
        this.playerEvent = event;
        this.serverEvent = null;
        this.fullMessage = event.getMessage();
        this.commandName = fullMessage.split(" ")[0].substring(1);
        this.location = event.getPlayer().getLocation();
        this.sourceType = "player";
        fire(event);
    }

    @Override
    public void cancellationChanged() {
        if (cancelled && serverEvent != null) {
            serverEvent.setCommand("denizen do_nothing");
        }
        super.cancellationChanged();
    }

    @EventHandler
    public void onServerEvent(ServerCommandEvent event) {
        this.playerEvent = null;
        this.serverEvent = event;
        this.fullMessage = event.getCommand();
        this.commandName = fullMessage.split(" ")[0];
        if (event.getSender() instanceof BlockCommandSender) {
            this.location = ((BlockCommandSender) event.getSender()).getBlock().getLocation();
            this.sourceType = "command_block";
        }
        else if (event.getSender() instanceof CommandMinecart) {
            this.location = ((CommandMinecart) event.getSender()).getLocation();
            this.sourceType = "command_minecart";
        }
        else {
            this.location = null;
            this.sourceType = "server";
        }
        fire(event);
    }
}

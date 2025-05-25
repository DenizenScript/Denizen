package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.paper.PaperModule;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.command.UnknownCommandEvent;

import java.util.Arrays;

public class UnknownCommandScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // command unknown
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Cancellable true
    //
    // @Triggers when an unknown command is processed by the server.
    //
    // @Context
    // <context.message> returns an ElementTag of the message to be shown to the command sender.
    // <context.command> returns the command name as an Element.
    // <context.raw_args> returns any args used as an Element.
    // <context.args> returns a ListTag of the arguments.
    // <context.source_type> returns the source of the command. Can be: PLAYER, SERVER, COMMAND_BLOCK, or COMMAND_MINECART.
    // <context.command_block_location> returns the command block's location (if the command was run from one).
    // <context.command_minecart> returns the EntityTag of the command minecart (if the command was run from one).
    //
    // @Determine
    // ElementTag to change the message returned to the command sender.
    // "NONE" to cancel the message.
    //
    // @Player when source_type is player.
    //
    // -->

    public UnknownCommandScriptEvent() {
        registerCouldMatcher("command unknown");
        this.<UnknownCommandScriptEvent, ElementTag>registerDetermination(null, ElementTag.class, (evt, context, text) -> {
            evt.event.message(PaperModule.parseFormattedText(text.toString(), ChatColor.WHITE));
        });
        this.<UnknownCommandScriptEvent>registerTextDetermination("none", (evt) -> {
            evt.event.message(null);
        });
    }

    public UnknownCommandEvent event;
    public String command;
    public String rawArgs;
    public String sourceType;

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getSender() instanceof Player player ? new PlayerTag(player) : null, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "command" -> new ElementTag(command, true);
            case "raw_args" -> new ElementTag(rawArgs, true);
            case "args" -> new ListTag(Arrays.asList(ArgumentHelper.buildArgs(rawArgs, false)), true);
            case "server" -> new ElementTag(sourceType.equals("server"));
            case "source_type" -> new ElementTag(sourceType, true);
            case "command_block_location" -> sourceType.equals("command_block") ? new LocationTag(((BlockCommandSender) event.getSender()).getBlock().getLocation()) : null;
            case "command_minecart" -> sourceType.equals("command_minecart") ? new EntityTag((CommandMinecart) event.getSender()) : null;
            case "message" -> new ElementTag(PaperModule.stringifyComponent(event.message()), true);
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void unknownCommandEvent(UnknownCommandEvent event) {
        this.event = event;
        String[] splitCommand = event.getCommandLine().split(" ", 2);
        this.command = splitCommand[0];
        this.rawArgs = splitCommand.length > 1 ? splitCommand[1] : "";
        if (event.getSender() instanceof Player) {
            this.sourceType = "player";
        }
        else if (event.getSender() instanceof BlockCommandSender) {
            this.sourceType = "command_block";
        }
        else if (event.getSender() instanceof CommandMinecart) {
            this.sourceType = "command_minecart";
        }
        else {
            this.sourceType = "server";
        }
        fire(event);
    }
}

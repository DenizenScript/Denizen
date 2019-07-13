package com.denizenscript.denizen.events.core;

import com.denizenscript.denizen.objects.dCuboid;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizen.objects.dWorld;
import com.denizenscript.denizen.scripts.containers.core.BukkitWorldScriptHelper;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.events.OldSmartEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CommandSmartEvent implements OldSmartEvent, Listener {

    class CommandHandlerData {
        public final String name;
        public final String event;

        public CommandHandlerData(String name, String event) {
            this.name = name;
            this.event = event;
        }
    }

    List<CommandHandlerData> cmds = new ArrayList<>();


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    @Override
    public boolean shouldInitialize(Set<String> events) {

        cmds = new ArrayList<>();
        // Loop through event names from loaded world script events
        boolean pass = false;
        for (String event : events) {

            // Use a regex pattern to narrow down matches
            Matcher m = Pattern.compile("on( ([^\\s]+))? command(in \\w+)?", Pattern.CASE_INSENSITIVE)
                    .matcher(event);

            if (m.matches()) {
                String cmd = m.group(2);
                if (cmd != null) {
                    ListTag split = ListTag.valueOf(cmd);
                    for (String str : split) {
                        cmds.add(new CommandHandlerData(CoreUtilities.toLowerCase(str), cmd));
                    }
                }
                pass = true;
            }
        }
        // No matches at all, so return false.
        return pass;
    }


    @Override
    public void _initialize() {
        // Yay! Your event is in use! Register it here.
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
        // Record that you loaded in the debug.
        Debug.log("Loaded Command SmartEvent.");
    }


    @Override
    public void breakDown() {
        // Unregister events or any other temporary links your event created in _intialize()
        PlayerCommandPreprocessEvent.getHandlerList().unregister(this);
        ServerCommandEvent.getHandlerList().unregister(this);
    }

    //////////////
    //  MECHANICS
    ///////////

    private List<String> getAll(String cmd) {
        List<String> newEvents = new ArrayList<>();
        cmd = CoreUtilities.toLowerCase(cmd);
        for (CommandHandlerData chd : cmds) {
            if (chd.name.equalsIgnoreCase(cmd)) {
                newEvents.add(chd.event + " command");
            }
        }
        return newEvents;
    }

    // <--[event]
    // @Events
    // command
    // <command_name>|... command
    //
    // @Regex ^on( [^\s]+)? command$
    // @Switch in <area>
    //
    // @Triggers when a player, console, or command block/minecart runs a Bukkit command. This happens before
    // any code of established commands allowing scripters to 'override' existing commands.
    // @Context
    // <context.command> returns the command name as an Element.
    // <context.raw_args> returns any args used as an Element.
    // <context.args> returns a ListTag of the arguments.
    // <context.server> returns true if the command was run from the console.
    // <context.command_block_location> returns the command block's location (if the command was run from one).
    // <context.command_minecart> returns the dEntity of the command minecart (if the command was run from one).
    //
    // @Determine
    // "FULFILLED" to tell Bukkit the command was handled.
    //
    // -->
    @EventHandler
    public void playerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Map<String, ObjectTag> context = new HashMap<>();

        String message = event.getMessage();
        String command = message.split(" ")[0].replace("/", "").toUpperCase();

        List<String> events = new ArrayList<>();

        events.add("command");
        events.add(command + " command");
        events.addAll(getAll(command));

        // Look for cuboids that contain the block's location
        List<dCuboid> cuboids = dCuboid.getNotableCuboidsContaining(event.getPlayer().getLocation());

        ListTag cuboid_context = new ListTag();
        List<String> cuboidEvents = new ArrayList<>();
        for (dCuboid cuboid : cuboids) {
            for (String str : events) {
                cuboidEvents.add(str + " in " + cuboid.identifySimple());
            }
            cuboid_context.add(cuboid.identifySimple());
        }
        for (String str : events) {
            cuboidEvents.add(str + " in " + new dWorld(event.getPlayer().getLocation().getWorld()).identifySimple());
        }
        events.addAll(cuboidEvents);
        // Add in cuboids context, with either the cuboids or an empty list
        context.put("cuboids", cuboid_context);

        List<String> args = Arrays.asList(ArgumentHelper.buildArgs(message.split(" ").length > 1 ? message.split(" ", 2)[1] : ""));

        // Fill context
        context.put("args", new ListTag(args));
        context.put("parsed_args", new ListTag(args));
        context.put("command", new ElementTag(command));
        context.put("raw_args", new ElementTag((message.split(" ").length > 1
                ? message.split(" ", 2)[1] : "")));
        context.put("server", new ElementTag(false));
        String determination;

        // Run any event scripts and get the determination.
        determination = BukkitWorldScriptHelper.doEvents(events,
                null, dEntity.getPlayerFrom(event.getPlayer()), context, true).toUpperCase();

        // If a script has determined fulfilled, cancel this event so the player doesn't
        // receive the default 'Invalid command' gibberish from bukkit.
        if (determination.equals("FULFILLED") || determination.equals("CANCELLED")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void serverCommand(ServerCommandEvent event) {

        if (event.getCommand().trim().length() == 0) {
            return;
        }

        Map<String, ObjectTag> context = new HashMap<>();

        String message = event.getCommand();
        String command = event.getCommand().split(" ")[0].replace("/", "").toUpperCase();

        List<String> events = new ArrayList<>();
        events.add("command");
        events.add(command + " command");
        events.addAll(getAll(command));

        List<String> args = Arrays.asList(ArgumentHelper.buildArgs(message.split(" ").length > 1 ? message.split(" ", 2)[1] : ""));
        List<String> parsed_args = Arrays.asList(ArgumentHelper.buildArgs(event.getCommand().split(" ").length > 1 ? event.getCommand().split(" ", 2)[1] : ""));

        // Fill context
        context.put("args", new ListTag(args));
        context.put("parsed_args", new ListTag(parsed_args));
        context.put("command", new ElementTag(command));
        context.put("raw_args", new ElementTag((message.split(" ").length > 1 ? event.getCommand().split(" ", 2)[1] : "")));
        context.put("server", new ElementTag(true));

        CommandSender sender = event.getSender();
        if (sender instanceof BlockCommandSender) {
            context.put("command_block_location", new dLocation(((BlockCommandSender) sender).getBlock().getLocation()));
        }
        else if (sender instanceof CommandMinecart) {
            context.put("command_minecart", new dEntity((CommandMinecart) sender));
        }

        String determination = BukkitWorldScriptHelper.doEvents(events, null, null, context);

        if (determination.equalsIgnoreCase("FULFILLED") || determination.equalsIgnoreCase("CANCELLED")) {
            event.setCommand("denizen do_nothing");
        }
    }
}

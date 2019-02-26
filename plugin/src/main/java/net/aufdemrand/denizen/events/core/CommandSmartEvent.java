package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.objects.dCuboid;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dWorld;
import net.aufdemrand.denizen.scripts.containers.core.BukkitWorldScriptHelper;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.events.OldSmartEvent;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
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

    List<CommandHandlerData> cmds = new ArrayList<CommandHandlerData>();


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    @Override
    public boolean shouldInitialize(Set<String> events) {

        cmds = new ArrayList<CommandHandlerData>();
        // Loop through event names from loaded world script events
        boolean pass = false;
        for (String event : events) {

            // Use a regex pattern to narrow down matches
            Matcher m = Pattern.compile("on( ([^\\s]+))? command(in \\w+)?", Pattern.CASE_INSENSITIVE)
                    .matcher(event);

            if (m.matches()) {
                String cmd = m.group(2);
                if (cmd != null) {
                    dList split = dList.valueOf(cmd);
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
        dB.log("Loaded Command SmartEvent.");
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
        List<String> newEvents = new ArrayList<String>();
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
    // <command_name>|... command (in <area>)
    //
    // @Regex ^on( [^\s]+)? command( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Triggers when a player, console, or command block/minecart runs a Bukkit command. This happens before
    // any code of established commands allowing scripters to 'override' existing commands.
    // @Context
    // <context.command> returns the command name as an Element.
    // <context.raw_args> returns any args used as an Element.
    // <context.args> returns a dList of the arguments.
    // <context.server> returns true if the command was run from the console.
    // <context.command_block_location> returns the command block's location (if the command was run from one).
    // <context.command_minecart> returns the dEntity of the command minecart (if the command was run from one).
    // <context.cuboids> DEPRECATED.
    //
    // @Determine
    // "FULFILLED" to tell Bukkit the command was handled.
    //
    // -->
    @EventHandler
    public void playerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Map<String, dObject> context = new HashMap<String, dObject>();

        String message = event.getMessage();
        String command = message.split(" ")[0].replace("/", "").toUpperCase();

        List<String> events = new ArrayList<String>();

        events.add("command");
        events.add(command + " command");
        events.addAll(getAll(command));

        // Look for cuboids that contain the block's location
        List<dCuboid> cuboids = dCuboid.getNotableCuboidsContaining(event.getPlayer().getLocation());

        dList cuboid_context = new dList();
        List<String> cuboidEvents = new ArrayList<String>();
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

        List<String> args = Arrays.asList(aH.buildArgs(message.split(" ").length > 1 ? message.split(" ", 2)[1] : ""));

        // Fill context
        context.put("args", new dList(args));
        context.put("parsed_args", new dList(args));
        context.put("command", new Element(command));
        context.put("raw_args", new Element((message.split(" ").length > 1
                ? message.split(" ", 2)[1] : "")));
        context.put("server", Element.FALSE);
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

        Map<String, dObject> context = new HashMap<String, dObject>();

        String message = event.getCommand();
        String command = event.getCommand().split(" ")[0].replace("/", "").toUpperCase();

        List<String> events = new ArrayList<String>();
        events.add("command");
        events.add(command + " command");
        events.addAll(getAll(command));

        List<String> args = Arrays.asList(aH.buildArgs(message.split(" ").length > 1 ? message.split(" ", 2)[1] : ""));
        List<String> parsed_args = Arrays.asList(aH.buildArgs(event.getCommand().split(" ").length > 1 ? event.getCommand().split(" ", 2)[1] : ""));

        // Fill context
        context.put("args", new dList(args));
        context.put("parsed_args", new dList(parsed_args));
        context.put("command", new Element(command));
        context.put("raw_args", new Element((message.split(" ").length > 1 ? event.getCommand().split(" ", 2)[1] : "")));
        context.put("server", Element.TRUE);

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

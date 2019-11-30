package com.denizenscript.denizen.utilities.command;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.notable.NotableManager;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.command.manager.Command;
import com.denizenscript.denizen.utilities.command.manager.CommandContext;
import com.denizenscript.denizen.utilities.command.manager.Paginator;
import com.denizenscript.denizen.utilities.command.manager.exceptions.CommandException;
import com.denizenscript.denizen.utilities.command.manager.messaging.Messaging;
import com.denizenscript.denizen.utilities.debugging.DebugSubmit;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.scripts.ScriptHelper;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.debugging.FutureWarning;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Set;


public class DenizenCommandHandler {

    private final Denizen denizen;

    public DenizenCommandHandler(Denizen denizen) {
        this.denizen = denizen;
    }

    // <--[language]
    // @name denizen permissions
    // @group Console Commands
    // @description
    // The following is a list of all permission nodes Denizen uses within Bukkit.
    //
    // denizen.basic         # use the basics of the /denizen command
    // denizen.notable       # use the /notable command
    // denizen.notable.basic # functionality within the /notable command, such as add or list
    // denizen.ex            # use the /ex command
    // denizen.debug         # use the /denizen debug command
    // denizen.submit        # use the /denizen submit command
    //
    // Additionally:
    // denizen.npc.health, denizen.npc.sneak,
    // denizen.npc.effect, denizen.npc.fish, denizen.npc.sleep, denizen.npc.stand,
    // denizen.npc.sit, denizen.npc.nameplate, denizen.npc.nickname, denizen.npc.trigger,
    // denizen.npc.assign, denizen.npc.constants, denizen.npc.pushable
    //
    // However, we recommend just giving op to whoever needs to access Denizen - they can
    // op themselves through Denizen anyway, why not save the trouble?
    // ( EG, /ex execute as_server "op <player.name>" )
    //
    // -->

    // <--[language]
    // @name /denizen submit command
    // @group Console Commands
    // @description
    // Use the '/denizen submit' command with '/denizen debug -r' to record debug output and post
    // it online for assisting developers to see.
    //
    // To begin recording, simply use '/denizen debug -r'. After that, any debug output sent to the
    // console and any player chat will be added to an internal record. Once enabled, you should then
    // fire off scripts and events that aren't working fully. Finally, you use the '/denizen submit'
    // command to take all the recording information and paste it to an online pastebin hosted by
    // the Denizen team. It will give you back a direct link to the full debug output, which you
    // can view yourself and send to other helpers without trouble.
    //
    // There is no limit to the recording size, to prevent any important information from being trimmed
    // away. Be careful not to leave debug recording enabled by accident, as it may eventually begin
    // using up large amounts of memory. (The submit command will automatically disable recording,
    // or you can instead just use '/denizen debug -r' again.)
    //
    // -->
    @Command(
            aliases = {"denizen"}, usage = "submit",
            desc = "Submits recorded logs triggered by /denizen debug -r", modifiers = {"submit"},
            min = 1, max = 3, permission = "denizen.submit")
    public void submit(CommandContext args, final CommandSender sender) throws CommandException {
        if (!Debug.record) {
            Messaging.sendError(sender, "Use /denizen debug -r  to record debug information to be submitted");
            return;
        }
        Debug.record = false;
        Messaging.send(sender, "Submitting...");
        final DebugSubmit submit = new DebugSubmit();
        submit.recording = Debug.Recording.toString();
        Debug.Recording = new StringBuilder();
        submit.start();
        BukkitRunnable task = new BukkitRunnable() {
            public void run() {
                if (!submit.isAlive()) {
                    if (submit.Result == null) {
                        Messaging.sendError(sender, "Error while submitting.");
                    }
                    else {
                        Messaging.send(sender, "Successfully submitted to https://one.denizenscript.com" + submit.Result);
                    }
                    this.cancel();
                }
            }
        };
        task.runTaskTimer(DenizenAPI.getCurrentInstance(), 0, 10);
    }

    // <--[language]
    // @name /denizen debug command
    // @group Console Commands
    // @description
    // Using the /denizen debug command interfaces with Denizen's debugger to allow control
    // over debug messages.
    //
    // To enable debugging mode, simply type '/denizen debug'. While debug is enabled, all debuggable
    // scripts, and any invoked actions, will output information to the console as they are executed.
    // By default, all scripts are debuggable while the debugger is enabled. To disable a script
    // specifically from debugging, simply add the 'debug:' node with a value of 'false' to your script
    // container. This is typically used to silence particularly spammy scripts. Any kind of script
    // container can be silenced using this method.
    //
    // To stop debugging, simply type the '/denizen debug' command again. This must be used without
    // any additional options. A message will be sent to show the current status of the debugger.
    // Note: While your server is in 'live production mode', the debugger should be disabled as your
    // server will run slower while outputting debug information.
    //
    // There are also several options to further help debugging. To use an option, simply attach them
    // to the /denizen debug command. One option, or multiple options can be used. For example: /denizen debug -ce
    //
    // '-c' enables/disables color. This is sometimes useful when debugging with a non-color console.
    // '-r' enables recording mode. See also: /denizen submit command
    // '-e' enables/disables world event timings. While enabled, the debugger will show all triggered events.
    // '-s' enables/disables stacktraces generated by Denizen. We might ask you to enable this when problems arise.
    // '-b' enables/disables the ScriptBuilder debug. When enabled, Denizen will show info on script and argument creation.
    //      Warning: Can be spammy.
    // '-n' enables/disables debug trimming. When enabled, messages longer than 512 characters will be 'snipped'.
    // '-i' enables/disables source information. When enabled, debug will show where it came from (when possible).
    // '-p' enables/disables packet debug logging. When enabled, all packets sent to players (from anywhere) will be logged to console.
    // '-f' enables/disables showing of future warnings. When enabled, future warnings (such as upcoming deprecations) will be displayed in console logs.
    // '-v' enables/disables advanced verbose log output. This will *flood* your console super hard.
    // '-o' enables/disables 'override' mode. This will display all script debug, even when 'debug: false' is set for scripts.
    // '-l' enables/disables script loading information. When enabled, '/ex reload' will produce a potentially large amount of debug output.
    // '-x' clears the debug filter (see below).
    //
    // The debugger also allows the targeting of specific scripts by using the '--filter script_name' argument. For
    // example: /denizen debug --filter 'my script|my other script' will instruct the debugger to only debug the
    // scripts named 'my script' and 'my other script'. Multiple scripts should be separated by a pipe character (|).
    // The --filter argument is cumulative, that is, scripts specified are added to the filter. To add more scripts,
    // simply use the command again. To clear the filter, use the -x option. Example: /denizen debug -x
    //
    // -->

    /*
     * DENIZEN DEBUG
     */
    @Command(
            aliases = {"denizen"}, usage = "debug",
            desc = "Toggles debug mode for Denizen.", modifiers = {"debug", "de", "db", "dbug"},
            min = 1, max = 5, permission = "denizen.debug", flags = "scebrxovnipfl")
    public void debug(CommandContext args, CommandSender sender) throws CommandException {
        if (args.hasFlag('s')) {
            if (!Debug.showDebug) {
                Debug.toggle();
            }
            Debug.showStackTraces = !Debug.showStackTraces;
            Messaging.sendInfo(sender, (Debug.showStackTraces ? "Denizen debugger is now showing caught " +
                    "exception stack traces." : "Denizen debugger is now hiding caught stacktraces."));
        }
        if (args.hasFlag('c')) {
            if (!Debug.showDebug) {
                Debug.toggle();
            }
            Debug.showColor = !Debug.showColor;
            Messaging.sendInfo(sender, (Debug.showColor ? "Denizen debugger is now showing color."
                    : "Denizen debugger color has been disabled."));
        }
        if (args.hasFlag('o')) {
            if (!Debug.showDebug) {
                Debug.toggle();
            }
            Debug.debugOverride = !Debug.debugOverride;
            Messaging.sendInfo(sender, (Debug.debugOverride ? "Denizen debugger is now overriding 'debug: false'."
                    : "Denizen debugger override has been disabled."));
        }
        if (args.hasFlag('e')) {
            if (!Debug.showDebug) {
                Debug.toggle();
            }
            com.denizenscript.denizencore.utilities.debugging.Debug.showEventsTrimming = !com.denizenscript.denizencore.utilities.debugging.Debug.showEventsTrimming;
            Messaging.sendInfo(sender, (com.denizenscript.denizencore.utilities.debugging.Debug.showEventsTrimming ? "Denizen debugger is now logging all " +
                    "world events." : "Denizen debugger is now hiding world events."));
        }
        if (args.hasFlag('b')) {
            if (!Debug.showDebug) {
                Debug.toggle();
            }
            com.denizenscript.denizencore.utilities.debugging.Debug.showScriptBuilder = !com.denizenscript.denizencore.utilities.debugging.Debug.showScriptBuilder;
            Messaging.sendInfo(sender, (com.denizenscript.denizencore.utilities.debugging.Debug.showScriptBuilder ? "Denizen debugger is now logging the " +
                    "ScriptBuilder." : "Denizen debugger is now hiding ScriptBuilder logging."));
        }
        if (args.hasFlag('r')) {
            if (!Debug.showDebug) {
                Debug.toggle();
            }
            Debug.record = !Debug.record;
            Debug.Recording = new StringBuilder();
            Messaging.sendInfo(sender, (Debug.record ? "Denizen debugger is now recording. Use /denizen " +
                    "submit to finish." : "Denizen debugger recording disabled."));
        }
        if (args.hasFlag('v')) {
            if (!Debug.showDebug) {
                Debug.toggle();
            }
            com.denizenscript.denizencore.utilities.debugging.Debug.verbose = !com.denizenscript.denizencore.utilities.debugging.Debug.verbose;
            Messaging.sendInfo(sender, (com.denizenscript.denizencore.utilities.debugging.Debug.verbose ? "Denizen debugger is now verbose." :
                    "Denizen debugger verbosity disabled."));
        }
        if (args.hasFlag('f')) {
            if (!Debug.showDebug) {
                Debug.toggle();
            }
            FutureWarning.futureWarningsEnabled = !FutureWarning.futureWarningsEnabled;
            Messaging.sendInfo(sender, (FutureWarning.futureWarningsEnabled ? "Denizen debugger is now showing future warnings." :
                    "Denizen debugger future-warnings disabled."));
        }
        if (args.hasFlag('x')) {
            Debug.filter = new ArrayList<>();
            Messaging.sendInfo(sender, "Denizen debugger filter removed.");
        }
        if (args.hasFlag('n')) {
            if (!Debug.showDebug) {
                Debug.toggle();
            }
            Debug.shouldTrim = !Debug.shouldTrim;
            Messaging.sendInfo(sender, (Debug.shouldTrim ? "Denizen debugger is now trimming long messages."
                    : "Denizen debugger is no longer trimming long messages."));
        }
        if (args.hasFlag('i')) {
            if (!Debug.showDebug) {
                Debug.toggle();
            }
            Debug.showSources = !Debug.showSources;
            Messaging.sendInfo(sender, (NMSHandler.debugPackets ? "Denizen debugger is now showing source information."
                    : "Denizen debugger is no longer showing source information."));
        }
        if (args.hasFlag('p')) {
            if (!Debug.showDebug) {
                Debug.toggle();
            }
            NMSHandler.debugPackets = !NMSHandler.debugPackets;
            Messaging.sendInfo(sender, (Debug.showSources ? "Denizen debugger is now showing packet logs."
                    : "Denizen debugger is no longer showing packet logs."));
        }
        if (args.hasFlag('l')) {
            if (!Debug.showDebug) {
                Debug.toggle();
            }
            com.denizenscript.denizencore.utilities.debugging.Debug.showLoading = !com.denizenscript.denizencore.utilities.debugging.Debug.showLoading;
            Messaging.sendInfo(sender, (com.denizenscript.denizencore.utilities.debugging.Debug.showLoading ? "Denizen debugger is now showing script loading information."
                    : "Denizen debugger is no longer showing script loading information."));
        }
        if (args.hasValueFlag("filter")) {
            if (!Debug.showDebug) {
                Debug.toggle();
            }
            for (String filter : args.getFlag("filter").split("\\|")) { // TODO: addAll?
                Debug.filter.add(filter);
            }
            Messaging.sendInfo(sender, "Denizen debugger filter now: " + Debug.filter.toString());

        }
        else if (args.getFlags().isEmpty()) {
            Debug.toggle();
            Messaging.sendInfo(sender, "Denizen debugger is now: "
                    + (Debug.showDebug ? "<a>ENABLED" : "<c>DISABLED"));
        }

    }

    /*
     * DENIZEN DO_NOTHING
     */
    @Command(
            aliases = {"denizen"}, usage = "do_nothing",
            desc = "Does nothing, for better server command handling", modifiers = {"do_nothing"},
            min = 1, max = 3, permission = "denizen.basic")
    public void do_nothing(CommandContext args, CommandSender sender) throws CommandException {
        // Do nothing
    }

    /*
     * DENIZEN VERSION
     */
    @Command(
            aliases = {"denizen"}, usage = "version",
            desc = "Shows the currently loaded version of Denizen.", modifiers = {"version"},
            min = 1, max = 3, permission = "denizen.basic")
    public void version(CommandContext args, CommandSender sender) throws CommandException {
        Messaging.sendInfo(sender, "<2>DENIZEN<7>: scriptable Minecraft!"); // TODO: "It's Scriptable!"?
        Messaging.send(sender, "");
        Messaging.send(sender, "<7>by: <f>the DenizenScript team, with help from many skilled contributors!");
        Messaging.send(sender, "<7>chat with us at: <f> https://discord.gg/Q6pZGSR");
        Messaging.send(sender, "<7>or learn more at: <f> https://denizenscript.com");
        Messaging.send(sender, "<7>version: <f>" + Denizen.versionTag + "<7>, core version: <f>" + DenizenCore.VERSION);
    }


    /*
     * DENIZEN SAVE
     */
    @Command(
            aliases = {"denizen"}, usage = "save",
            desc = "Saves the current state of Denizen/saves.yml.", modifiers = {"save"},
            min = 1, max = 3, permission = "denizen.basic", flags = "s")
    public void save(CommandContext args, CommandSender sender) throws CommandException {

        DenizenAPI.getCurrentInstance().saveSaves();

        Messaging.send(sender, "Denizen/saves.yml saved to disk from memory.");
    }


    /*
     * DENIZEN RELOAD
     */
    @Command(aliases = {"denizen"}, usage = "reload (saves|notables|config|scripts) (-a)",
            desc = "Reloads various Denizen files from disk to memory.", modifiers = {"reload"},
            min = 1, max = 3, permission = "denizen.basic", flags = "a")
    public void reload(CommandContext args, CommandSender sender) throws CommandException {

        // Get reload type
        if (args.hasFlag('a')) {
            denizen.reloadConfig();
            DenizenCore.reloadScripts();
            denizen.notableManager().reloadNotables();
            denizen.reloadSaves();
            Messaging.send(sender, "Denizen/saves.yml, Denizen/notables.yml, Denizen/config.yml, and Denizen/scripts/... reloaded from disk to memory.");
            if (ScriptHelper.hadError()) {
                Messaging.sendError(sender, "There was an error loading your scripts, check the console for details!");
            }
            return;
        }
        // Reload a specific item
        if (args.length() > 2) {
            if (args.getString(1).equalsIgnoreCase("saves")) {
                denizen.reloadSaves();
                Messaging.send(sender, "Denizen/saves.yml reloaded from disk to memory.");
                return;
            }
            else if (args.getString(1).equalsIgnoreCase("notables")) {
                denizen.notableManager().reloadNotables();
                Messaging.send(sender, "Denizen/notables.yml reloaded from disk to memory.");
                return;
            }
            else if (args.getString(1).equalsIgnoreCase("config")) {
                denizen.reloadConfig();
                Messaging.send(sender, "Denizen/config.yml reloaded from disk to memory.");
                return;
            }
            else if (args.getString(1).equalsIgnoreCase("scripts")) {
                DenizenCore.reloadScripts();
                Messaging.send(sender, "Denizen/scripts/... reloaded from disk to memory.");
                if (ScriptHelper.hadError()) {
                    Messaging.sendError(sender, "There was an error loading your scripts, check the console for details!");
                }
                Messaging.sendError(sender, "'/denizen reload scripts' is the old way of doing things ... use '/ex reload' instead!");
                return;
            }
        }

        Messaging.send(sender, "");
        Messaging.send(sender, "<f>Specify which parts to reload. Valid options are: SAVES, NOTABLES, CONFIG, SCRIPTS");
        Messaging.send(sender, "<b>Example: /denizen reload scripts");
        Messaging.send(sender, "<f>Use '-a' to reload all parts.");
        Messaging.send(sender, "");

    }


    /*
     * DENIZEN SCRIPTS
     */
    @Command(
            aliases = {"denizen"}, usage = "scripts (--type assignment|task|...) (--filter string)",
            desc = "Lists currently loaded dScripts.", modifiers = {"scripts"},
            min = 1, max = 4, permission = "denizen.basic")
    public void scripts(CommandContext args, CommandSender sender) throws CommandException {
        // Fill arguments
        String type = null;
        if (args.hasValueFlag("type")) {
            type = args.getFlag("type");
        }
        String filter = null;
        if (args.hasValueFlag("filter")) {
            filter = args.getFlag("filter");
        }
        // Get script names from the scripts.yml in memory
        Set<String> scripts = ScriptRegistry.scriptContainers.keySet();
        // New Paginator to display script names
        Paginator paginator = new Paginator().header("Scripts");
        paginator.addLine("<e>Key: <a>Type  <b>Name");
        // Add scripts to Paginator
        for (String script : scripts) {
            ScriptContainer scriptContainer = ScriptRegistry.getScriptContainer(script);
            // If a --type has been specified...
            if (type != null) {
                if (scriptContainer.getContainerType().equalsIgnoreCase(type)) {
                    if (filter != null) {
                        if (script.contains(filter.toUpperCase())) {
                            paginator.addLine("<a>" + scriptContainer.getContainerType().substring(0, 3) + "  <b>" + script);
                        }
                    }
                    else {
                        paginator.addLine("<a>" + scriptContainer.getContainerType().substring(0, 3) + "  <b>" + script);
                    }
                }
                // If a --filter has been specified...
            }
            else if (filter != null) {
                if (script.contains(filter.toUpperCase())) {
                    paginator.addLine("<a>" + scriptContainer.getContainerType().substring(0, 3) + "  <b>" + script);
                }
            }
            else {
                paginator.addLine("<a>" + scriptContainer.getContainerType().substring(0, 3) + "  <b>" + script);
            }
        }
        // Send the contents of the Paginator to the Player (or Console)
        if (!paginator.sendPage(sender, args.getInteger(1, 1))) {
            throw new CommandException("The page " + args.getInteger(1, 1) + " does not exist.");
        }
    }

    @Command(
            aliases = {"notable"}, usage = "add",
            desc = "Adds a new notable to your current location", modifiers = {"add", "save"},
            // Even though different arguments will be combined into one
            // if they are delimited by quotes, their max number is checked
            // before that, so it needs to be high
            min = 2, max = 20, permission = "denizen.notable.basic")
    public void addnotable(CommandContext args, CommandSender sender) throws CommandException {

        NotableManager.saveAs(new LocationTag(((Player) sender).getLocation()), args.getString(1));
        Messaging.send(sender, "Created new notable called " + (args.getString(1)));
    }

    @Command(
            aliases = {"notable"}, usage = "list",
            desc = "Lists all notable locations", modifiers = {"list"},
            min = 1, max = 1, permission = "denizen.notable.basic")
    public void listnotable(CommandContext args, CommandSender sender) throws CommandException {

        Messaging.send(sender, NotableManager.getAllType(LocationTag.class).toString());
    }
}

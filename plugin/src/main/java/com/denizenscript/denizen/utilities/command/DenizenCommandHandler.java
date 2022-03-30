package com.denizenscript.denizen.utilities.command;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.utilities.command.manager.Command;
import com.denizenscript.denizen.utilities.command.manager.CommandContext;
import com.denizenscript.denizen.utilities.command.manager.Paginator;
import com.denizenscript.denizen.utilities.command.manager.exceptions.CommandException;
import com.denizenscript.denizen.utilities.command.manager.messaging.Messaging;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.flags.PlayerFlagHandler;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.objects.notable.NoteManager;
import com.denizenscript.denizencore.scripts.ScriptHelper;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.FutureWarning;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class DenizenCommandHandler {

    public DenizenCommandHandler() {
    }

    // <--[language]
    // @name denizen permissions
    // @group Console Commands
    // @description
    // The following is a list of all permission nodes Denizen uses within Bukkit.
    //
    // denizen.clickable     # use the 'denizenclickable' command, which is automatically executed when using <@link command clickable>
    // denizen.basic         # use the basics of the /denizen command
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
    // However, we recommend just giving op to whoever needs to access Denizen - they can op themselves through Denizen anyway, why not save the trouble?
    // ( EG, /ex execute as_server "op <player.name>" )
    //
    // -->

    // <--[language]
    // @name /denizen submit command
    // @group Console Commands
    // @description
    // Use the '/denizen submit' command with '/denizen debug -r' to record debug output and post it online for assisting developers to see.
    //
    // To begin recording, simply use '/denizen debug -r'.
    // After that, any debug output sent to the console and any player chat will be added to an internal record.
    // Once enabled, you should then fire off scripts and events that aren't working fully.
    // Finally, you use the '/denizen submit' command to take all the recording information and paste it to an online pastebin hosted by the Denizen team.
    // It will give you back a direct link to the full debug output, which you can view yourself and send to other helpers without trouble.
    //
    // There is no limit to the recording size, to prevent any important information from being trimmed away.
    // Be careful not to leave debug recording enabled by accident, as it may eventually begin using up large amounts of memory.
    // (The submit command will automatically disable recording, or you can instead just use '/denizen debug -r' again.)
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
        Messaging.send(sender, "Submitting...");
        DenizenCore.implementation.submitRecording((s) -> {
            if (s == null) {
                Messaging.sendError(sender, "Error while submitting.");
            }
            else {
                Messaging.send(sender, "Successfully submitted to " + s);
            }
        });
    }

    // <--[language]
    // @name /denizen debug command
    // @group Console Commands
    // @description
    // Using the /denizen debug command interfaces with Denizen's debugger to allow control over debug messages.
    //
    // To enable debugging mode, simply type '/denizen debug'.
    // While debug is enabled, all debuggable scripts, and any invoked actions, will output information to the console as they are executed.
    // By default, all scripts are debuggable while the debugger is enabled.
    // To disable a script specifically from debugging, simply add the 'debug:' node with a value of 'false' to your script container.
    // This is typically used to silence particularly spammy scripts. Any kind of script container can be silenced using this method.
    //
    // To stop debugging, simply type the '/denizen debug' command again.
    // This must be used without any additional options. A message will be sent to show the current status of the debugger.
    //
    // Note: you should almost NEVER disable debug entirely. Instead, always disable it on a per-script basis.
    // If debug is globally disabled, that will hide important error messages, not just normal debug output.
    //
    // There are also several options to further help debugging. To use an option, simply attach them to the /denizen debug command.
    // One option, or multiple options can be used. For example: /denizen debug -sbi
    //
    // '-c' enables/disables color. This is sometimes useful when debugging with a non-color console.
    // '-r' enables recording mode. See also: /denizen submit command
    // '-s' enables/disables stacktraces generated by Denizen. We might ask you to enable this when problems arise.
    // '-b' enables/disables the ScriptBuilder debug. When enabled, Denizen will show info on script and argument creation. Warning: Can be spammy.
    // '-n' enables/disables debug trimming. When enabled, messages longer than 1024 characters will be 'snipped'.
    // '-i' enables/disables source information. When enabled, debug will show where it came from (when possible).
    // '-p' enables/disables packet debug logging. When enabled, all packets sent to players (from anywhere) will be logged to console.
    // or, '--pfilter (filter)' to enable packet debug logging with a string contain filter.
    // '-f' enables/disables showing of future warnings. When enabled, future warnings (such as upcoming deprecations) will be displayed in console logs.
    // '-v' enables/disables advanced verbose log output. This will *flood* your console super hard.
    // '-o' enables/disables 'override' mode. This will display all script debug, even when 'debug: false' is set for scripts.
    // '-l' enables/disables script loading information. When enabled, '/ex reload' will produce a potentially large amount of debug output.
    //
    // -->

    /*
     * DENIZEN DEBUG
     */
    @Command(
            aliases = {"denizen"}, usage = "debug",
            desc = "Toggles debug mode for Denizen.", modifiers = {"debug", "de", "db", "dbug"},
            min = 1, max = 5, permission = "denizen.debug", flags = "scbrovnipfl")
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
            Debug.recording = new StringBuilder();
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
            Messaging.sendInfo(sender, (Debug.showSources ? "Denizen debugger is now showing source information."
                    : "Denizen debugger is no longer showing source information."));
        }
        if (args.hasFlag('p')) {
            if (!Debug.showDebug) {
                Debug.toggle();
            }
            NetworkInterceptHelper.enable();
            NMSHandler.debugPackets = !NMSHandler.debugPackets;
            NMSHandler.debugPacketFilter = "";
            Messaging.sendInfo(sender, (NMSHandler.debugPackets ? "Denizen debugger is now showing unfiltered packet logs."
                    : "Denizen debugger is no longer showing packet logs."));
        }
        if (args.hasValueFlag("pfilter")) {
            if (!Debug.showDebug) {
                Debug.toggle();
            }
            NetworkInterceptHelper.enable();
            NMSHandler.debugPackets = true;
            NMSHandler.debugPacketFilter = CoreUtilities.toLowerCase(args.getFlag("pfilter"));
            Messaging.sendInfo(sender, "Denizen debug packet log now enabled and filtered.");
            return;
        }
        if (args.hasFlag('l')) {
            if (!Debug.showDebug) {
                Debug.toggle();
            }
            com.denizenscript.denizencore.utilities.debugging.Debug.showLoading = !com.denizenscript.denizencore.utilities.debugging.Debug.showLoading;
            Messaging.sendInfo(sender, (com.denizenscript.denizencore.utilities.debugging.Debug.showLoading ? "Denizen debugger is now showing script loading information."
                    : "Denizen debugger is no longer showing script loading information."));
        }
        if (args.getFlags().isEmpty()) {
            Debug.toggle();
            Messaging.sendInfo(sender, "Denizen debugger is now: " + (Debug.showDebug ? "<a>ENABLED" : "<c>DISABLED"));
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
            desc = "Saves the current Denizen save data to file as needed.", modifiers = {"save"},
            min = 1, max = 3, permission = "denizen.basic")
    public void save(CommandContext args, CommandSender sender) throws CommandException {
        DenizenCore.saveAll();
        Denizen.getInstance().saveSaves(true);
        Messaging.send(sender, "Denizen save data saved to disk from memory.");
    }

    /*
     * DENIZEN RELOAD
     */
    @Command(aliases = {"denizen"}, usage = "reload (saves|notes|config|scripts) (-a)",
            desc = "Reloads various Denizen files from disk to memory.", modifiers = {"reload"},
            min = 1, max = 3, permission = "denizen.basic", flags = "a")
    public void reload(CommandContext args, CommandSender sender) throws CommandException {
        if (args.hasFlag('a')) {
            Denizen.getInstance().reloadConfig();
            DenizenCore.reloadScripts();
            PlayerFlagHandler.reloadAllFlagsNow();
            NoteManager.reload();
            Denizen.getInstance().reloadSaves();
            Messaging.send(sender, "Denizen save data, config, and scripts reloaded from disk to memory.");
            if (ScriptHelper.hadError()) {
                Messaging.sendError(sender, "There was an error loading your scripts, check the console for details!");
            }
            return;
        }
        if (args.length() > 2) {
            if (args.getString(1).equalsIgnoreCase("saves")) {
                Denizen.getInstance().reloadSaves();
                PlayerFlagHandler.reloadAllFlagsNow();
                Messaging.send(sender, "Denizen save data reloaded from disk to memory.");
                return;
            }
            else if (args.getString(1).equalsIgnoreCase("notes")) {
                NoteManager.reload();
                Messaging.send(sender, "Denizen note data reloaded from disk to memory.");
                return;
            }
            else if (args.getString(1).equalsIgnoreCase("config")) {
                Denizen.getInstance().reloadConfig();
                Messaging.send(sender, "Denizen config file reloaded from disk to memory.");
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
        Messaging.send(sender, "<f>Specify which parts to reload. Valid options are: SAVES, NOTES, CONFIG, SCRIPTS");
        Messaging.send(sender, "<b>Example: /denizen reload scripts");
        Messaging.send(sender, "<f>Use '-a' to reload all parts.");
        Messaging.send(sender, "<f>Note that you shouldn't use this command generally, instead use '/ex reload' - see also the Beginner's Guide @ https://guide.denizenscript.com/");
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
        String type = null;
        if (args.hasValueFlag("type")) {
            type = args.getFlag("type");
        }
        String filter = null;
        if (args.hasValueFlag("filter")) {
            filter = args.getFlag("filter");
        }
        Set<String> scripts = ScriptRegistry.scriptContainers.keySet();
        Paginator paginator = new Paginator().header("Scripts");
        paginator.addLine("<e>Key: <a>Type  <b>Name");
        for (String script : scripts) {
            ScriptContainer scriptContainer = ScriptRegistry.getScriptContainer(script);
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
        if (!paginator.sendPage(sender, args.getInteger(1, 1))) {
            throw new CommandException("The page " + args.getInteger(1, 1) + " does not exist.");
        }
    }
}

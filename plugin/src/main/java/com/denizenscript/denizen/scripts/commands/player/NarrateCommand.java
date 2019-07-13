package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.scripts.containers.core.FormatScriptContainer;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.aH;
import com.denizenscript.denizencore.objects.dList;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.tags.TagManager;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.List;

public class NarrateCommand extends AbstractCommand {

    // <--[command]
    // @Name Narrate
    // @Syntax narrate [<text>] (targets:<player>|...) (format:<name>)
    // @Required 1
    // @Short Shows some text to the player.
    // @Group player
    //
    // @Description
    // Prints some text into the target's chat area. If no target is specified it will default to the attached player
    // or the console. Accepts the 'format:<name>' argument, which will reformat the text according to the specified
    // format script.
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to narrate text to the player.
    // - narrate "Hello World!"
    //
    // @Usage
    // Use to narrate text to a list of players.
    // - narrate "Hello there." targets:p@mcmonkey4eva|p@Morphan1|p@Fortifier42
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {


        if (scriptEntry.getArguments().size() > 4) { // TODO: Use this more often!
            throw new InvalidArgumentsException("Too many arguments! Did you forget a 'quote'?");
        }

        // Iterate through arguments
        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {
            if (!scriptEntry.hasObject("format")
                    && arg.matchesPrefix("format", "f")) {
                String formatStr = arg.getValue();
                FormatScriptContainer format = ScriptRegistry.getScriptContainer(formatStr);
                if (format == null) {
                    dB.echoError("Could not find format script matching '" + formatStr + '\'');
                }
                scriptEntry.addObject("format", format);
            }

            // Add players to target list
            else if (!scriptEntry.hasObject("targets")
                    && arg.matchesPrefix("target", "targets", "t")) {
                scriptEntry.addObject("targets", arg.asType(dList.class).filter(dPlayer.class, scriptEntry));
            }

            // Use raw_value as to not accidentally strip a value before any :'s.
            else if (!scriptEntry.hasObject("text")) {
                scriptEntry.addObject("text", new Element(TagManager.cleanOutputFully(arg.raw_value)));
            }
            else {
                arg.reportUnhandled();
            }

        }

        // If there are no targets, check if you can add this player
        // to the targets
        if (!scriptEntry.hasObject("targets")) {
            scriptEntry.addObject("targets",
                    (Utilities.entryHasPlayer(scriptEntry) ? Arrays.asList(Utilities.getEntryPlayer(scriptEntry)) : null));
        }

        if (!scriptEntry.hasObject("text")) {
            throw new InvalidArgumentsException("Missing any text!");
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) {
        // Get objects
        List<dPlayer> targets = (List<dPlayer>) scriptEntry.getObject("targets");
        String text = scriptEntry.getElement("text").asString();
        FormatScriptContainer format = (FormatScriptContainer) scriptEntry.getObject("format");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(),
                    aH.debugObj("Narrating", text)
                            + aH.debugList("Targets", targets)
                            + (format != null ? aH.debugObj("Format", format.getName()) : ""));
        }

        if (targets == null) {
            Bukkit.getServer().getConsoleSender().sendMessage(format != null ? format.getFormattedText(scriptEntry) : text);
            return;
        }

        for (dPlayer player : targets) {
            if (player != null && player.isOnline()) {
                player.getPlayerEntity().sendMessage(format != null ? format.getFormattedText(scriptEntry) : text);
            }
            else {
                dB.echoError("Narrated to non-existent or offline player!");
            }
        }
    }
}

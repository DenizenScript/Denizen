package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.scripts.containers.core.FormatScriptContainer;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ListTag;
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
    // @Syntax narrate [<text>] (targets:<player>|...) (format:<script>)
    // @Required 1
    // @Short Shows some text to the player.
    // @Group player
    //
    // @Description
    // Prints some text into the target's chat area. If no target is specified it will default to the attached player
    // or the console. Accepts the 'format:<script>' argument, which will reformat the text according to the specified
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
        for (Argument arg : scriptEntry.getProcessedArgs()) {
            if (!scriptEntry.hasObject("format")
                    && arg.matchesPrefix("format", "f")) {
                String formatStr = arg.getValue();
                FormatScriptContainer format = ScriptRegistry.getScriptContainer(formatStr);
                if (format == null) {
                    Debug.echoError("Could not find format script matching '" + formatStr + '\'');
                }
                scriptEntry.addObject("format", format);
            }

            // Add players to target list
            else if (!scriptEntry.hasObject("targets")
                    && arg.matchesPrefix("target", "targets", "t")) {
                scriptEntry.addObject("targets", arg.asType(ListTag.class).filter(PlayerTag.class, scriptEntry));
            }

            // Use raw_value as to not accidentally strip a value before any :'s.
            else if (!scriptEntry.hasObject("text")) {
                scriptEntry.addObject("text", new ElementTag(TagManager.cleanOutputFully(arg.raw_value)));
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
        List<PlayerTag> targets = (List<PlayerTag>) scriptEntry.getObject("targets");
        String text = scriptEntry.getElement("text").asString();
        FormatScriptContainer format = (FormatScriptContainer) scriptEntry.getObject("format");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(),
                    ArgumentHelper.debugObj("Narrating", text)
                            + ArgumentHelper.debugList("Targets", targets)
                            + (format != null ? ArgumentHelper.debugObj("Format", format.getName()) : ""));
        }

        if (targets == null) {
            Bukkit.getServer().getConsoleSender().sendMessage(format != null ? format.getFormattedText(scriptEntry) : text);
            return;
        }

        for (PlayerTag player : targets) {
            if (player != null && player.isOnline()) {
                player.getPlayerEntity().sendMessage(format != null ? format.getFormattedText(scriptEntry) : text);
            }
            else {
                Debug.echoError("Narrated to non-existent or offline player!");
            }
        }
    }
}

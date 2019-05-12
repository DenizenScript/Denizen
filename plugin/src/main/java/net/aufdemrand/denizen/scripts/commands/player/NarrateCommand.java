package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.containers.core.FormatScriptContainer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.ScriptRegistry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.tags.TagManager;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.List;

public class NarrateCommand extends AbstractCommand {

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
                    (((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer() ? Arrays.asList(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer()) : null));
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

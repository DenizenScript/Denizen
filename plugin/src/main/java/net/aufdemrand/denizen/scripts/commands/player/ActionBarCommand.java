package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.nms.NMSHandler;
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

import java.util.Arrays;
import java.util.List;

public class ActionBarCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matchesPrefix("format", "f")) {
                String formatStr = arg.getValue();
                FormatScriptContainer format = ScriptRegistry.getScriptContainer(formatStr);
                if (format == null) {
                    dB.echoError("Could not find format script matching '" + formatStr + '\'');
                }
                scriptEntry.addObject("format", format);
            }
            if (arg.matchesPrefix("targets", "target")
                    && arg.matchesArgumentList(dPlayer.class)) {
                scriptEntry.addObject("targets", arg.asType(dList.class).filter(dPlayer.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("text")) {
                scriptEntry.addObject("text", new Element(TagManager.cleanOutputFully(arg.raw_value)));
            }
        }

        if (!scriptEntry.hasObject("text")) {
            throw new InvalidArgumentsException("Must specify a message!");
        }

        if (!scriptEntry.hasObject("targets") && !((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer()) {
            throw new InvalidArgumentsException("Must specify target(s).");
        }

        if (!scriptEntry.hasObject("targets")) {
            BukkitScriptEntryData data = (BukkitScriptEntryData) scriptEntry.entryData;
            if (!data.hasPlayer()) {
                throw new InvalidArgumentsException("Must specify valid player Targets!");
            }
            else {
                scriptEntry.addObject("targets",
                        Arrays.asList(data.getPlayer()));
            }
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        Element text = scriptEntry.getElement("text");
        FormatScriptContainer format = (FormatScriptContainer) scriptEntry.getObject("format");

        List<dPlayer> targets = (List<dPlayer>) scriptEntry.getObject("targets");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), text.debug() + aH.debugList("Targets", targets));

        }
        if (format != null) {
            text = new Element(format.getFormattedText(scriptEntry));
        }

        for (dPlayer player : targets) {
            if (player.isValid() && player.isOnline()) {
                NMSHandler.getInstance().getPacketHelper().sendActionBarMessage(player.getPlayerEntity(), text.asString());
            }
            else {
                dB.echoError(scriptEntry.getResidingQueue(), "Tried to send action bar message to non-existent or offline player!");
            }
        }

    }

}

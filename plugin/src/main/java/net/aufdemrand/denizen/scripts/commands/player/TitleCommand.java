package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Duration;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

import java.util.Arrays;
import java.util.List;

public class TitleCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matchesPrefix("title")) {
                scriptEntry.addObject("title", arg.asElement());
            }
            else if (arg.matchesPrefix("subtitle")) {
                scriptEntry.addObject("subtitle", arg.asElement());
            }
            else if (arg.matchesPrefix("fade_in")
                    && arg.matchesArgumentType(Duration.class)) {
                scriptEntry.addObject("fade_in", arg.asType(Duration.class));
            }
            else if (arg.matchesPrefix("stay")
                    && arg.matchesArgumentType(Duration.class)) {
                scriptEntry.addObject("stay", arg.asType(Duration.class));
            }
            else if (arg.matchesPrefix("fade_out")
                    && arg.matchesArgumentType(Duration.class)) {
                scriptEntry.addObject("fade_out", arg.asType(Duration.class));
            }
            else if (arg.matchesPrefix("targets", "target")
                    && arg.matchesArgumentList(dPlayer.class)) {
                scriptEntry.addObject("targets", arg.asType(dList.class).filter(dPlayer.class));
            }

        }

        if (!scriptEntry.hasObject("title") && !scriptEntry.hasObject("subtitle")) {
            throw new InvalidArgumentsException("Must have a title or subtitle!");
        }

        scriptEntry.defaultObject("fade_in", new Duration(1)).defaultObject("stay", new Duration(3))
                .defaultObject("fade_out", new Duration(1))
                .defaultObject("targets", Arrays.asList(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer()));

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element title = scriptEntry.getElement("title");
        Element subtitle = scriptEntry.getElement("subtitle");
        Duration fade_in = scriptEntry.getdObject("fade_in");
        Duration stay = scriptEntry.getdObject("stay");
        Duration fade_out = scriptEntry.getdObject("fade_out");
        List<dPlayer> targets = (List<dPlayer>) scriptEntry.getObject("targets");

        dB.report(scriptEntry, getName(),
                (title != null ? title.debug() : "") +
                        (subtitle != null ? subtitle.debug() : "") +
                        fade_in.debug() +
                        stay.debug() +
                        fade_out.debug() +
                        aH.debugObj("targets", targets));

        for (dPlayer player : targets) {
            if (player.isValid() && player.isOnline()) {
                NMSHandler.getInstance().getPacketHelper().showTitle(player.getPlayerEntity(),
                        title != null ? title.asString() : "",
                        subtitle != null ? subtitle.asString() : "",
                        fade_in.getTicksAsInt(),
                        stay.getTicksAsInt(),
                        fade_out.getTicksAsInt());
            }
        }

    }

}

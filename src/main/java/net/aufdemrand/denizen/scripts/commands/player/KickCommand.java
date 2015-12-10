package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

import java.util.List;

public class KickCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matchesPrefix("reason")) {
                scriptEntry.addObject("reason", arg.asElement());
            }
            else if (arg.matchesPrefix("targets", "target")
                    && arg.matchesArgumentList(dPlayer.class)) {
                scriptEntry.addObject("targets", arg.asType(dList.class).filter(dPlayer.class));
            }
        }

        scriptEntry.defaultObject("reason", new Element("Kicked."));

        if (!scriptEntry.hasObject("targets")) {
            throw new InvalidArgumentsException("Must specify target(s).");
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element reason = scriptEntry.getElement("reason");
        List<dPlayer> targets = (List<dPlayer>) scriptEntry.getObject("targets");

        dB.report(scriptEntry, getName(),
                aH.debugObj("targets", targets) +
                        reason.debug());

        for (dPlayer player : targets) {
            if (player.isValid() && player.isOnline()) {
                player.getPlayerEntity().kickPlayer(reason.toString());
            }
        }

    }

}

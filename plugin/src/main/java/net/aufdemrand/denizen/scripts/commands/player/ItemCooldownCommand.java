package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Duration;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

import java.util.ArrayList;

public class ItemCooldownCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("materials")
                    && (arg.matchesArgumentType(dMaterial.class)
                    || arg.matchesArgumentType(dList.class))) {
                scriptEntry.addObject("materials", arg.asType(dList.class).filter(dMaterial.class));
            }
            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesPrefix("d", "duration")
                    && arg.matchesArgumentType(Duration.class)) {
                scriptEntry.addObject("duration", arg.asType(Duration.class));
            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("materials")) {
            throw new InvalidArgumentsException("Missing materials argument!");
        }

        scriptEntry.defaultObject("duration", new Duration(1));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        ArrayList<dMaterial> materials = (ArrayList<dMaterial>) scriptEntry.getObject("materials");
        Duration duration = scriptEntry.getdObject("duration");
        dPlayer player = ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer();

        if (player == null) {
            dB.echoError("Invalid linked player.");
            return;
        }

        dB.report(scriptEntry, getName(), aH.debugList("materials", materials) + duration.debug());

        for (dMaterial mat : materials) {
            player.getPlayerEntity().setCooldown(mat.getMaterial(), duration.getTicksAsInt());
        }
    }
}

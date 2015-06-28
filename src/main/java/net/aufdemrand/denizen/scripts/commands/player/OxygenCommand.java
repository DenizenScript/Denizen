package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

public class OxygenCommand extends AbstractCommand {

    public enum Type {MAXIMUM, REMAINING}

    public enum Mode {SET, ADD, REMOVE}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("type")
                    && arg.matchesPrefix("type", "t")
                    && arg.matchesEnum(Type.values())) {
                scriptEntry.addObject("type", arg.asElement());
            }

            else if (!scriptEntry.hasObject("mode")
                    && arg.matchesPrefix("mode", "m")
                    && arg.matchesEnum(Mode.values())) {
                scriptEntry.addObject("mode", arg.asElement());
            }

            else if (!scriptEntry.hasObject("amount")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("amount", arg.asElement());
            }

        }

        if (!((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer() || !((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().isValid())
            throw new InvalidArgumentsException("Must have player context!");

        if (!scriptEntry.hasObject("amount"))
            throw new InvalidArgumentsException("Must specify a valid amount!");

        scriptEntry.defaultObject("type", new Element("REMAINING")).defaultObject("mode", new Element("SET"));

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element type = scriptEntry.getElement("type");
        Element mode = scriptEntry.getElement("mode");
        Element amount = scriptEntry.getElement("amount");

        dB.report(scriptEntry, getName(), type.debug() + mode.debug() + amount.debug());

        dPlayer player = ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer();

        switch (Type.valueOf(type.asString().toUpperCase())) {
            case MAXIMUM:
                switch (Mode.valueOf(type.asString().toUpperCase())) {
                    case SET:
                        player.setMaximumAir(amount.asInt());
                        break;
                    case ADD:
                        player.setMaximumAir(player.getMaximumAir() + amount.asInt());
                        break;
                    case REMOVE:
                        player.setMaximumAir(player.getMaximumAir() - amount.asInt());
                        break;
                }
                return;
            case REMAINING:
                switch (Mode.valueOf(type.asString().toUpperCase())) {
                    case SET:
                        player.setRemainingAir(amount.asInt());
                        break;

                    case ADD:
                        player.setRemainingAir(player.getRemainingAir() + amount.asInt());
                        break;

                    case REMOVE:
                        player.setRemainingAir(player.getRemainingAir() - amount.asInt());
                        break;
                }
                return;
        }
    }
}

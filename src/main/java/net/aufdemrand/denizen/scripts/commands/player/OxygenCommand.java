package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.entity.Player;


public class OxygenCommand extends AbstractCommand {

    public enum Type { MAXIMUM, REMAINING }
    public enum Mode { SET, ADD, REMOVE }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        if(scriptEntry.getArguments().size() < 1) {
            throw new InvalidArgumentsException("Must specify amount/quantity.");
        }

        Mode mode = Mode.SET;
        Type type = Type.REMAINING;
        int amount = 0;

        for (String arg : scriptEntry.getArguments()) {
            if(aH.matchesValueArg("type", arg, aH.ArgumentType.String)) {
                try {
                    type = Type.valueOf(aH.getStringFrom(arg));
                    dB.echoDebug(scriptEntry, "Set type to " + type.name());
                } catch(Exception e) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Invalid type: " + e.getMessage());
                }
            }
            else if(aH.matchesValueArg("mode", arg, aH.ArgumentType.String)) {
               try {
                    mode = Mode.valueOf(aH.getStringFrom(arg));
                    dB.echoDebug(scriptEntry, "Set mode to " + mode.name());
                } catch(Exception e) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Invalid mode: " + e.getMessage());
                }
            }
            else if(aH.matchesQuantity(arg) || aH.matchesQuantity("amt")) {
                amount = aH.getIntegerFrom(arg);

                dB.echoDebug(scriptEntry, "Amount set to " + amount);
            }
        }

        scriptEntry.addObject("mode", mode);
        scriptEntry.addObject("type", type);
        scriptEntry.addObject("amount", amount);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        Player player = scriptEntry.getPlayer().getPlayerEntity();
        Mode mode = (Mode) scriptEntry.getObject("mode");
        Type type = (Type) scriptEntry.getObject("type");
        int amount = (Integer) scriptEntry.getObject("amount");

        dB.report(scriptEntry, getName(),
                aH.debugObj("Type", type.name()) + aH.debugObj("Mode", mode.name())
                        + aH.debugObj("Amount", amount));

        if(type == Type.MAXIMUM) {
            switch(mode) {
                case SET:
                    player.setMaximumAir(amount);
                    break;

                case ADD:
                    player.setMaximumAir(player.getRemainingAir() + amount);
                    break;

                case REMOVE:
                    player.setMaximumAir(player.getRemainingAir() - amount);
            }
        } else {
            switch(mode) {
                case SET:
                    player.setRemainingAir(amount);
                    break;

                case ADD:
                    player.setRemainingAir(player.getRemainingAir() + amount);
                    break;

                case REMOVE:
                    player.setRemainingAir(player.getRemainingAir() - amount);
            }
        }
    }

}

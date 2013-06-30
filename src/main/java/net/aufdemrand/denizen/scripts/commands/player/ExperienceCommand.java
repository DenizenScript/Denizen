package net.aufdemrand.denizen.scripts.commands.player;

import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;


public class ExperienceCommand extends AbstractCommand {

    private enum Type { SET, GIVE, TAKE }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        int amount = 0;
        Type type = Type.SET;
        boolean level = false;

        for (String arg : scriptEntry.getArguments()) {

            if (aH.matchesQuantity(arg) || aH.matchesInteger(arg)) {
                amount = aH.getIntegerFrom(arg);
            }

            else if (aH.matchesArg("SET, GIVE, TAKE", arg))
                type = Type.valueOf(arg.toUpperCase());

            else if(aH.matchesArg("LEVEL", arg))
                level = true;

            else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        scriptEntry.addObject("quantity", amount)
                .addObject("type", type)
                .addObject("level", level);

    }

    
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Type type = (Type) scriptEntry.getObject("type");
        Integer quantity = (Integer) scriptEntry.getObject("quantity");
        Boolean level = (Boolean) scriptEntry.getObject("level");

        dB.report(name, aH.debugObj("Type", type.toString())
            + aH.debugObj("Quantity", level ? quantity.toString() + " levels" : quantity.toString())
            + aH.debugObj("Player", scriptEntry.getPlayer().getName()));

        Player player = scriptEntry.getPlayer().getPlayerEntity();

        switch (type) {
            case SET:
                if(level)
                    player.setLevel(quantity);
                else
                    player.setTotalExperience(quantity);
                break;

            case GIVE:
                if(level)
                    player.setLevel(player.getLevel() + quantity);
                else
                    player.giveExp(player.getTotalExperience() + quantity);
                break;

            case TAKE:
                if(level)
                    player.setLevel((player.getLevel() - quantity < 0
                            ? 0 : player.getLevel() - quantity));
                else
                    player.setTotalExperience((player.getTotalExperience() - quantity < 0
                            ? 0 : player.getTotalExperience() - quantity));
                break;
        }

    }
}

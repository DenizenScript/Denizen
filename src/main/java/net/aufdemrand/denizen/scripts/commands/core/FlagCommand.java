package net.aufdemrand.denizen.scripts.commands.core;

import org.bukkit.event.Listener;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.flags.FlagManager.Flag;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/**
 * Sets a Player 'Flag'. Flags can hold information to check against
 * with the FLAGGED requirement.
 *  
 * @author Jeremy Schroeder
 */

public class FlagCommand extends AbstractCommand implements Listener {

    /* FLAG (DENIZEN|PLAYER|GLOBAL) [[NAME([#])]:[VALUE]|[NAME]:[FLAG_ACTION]:(VALUE)] (PLAYER:name) (NPCID:#)

	/* Arguments: [] - Required, () - Optional 
     * NPC|PLAYER|GLOBAL specified FlagType.
     * NAME specifies flag Name
     *  ...[#] specifies optional index, if working with a Flag List.
     *     ...:VALUE specifies the value to set the flag to.
     *  or ...:FLAG_ACTION(:VALUE) specifies the FlagAction (and value if necessary).
     *         Valid FLAG_ACTIONs: ++|+      --|-      *         /       ->      <-
     *                Description: Increase, Decrease, Multiply, Divide, Insert, Remove
     * 
     * DURATION:# Sets an expiration on the Flag. After this time, the flag is no longer valid.
     * 
     * Example usages:
     * FLAG 'MAGICSHOPITEM:FEATHER' 'DURATION:60'
     * FLAG 'HOSTILECOUNT:++'
     * FLAG 'ALIGNMENT:--:10'
     * FLAG 'CUSTOMFLAG:VALUE'
     * FLAG 'COMPLETED_QUESTS:->:This Quest'
     * FLAG 'REQUIRED_QUESTS:<-:Other Quest'
     * FLAG 'CUSTOM_PRICE[2]:12'
     */

    public enum FlagAction { SET_VALUE, SET_BOOLEAN, INCREASE, DECREASE, MULTIPLY, DIVIDE, INSERT, REMOVE }
    public enum FlagType { GLOBAL, NPC, PLAYER }

    private String flagName;
    private String flagValue;
    private String playerName;
    private Flag flag;
    private int npcId;
    private int index;
    private int duration;
    FlagAction flagAction;
    FlagType flagType;

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Set some defaults with information from the scriptEntry
        flagName = null;
        flagValue = null;
        playerName = null;
        flag = null;
        npcId = -1; 
        index = -1;
        duration = -1;
        flagAction = null;
        flagType = FlagType.PLAYER;
        
        if (scriptEntry.getNPC() != null) npcId = scriptEntry.getNPC().getId();
        if (scriptEntry.getPlayer() != null) playerName = scriptEntry.getPlayer().getName();
        else if (scriptEntry.getOfflinePlayer() != null) playerName = scriptEntry.getOfflinePlayer().getName();

        for (String arg : scriptEntry.getArguments()) {

            if (aH.matchesDuration(arg)) {
                duration = aH.getIntegerFrom(arg);
                dB.echoDebug("...flag will expire after '%s' seconds.", arg);
                continue;

                // FlagTypes
            }	else if (aH.matchesArg("GLOBAL", arg) || aH.matchesArg("DENIZEN", arg) || aH.matchesArg("PLAYER", arg)) {
                flagType = FlagType.valueOf(arg.toUpperCase());
                dB.echoDebug(Messages.DEBUG_SET_FLAG_TYPE, flagType.name());
                continue;

                // Determine flagAction and set the flagName/flagValue
            }	else if (arg.split(":", 3).length > 1) {
                String[] flagArgs = arg.split(":");
                flagName = flagArgs[0].toUpperCase();

                if (flagArgs.length == 2) {
                    if (flagArgs[1].contains("+")) {
                        flagAction = FlagAction.INCREASE;
                        flagValue = "1";
                    }   else if (flagArgs[1].contains("-")) {
                        flagAction = FlagAction.DECREASE;
                        flagValue = "1";
                    }   else {
                        flagAction = FlagAction.SET_VALUE;
                        flagValue = arg.split(":")[1];
                    }
                } else if (flagArgs.length == 3) {
                         if (flagArgs[1].contains("->")) flagAction = FlagAction.INSERT;
                    else if (flagArgs[1].contains("<-")) flagAction = FlagAction.REMOVE;
                    else if (flagArgs[1].contains("+")) flagAction = FlagAction.INCREASE;
                    else if (flagArgs[1].contains("-")) flagAction = FlagAction.DECREASE;
                    else if (flagArgs[1].contains("*")) flagAction = FlagAction.MULTIPLY;
                    else if (flagArgs[1].contains("/")) flagAction = FlagAction.DIVIDE;
                    flagValue = flagArgs[2];
                }

                dB.echoDebug(Messages.DEBUG_SET_FLAG_ACTION, arg);
                continue;

            }	else {
                flagName = arg.toUpperCase();
                flagAction = FlagAction.SET_BOOLEAN;
                dB.echoDebug("...setting '%s' as boolean flag.", arg.toUpperCase());
                continue;
            }
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Set working index, if specified. Usage example: - FLAG FLAGNAME[3]:VALUE specifies an index of 3 should be set with VALUE.
        if (flagName.split("\\[").length > 1) {
            try {
                index = Integer.valueOf(flagName.split("\\[")[1].replace("]", ""));
            } catch (Exception e) { index = -1; }

            dB.echoDebug("...flag list index set to '" + index + "'.");
            flagName = flagName.split("\\[")[0];
        }

        // Get flag
        if (flagType.equals(FlagType.NPC)) flag = denizen.flagManager().getNPCFlag(npcId, flagName);
        else if (flagType.equals(FlagType.PLAYER)) flag = denizen.flagManager().getPlayerFlag(playerName, flagName);
        else flag = denizen.flagManager().getGlobalFlag(flagName);

        // Do flagAction
        switch (flagAction) {
        case INCREASE: case DECREASE: case MULTIPLY:case DIVIDE:
            double currentValue = flag.get(index).asDouble();
            flag.set(math(currentValue, Double.valueOf(flagValue), flagAction), index);
            break;
        case SET_BOOLEAN:
            flag.set(true, index);
            break;
        case SET_VALUE:
            flag.set(flagValue, index);
            break;
        case INSERT:
            flag.add(flagValue);
            break;
        case REMOVE:
            flag.remove(flagValue, index);
            break;
        }

        // Set flag duration
        if (duration > 0) flag.setExpiration(System.currentTimeMillis() + (duration * 1000));
    }

    private double math(double currentValue, double value, FlagAction flagAction) {
        switch (flagAction) {
        case INCREASE:
            return currentValue + value;
        case DECREASE:
            return currentValue - value;
        case MULTIPLY:
            return currentValue * value;
        case DIVIDE:
            return currentValue / value;
		default:
			break;
        }

        return 0;
    }

    @Override
    public void onEnable() {
        // Nothing to do here
    }

}

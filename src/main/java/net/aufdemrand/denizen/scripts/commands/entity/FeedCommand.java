package net.aufdemrand.denizen.scripts.commands.entity;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.traits.HungerTrait;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

/**
 * Feeds a (Player) entity.
 *
 * @author Jeremy Schroeder, Mason Adkins
 */

public class FeedCommand extends AbstractCommand {

    @Override
    public void onEnable() {
        // nothing to do here
    }

    /* FEED (AMT:#) (TARGET:NPC|PLAYER) */

    /*
     * Arguments: [] - Required, () - Optional
     * (AMT:#) 1-20, usually.
     * (TARGET:NPC|PLAYER) Specifies which object is the target of the feeding effects.
     *          Default: Player, unless not available
     *
     * Example Usage:
     * FEED AMT:20 TARGET:NPC
     * FEED AMT:5
     * FEED
     *
     */

    private enum TargetType { NPC, PLAYER }

    private int amount;
    private LivingEntity target;
    private TargetType targetType;

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Must reset ALL private variables, else information left over from last time
        // might be used.
        targetType = TargetType.PLAYER;
        amount = Integer.MAX_VALUE;
        // Set target to Player by default, if available
        if (scriptEntry.getPlayer() != null) target = scriptEntry.getPlayer().getPlayerEntity();
        else target = null;

        for (String arg : scriptEntry.getArguments()) {

            if (aH.matchesQuantity(arg) || aH.matchesValueArg("amt", arg, ArgumentType.Integer)) {
                amount = aH.getIntegerFrom(arg);
                dB.echoDebug(Messages.DEBUG_SET_QUANTITY, String.valueOf(amount));

            } else if (aH.matchesValueArg("target", arg, ArgumentType.String)) {
                try {
                    targetType = TargetType.valueOf(aH.getStringFrom(arg));
                    dB.echoDebug("TARGET to FEED: " + targetType.name());
                } catch (Exception e) {
                    dB.echoError("Invalid TARGET! Valid: NPC, PLAYER");
                }

            } else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        // If TARGET is NPC/PLAYER and no NPC/PLAYER available, throw exception.
        if (targetType == TargetType.PLAYER && scriptEntry.getPlayer() == null) throw new InvalidArgumentsException(Messages.ERROR_NO_PLAYER);
        else if (targetType == TargetType.NPC && scriptEntry.getNPC() == null) throw new InvalidArgumentsException(Messages.ERROR_NO_NPCID);
        // If TARGET is NPC, set entity.
        else if (targetType == TargetType.NPC) target = scriptEntry.getNPC().getEntity();

    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Target is a NPC
        if (CitizensAPI.getNPCRegistry().isNPC(target)) {
            NPC npc = CitizensAPI.getNPCRegistry().getNPC(target);
            if (!npc.hasTrait(HungerTrait.class)) throw new CommandExecutionException("This NPC does not have the HungerTrait enabled! Use /trait hunger");
            // Set hunger level to zero
            if (amount == Integer.MAX_VALUE) npc.getTrait(HungerTrait.class).setHunger(0.00);
            // else, feed NPC
            else npc.getTrait(HungerTrait.class).feed(amount);

        // Target is a Player
        } else {
           // Set to max food level
           if (amount == Integer.MAX_VALUE) ((Player) target).setFoodLevel(20);
           // else, increase food levels
           else ((Player) target).setFoodLevel(((Player) target).getFoodLevel() + amount);
        }

    }


}

package net.aufdemrand.denizen.scripts.commands.core;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.traits.HealthTrait;
import net.aufdemrand.denizen.npc.traits.HungerTrait;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.helpers.ArgumentHelper.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.Debugger.Messages;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

/**
 * Feeds a (Player) entity.
 * 
 * @author Jeremy Schroeder, Mason Adkins
 */

public class HealCommand extends AbstractCommand {

    @Override
    public void onEnable() {
        // nothing to do here
    }

    /* HEAL (AMT:#) (TARGET:NPC|PLAYER) */

    /* 
     * Arguments: [] - Required, () - Optional 
     * (AMT:#) 1-20, usually.
     * (TARGET:NPC|PLAYER) Specifies which object is the target of the feeding effects. 
     *          Default: Player, unless not available
     *   
     * Example Usage:
     * HEAL AMT:20 TARGET:NPC
     * HEAL AMT:5
     * HEAL
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
        if (scriptEntry.getPlayer() != null) target = (LivingEntity) scriptEntry.getPlayer();
        else target = null;
        
        for (String arg : scriptEntry.getArguments()) {

            if (aH.matchesQuantity(arg) || aH.matchesValueArg("amt", arg, ArgumentType.Integer)) {
                amount = aH.getIntegerFrom(arg);
                dB.echoDebug(Messages.DEBUG_SET_QUANTITY, String.valueOf(amount));
                continue;

            } else if (aH.matchesValueArg("target", arg, ArgumentType.String)) {
                try {
                    targetType = TargetType.valueOf(aH.getStringFrom(arg));
                    dB.echoDebug("TARGET to HEAL: " + targetType.name());
                } catch (Exception e) {
                    dB.echoError("Invalid TARGET! Valid: NPC, PLAYER");
                }
                continue;

            } else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        // If TARGET is NPC/PLAYER and no NPC/PLAYER available, throw exception.
        if (targetType == TargetType.PLAYER && scriptEntry.getPlayer() == null) throw new InvalidArgumentsException(Messages.ERROR_NO_PLAYER);
        else if (targetType == TargetType.NPC && scriptEntry.getNPC() == null) throw new InvalidArgumentsException(Messages.ERROR_NO_NPCID);
        // If TARGET is NPC, set entity.
        else if (targetType == TargetType.NPC) target = scriptEntry.getNPC().getEntity();
        
        return;
    }

    
    @Override
    public void execute(String commandName) throws CommandExecutionException {

        // Target is a NPC
        if (CitizensAPI.getNPCRegistry().isNPC(target)) {
            NPC npc = CitizensAPI.getNPCRegistry().getNPC(target);
            if (!npc.hasTrait(HealthTrait.class)) npc.addTrait(HealthTrait.class);
            // Set health to max
            if (amount == Integer.MAX_VALUE) npc.getTrait(HealthTrait.class).setHealth(npc.getTrait(HealthTrait.class).getMaxHealth());
            // else, set Health
            else npc.getTrait(HealthTrait.class).heal(amount);
        
        // Target is a Player
        } else {
           // Set to max food level
           if (amount == Integer.MAX_VALUE) ((Player) target).setFoodLevel(20);
           // else, increase food levels
           else ((Player) target).setFoodLevel(((Player) target).getFoodLevel() + amount);
        }
    
        return;
    }
    
    
}

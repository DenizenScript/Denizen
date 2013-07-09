package net.aufdemrand.denizen.scripts.commands.entity;

import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.aH.ArgumentType;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.citizensnpcs.api.npc.NPC;

/**
 * Heals a Player or NPC.
 *
 * @author Jeremy Schroeder, Mason Adkins
 */

public class HealCommand extends AbstractCommand {

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

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        TargetType targetType = TargetType.PLAYER;
        Integer amount = Integer.MAX_VALUE;

        for (String arg : scriptEntry.getArguments()) {

            if (aH.matchesQuantity(arg) || aH.matchesDouble(arg)
                    || aH.matchesValueArg("amt", arg, ArgumentType.Double))
                amount = aH.getIntegerFrom(arg);

            else if (aH.matchesValueArg("target", arg, ArgumentType.String)) {
                try {
                    targetType = TargetType.valueOf(aH.getStringFrom(arg).toUpperCase());
                } catch (Exception e) { dB.echoError("Invalid TARGET! Valid: NPC, PLAYER"); }

            }   else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        // If TARGET is NPC/PLAYER and no NPC/PLAYER available, throw exception.
        if (targetType == TargetType.PLAYER && scriptEntry.getPlayer() == null)
            throw new InvalidArgumentsException(Messages.ERROR_NO_PLAYER);

        else if (targetType == TargetType.NPC && scriptEntry.getNPC() == null)
            throw new InvalidArgumentsException(Messages.ERROR_NO_NPCID);

        scriptEntry.addObject("target", targetType)
                .addObject("amount", amount);
    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        TargetType target = (TargetType) scriptEntry.getObject("target");
        Double amount = (Double) scriptEntry.getObject("amount");

        dB.report(getName(),
                aH.debugObj("Target", (target == TargetType.PLAYER ? scriptEntry.getPlayer().getName()
                        : scriptEntry.getNPC().getName()))
                        + aH.debugObj("Amount", (amount == Double.MAX_VALUE ? "Full"
                        : String.valueOf(amount))));

        switch (target) {

            case NPC:
                NPC npc = scriptEntry.getNPC().getCitizen();
                
                // Set health to max
                if (amount == Integer.MAX_VALUE)
                    npc.getBukkitEntity().setHealth(npc.getBukkitEntity().getMaxHealth());
                    // else, set Health
                else npc.getBukkitEntity().setHealth(npc.getBukkitEntity().getHealth() + amount);
                return;

            case PLAYER:
                Player player = scriptEntry.getPlayer().getPlayerEntity();
                // Set to max health
                if (amount == Integer.MAX_VALUE) player.setHealth(player.getMaxHealth());
                    // else, increase health
                else ((CraftLivingEntity) player).getHandle().setHealth((float) (player.getHealth() + amount));
        }

    }
}

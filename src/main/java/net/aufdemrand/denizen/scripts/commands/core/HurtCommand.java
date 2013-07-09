package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
import org.bukkit.entity.Player;

public class HurtCommand extends AbstractCommand {

    private enum TargetType { NPC, PLAYER }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        TargetType targetType = TargetType.PLAYER;
        double amount = 1;

        for (String arg : scriptEntry.getArguments()) {

            if (aH.matchesQuantity(arg) || aH.matchesDouble(arg)
                    || aH.matchesValueArg("amt", arg, ArgumentType.Double))
                amount = aH.getDoubleFrom(arg);

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

                npc.getBukkitEntity().setHealth(npc.getBukkitEntity().getHealth() - amount);
                return;
                
            case PLAYER:
                Player player = scriptEntry.getPlayer();
                // Injure player
                ((CraftLivingEntity) player).getHandle().setHealth((float) (player.getHealth() - amount));
                return;
        }

    }
}

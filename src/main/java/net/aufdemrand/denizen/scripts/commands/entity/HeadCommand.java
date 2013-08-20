package net.aufdemrand.denizen.scripts.commands.entity;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.aH.ArgumentType;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;

/**
 * Sets an NPC or player's head to that of a specific player's skin.
 *
 * @author David Cernat
 */

public class HeadCommand extends AbstractCommand {

    private enum TargetType { NPC, PLAYER }
    
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
    
        TargetType targetType = TargetType.NPC;
        Integer duration = null;
        String skin = null;
        
        for (String arg : scriptEntry.getArguments()) {
            // If argument is a duration
            if (aH.matchesDuration(arg)) {
                duration = aH.getIntegerFrom(arg);
                dB.echoDebug("...head duration set to '%s'.", arg);
            }
                        
            else if (aH.matchesArg("PLAYER", arg)) {
                targetType = TargetType.PLAYER;
                dB.echoDebug("...will affect the player!");
            }
               
            else if (aH.matchesValueArg("skin", arg, ArgumentType.String)) {
                skin = aH.getStringFrom(arg);
                dB.echoDebug("...will have " + skin + "'s head");

            } else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }
        
        // If TARGET is NPC/PLAYER and no NPC/PLAYER available, throw exception.
        if (targetType == TargetType.PLAYER && scriptEntry.getPlayer() == null) throw new InvalidArgumentsException(Messages.ERROR_NO_PLAYER);
        else if (targetType == TargetType.NPC && scriptEntry.getNPC() == null) throw new InvalidArgumentsException(Messages.ERROR_NO_NPCID);

        scriptEntry.addObject("target", targetType)
                   .addObject("skin", skin);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        
        TargetType target = (TargetType) scriptEntry.getObject("target");
        String skin = (String) scriptEntry.getObject("skin");
        
        // Create head item with chosen skin
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
        ItemMeta itemMeta = item.getItemMeta();
        ((SkullMeta) itemMeta).setOwner(skin);
        item.setItemMeta(itemMeta);
        
        if (target.name().equals("NPC")) {
            NPC npc = scriptEntry.getNPC().getCitizen();
            
            if (!npc.hasTrait(Equipment.class)) npc.addTrait(Equipment.class);
            Equipment trait = npc.getTrait(Equipment.class);
            trait.set(1, item);
        }
        else {
            Player player = scriptEntry.getPlayer().getPlayerEntity();
            player.getInventory().setHelmet(item);
        }

    }

}
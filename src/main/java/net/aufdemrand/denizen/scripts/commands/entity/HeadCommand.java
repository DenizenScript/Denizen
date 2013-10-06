package net.aufdemrand.denizen.scripts.commands.entity;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.trait.trait.Equipment;

/**
 * Makes players or NPCs wear a specific player's head.
 *
 * @author David Cernat
 */

public class HeadCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("skin")
                    && (arg.matchesPrefix("skin, s")))
                scriptEntry.addObject("skin", arg.asElement());

            else if (!scriptEntry.hasObject("entities")
                    && arg.matches("PLAYER")
                    && scriptEntry.hasPlayer())
                scriptEntry.addObject("entities", Arrays.asList(scriptEntry.getPlayer().getDenizenEntity()));

            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class))
                scriptEntry.addObject("entities", ((dList) arg.asType(dList.class)).filter(dEntity.class));

            else dB.echoError(dB.Messages.ERROR_UNKNOWN_ARGUMENT, arg.raw_value);
        }

        // Use the NPC or the Player as the default entity
        scriptEntry.defaultObject("entities",
                (scriptEntry.hasNPC() ? Arrays.asList(scriptEntry.getNPC().getDenizenEntity()) : null),
                (scriptEntry.hasPlayer() ? Arrays.asList(scriptEntry.getPlayer().getDenizenEntity()) : null));

        if (!scriptEntry.hasObject("skin"))
            throw new InvalidArgumentsException(dB.Messages.ERROR_MISSING_OTHER, "SKIN");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        Element skin = scriptEntry.getElement("skin");

        // Report to dB
        dB.report(getName(),
                aH.debugObj("entities", entities.toString()) +
                skin.debug());

        // Create head item with chosen skin
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
        ItemMeta itemMeta = item.getItemMeta();
        ((SkullMeta) itemMeta).setOwner(skin.asString().replaceAll("[pP]@", ""));
        item.setItemMeta(itemMeta);

        for (dEntity entity : entities) {
            if (entity.isNPC()) {
                if (!entity.getNPC().hasTrait(Equipment.class))
                    entity.getNPC().addTrait(Equipment.class);
                Equipment trait = entity.getNPC().getTrait(Equipment.class);
                trait.set(1, item);
            }
            else if (entity.isPlayer()) {
                entity.getPlayer().getInventory().setHelmet(item);
            }
            else {
                if (entity.isLivingEntity() && entity.getLivingEntity().getEquipment() != null)
                    entity.getLivingEntity().getEquipment().setHelmet(item);
                else
                    dB.echoError(entity.identify() + " is not a living entity!");
            }
        }
    }
}

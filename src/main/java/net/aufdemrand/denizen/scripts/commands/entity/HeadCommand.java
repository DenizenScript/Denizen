package net.aufdemrand.denizen.scripts.commands.entity;

import java.util.Arrays;
import java.util.List;

import net.aufdemrand.denizen.objects.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.trait.trait.Equipment;

/**
 * Makes players or NPCs wear a specific player's head.
 *
 * @author David Cernat, aufdemrand
 */

public class HeadCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("material")
                    && arg.matchesArgumentType(dMaterial.class)
                    && !arg.matchesPrefix("skin", "s"))
                scriptEntry.addObject("material", arg.asType(dMaterial.class));

            else if (!scriptEntry.hasObject("skin")
                    && (arg.matchesPrefix("skin", "s")))
                scriptEntry.addObject("skin", arg.asElement());

            else if (!scriptEntry.hasObject("entities")
                    && arg.matches("player")
                    && scriptEntry.hasPlayer())
                scriptEntry.addObject("entities", Arrays.asList(scriptEntry.getPlayer().getDenizenEntity()));

            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class))
                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class));

            else arg.reportUnhandled();
        }

        // Use the NPC or the Player as the default entity
        scriptEntry.defaultObject("entities",
                (scriptEntry.hasNPC() ? Arrays.asList(scriptEntry.getNPC().getDenizenEntity()) : null),
                (scriptEntry.hasPlayer() ? Arrays.asList(scriptEntry.getPlayer().getDenizenEntity()) : null));

        if (!scriptEntry.hasObject("skin") && !scriptEntry.hasObject("material"))
            throw new InvalidArgumentsException("Must specify a skin or material!");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        Element skin = scriptEntry.getElement("skin");
        dMaterial material = scriptEntry.getdObject("material");

        // Report to dB
        dB.report(scriptEntry, getName(),
                aH.debugObj("entities", entities.toString()) +
                        (skin != null ? skin.debug() : "") + (material != null ? material.debug() : ""));

        ItemStack item = null;

        // Create head item with chosen skin, or item/skin
        if (skin != null) {
            item = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
            ItemMeta itemMeta = item.getItemMeta();
            ((SkullMeta) itemMeta).setOwner(skin.asString().replaceAll("[pP]@", ""));
            item.setItemMeta(itemMeta);

        } else if (material != null)
            item = new ItemStack(material.getMaterial());

        // Loop through entities, apply the item/skin

        for (dEntity entity : entities) {
            if (entity.isNPC()) {
                if (!entity.getDenizenNPC().getCitizen().hasTrait(Equipment.class))
                    entity.getDenizenNPC().getCitizen().addTrait(Equipment.class);
                Equipment trait = entity.getDenizenNPC().getCitizen().getTrait(Equipment.class);
                trait.set(1, item);

            } else if (entity.isPlayer()) {
                entity.getPlayer().getInventory().setHelmet(item);

            } else {
                if (entity.isLivingEntity() && entity.getLivingEntity().getEquipment() != null)
                    entity.getLivingEntity().getEquipment().setHelmet(item);

                else
                    dB.echoError(scriptEntry.getResidingQueue(), entity.identify() + " is not a living entity!");

            }
        }
    }
}

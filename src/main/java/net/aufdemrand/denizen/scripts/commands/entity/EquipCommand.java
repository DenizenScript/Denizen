package net.aufdemrand.denizen.scripts.commands.entity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.LivingEntity;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;

/**
 * Equip entities with items and armor.
 *
 * @author David Cernat
 */

public class EquipCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        Map<String, dItem> equipment = new HashMap<String,dItem>();

        // Initialize necessary fields
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("entities")
                && arg.matchesArgumentList(dEntity.class)) {

                scriptEntry.addObject("entities", ((dList) arg.asType(dList.class)).filter(dEntity.class));
            }

            else if (arg.matchesArgumentType(dItem.class)
                     && arg.matchesPrefix("head, helmet")) {
                equipment.put("head", dItem.valueOf(arg.getValue()));
            }

            else if (arg.matchesArgumentType(dItem.class)
                     && arg.matchesPrefix("chest, chestplate")) {
                equipment.put("chest", dItem.valueOf(arg.getValue()));
            }

            else if (arg.matchesArgumentType(dItem.class)
                     && arg.matchesPrefix("legs, leggings")) {
                equipment.put("legs", dItem.valueOf(arg.getValue()));
            }

            else if (arg.matchesArgumentType(dItem.class)
                     && arg.matchesPrefix("boots, feet")) {
                equipment.put("boots", dItem.valueOf(arg.getValue()));
            }

            // Default to item in hand if no prefix is used
            else if (arg.matchesArgumentType(dItem.class)) {
               equipment.put("hand", dItem.valueOf(arg.getValue()));
            }

            else if (arg.matches("player") && scriptEntry.hasPlayer()) {
                // Player arg for compatibility with old scripts
                scriptEntry.addObject("entities", Arrays.asList(scriptEntry.getPlayer().getDenizenEntity()));
            }

            else arg.reportUnhandled();
        }

        // Make sure at least one equipment argument was used
        if (equipment.isEmpty())
            throw new InvalidArgumentsException("Must specify equipment!");

        scriptEntry.addObject("equipment", equipment);

        // Use player or NPC as default entity
        scriptEntry.defaultObject("entities", (scriptEntry.hasNPC() ? Arrays.asList(scriptEntry.getNPC().getDenizenEntity()) : null),
                                              (scriptEntry.hasPlayer() ? Arrays.asList(scriptEntry.getPlayer().getDenizenEntity()) : null));

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry)
            throws CommandExecutionException {

        Map<String, dItem> equipment = (Map<String, dItem>) scriptEntry.getObject("equipment");
        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");

        // Report to dB
        dB.report(scriptEntry, getName(), aH.debugObj("entities", entities.toString()) +
                             aH.debugObj("equipment", equipment.toString()));

        for (dEntity entity : entities) {

            if (entity.isGeneric()) {
                dB.echoError("Cannot equip generic entity " + entity.identify() + "!");
            }
            else if (entity.isNPC()) {

                NPC npc = entity.getNPC();

                if (npc != null) {

                    if (!npc.hasTrait(Equipment.class)) npc.addTrait(Equipment.class);
                    Equipment trait = npc.getTrait(Equipment.class);

                    if (equipment.get("hand")  != null) trait.set(0, equipment.get("hand").getItemStack());
                    if (equipment.get("head")  != null) trait.set(1, equipment.get("head").getItemStack());
                    if (equipment.get("chest") != null) trait.set(2, equipment.get("chest").getItemStack());
                    if (equipment.get("legs")  != null) trait.set(3, equipment.get("legs").getItemStack());
                    if (equipment.get("boots") != null) trait.set(4, equipment.get("boots").getItemStack());
                }

            }
            else {

                LivingEntity livingEntity = entity.getLivingEntity();

                if (livingEntity != null) {

                    if (equipment.get("hand")  != null) livingEntity.getEquipment().setItemInHand(equipment.get("hand").getItemStack());
                    if (equipment.get("head")  != null) livingEntity.getEquipment().setHelmet(equipment.get("head").getItemStack());
                    if (equipment.get("chest") != null) livingEntity.getEquipment().setChestplate(equipment.get("chest").getItemStack());
                    if (equipment.get("legs")  != null) livingEntity.getEquipment().setLeggings(equipment.get("legs").getItemStack());
                    if (equipment.get("boots") != null) livingEntity.getEquipment().setBoots(equipment.get("boots").getItemStack());
                }
            }
        }
    }

}

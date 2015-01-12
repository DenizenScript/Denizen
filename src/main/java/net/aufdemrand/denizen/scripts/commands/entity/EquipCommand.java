package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.entity.LivingEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class));
            }

            else if (arg.matchesArgumentType(dItem.class)
                     && arg.matchesPrefix("head", "helmet")) {
                equipment.put("head", dItem.valueOf(arg.getValue()));
            }

            else if (arg.matchesArgumentType(dItem.class)
                     && arg.matchesPrefix("chest", "chestplate")) {
                equipment.put("chest", dItem.valueOf(arg.getValue()));
            }

            else if (arg.matchesArgumentType(dItem.class)
                     && arg.matchesPrefix("legs", "leggings")) {
                equipment.put("legs", dItem.valueOf(arg.getValue()));
            }

            else if (arg.matchesArgumentType(dItem.class)
                     && arg.matchesPrefix("boots", "feet")) {
                equipment.put("boots", dItem.valueOf(arg.getValue()));
            }

            // Default to item in hand if no prefix is used
            else if (arg.matchesArgumentType(dItem.class)) {
               equipment.put("hand", dItem.valueOf(arg.getValue()));
            }

            else if (arg.matches("player") && ((BukkitScriptEntryData)scriptEntry.entryData).hasPlayer()) {
                // Player arg for compatibility with old scripts
                scriptEntry.addObject("entities", Arrays.asList(((BukkitScriptEntryData)scriptEntry.entryData).getPlayer().getDenizenEntity()));
            }

            else arg.reportUnhandled();
        }

        // Make sure at least one equipment argument was used
        if (equipment.isEmpty())
            throw new InvalidArgumentsException("Must specify equipment!");

        scriptEntry.addObject("equipment", equipment);

        // Use player or NPC as default entity
        scriptEntry.defaultObject("entities", (((BukkitScriptEntryData)scriptEntry.entryData).hasNPC() ? Arrays.asList(((BukkitScriptEntryData)scriptEntry.entryData).getNPC().getDenizenEntity()) : null),
                                              (((BukkitScriptEntryData)scriptEntry.entryData).hasPlayer() ? Arrays.asList(((BukkitScriptEntryData)scriptEntry.entryData).getPlayer().getDenizenEntity()) : null));

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
                dB.echoError(scriptEntry.getResidingQueue(), "Cannot equip generic entity " + entity.identify() + "!");
            }
            else if (entity.isNPC()) {

                dNPC npc = entity.getDenizenNPC();

                if (npc != null) {

                    Equipment trait = npc.getEquipmentTrait();

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

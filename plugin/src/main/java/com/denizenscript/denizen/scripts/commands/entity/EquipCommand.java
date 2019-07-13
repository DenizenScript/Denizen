package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dItem;
import com.denizenscript.denizen.objects.dNPC;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquipCommand extends AbstractCommand {

    // <--[command]
    // @Name Equip
    // @Syntax equip (<entity>|...) (hand:<item>) (offhand:<item>) (head:<item>) (chest:<item>) (legs:<item>) (boots:<item>) (saddle:<item>) (horse_armor:<item>)
    // @Required 1
    // @Short Equips items and armor on a list of entities.
    // @Group entity
    //
    // @Description
    // This command equips an item or armor to an entity or list of entities to the specified slot(s).
    // Set the item to 'i@air' to unequip any slot.
    //
    // @Tags
    // <e@entity.equipment>
    // <e@entity.equipment.helmet>
    // <e@entity.equipment.chestplate>
    // <e@entity.equipment.leggings>
    // <e@entity.equipment.boots>
    // <in@inventory.equipment>
    //
    // @Usage
    // Use to equip a stone block on the player's head.
    // - equip <player> head:i@stone
    //
    // @Usage
    // Use to equip a iron helmet on two players named Bob and Steve.
    // - equip p@bob|p@steve head:i@iron_helmet
    //
    // @Usage
    // Use to unequip all armor off the player.
    // - equip <player> head:i@air chest:i@air legs:i@air boots:i@air
    //
    // @Usage
    // Use to equip a saddle on a horse.
    // - equip e@horse saddle:i@saddle
    //
    // @Usage
    // Use to equip a saddle on a pig.
    // - equip e@pig saddle:i@saddle
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        Map<String, dItem> equipment = new HashMap<>();

        // Initialize necessary fields
        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class)) {

                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(dEntity.class, scriptEntry));
            }
            else if (arg.matchesArgumentType(dItem.class)
                    && arg.matchesPrefix("head", "helmet")) {
                equipment.put("head", dItem.valueOf(arg.getValue(), scriptEntry.entryData.getTagContext()));
            }
            else if (arg.matchesArgumentType(dItem.class)
                    && arg.matchesPrefix("chest", "chestplate")) {
                equipment.put("chest", dItem.valueOf(arg.getValue(), scriptEntry.entryData.getTagContext()));
            }
            else if (arg.matchesArgumentType(dItem.class)
                    && arg.matchesPrefix("legs", "leggings")) {
                equipment.put("legs", dItem.valueOf(arg.getValue(), scriptEntry.entryData.getTagContext()));
            }
            else if (arg.matchesArgumentType(dItem.class)
                    && arg.matchesPrefix("boots", "feet")) {
                equipment.put("boots", dItem.valueOf(arg.getValue(), scriptEntry.entryData.getTagContext()));
            }
            else if (arg.matchesArgumentType(dItem.class)
                    && arg.matchesPrefix("saddle")) {
                equipment.put("saddle", dItem.valueOf(arg.getValue(), scriptEntry.entryData.getTagContext()));
            }
            else if (arg.matchesArgumentType(dItem.class)
                    && arg.matchesPrefix("horse_armor", "horse_armour")) {
                equipment.put("horse_armor", dItem.valueOf(arg.getValue(), scriptEntry.entryData.getTagContext()));
            }
            else if (arg.matchesArgumentType(dItem.class)
                    && arg.matchesPrefix("offhand")) {
                equipment.put("offhand", dItem.valueOf(arg.getValue(), scriptEntry.entryData.getTagContext()));
            }

            // Default to item in hand if no prefix is used
            else if (arg.matchesArgumentType(dItem.class)) {
                equipment.put("hand", dItem.valueOf(arg.getValue(), scriptEntry.entryData.getTagContext()));
            }
            else if (arg.matches("player") && Utilities.entryHasPlayer(scriptEntry)) {
                // Player arg for compatibility with old scripts
                scriptEntry.addObject("entities", Arrays.asList(Utilities.getEntryPlayer(scriptEntry).getDenizenEntity()));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Make sure at least one equipment argument was used
        if (equipment.isEmpty()) {
            throw new InvalidArgumentsException("Must specify equipment!");
        }

        scriptEntry.addObject("equipment", equipment);

        // Use player or NPC as default entity
        scriptEntry.defaultObject("entities", (Utilities.entryHasNPC(scriptEntry) ? Arrays.asList(Utilities.getEntryNPC(scriptEntry).getDenizenEntity()) : null),
                (Utilities.entryHasPlayer(scriptEntry) ? Arrays.asList(Utilities.getEntryPlayer(scriptEntry).getDenizenEntity()) : null));

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) {

        Map<String, dItem> equipment = (Map<String, dItem>) scriptEntry.getObject("equipment");
        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), ArgumentHelper.debugObj("entities", entities.toString()) +
                    ArgumentHelper.debugObj("equipment", equipment.toString()));
        }

        for (dEntity entity : entities) {

            if (entity.isGeneric()) {
                Debug.echoError(scriptEntry.getResidingQueue(), "Cannot equip generic entity " + entity.identify() + "!");
            }
            else if (entity.isCitizensNPC()) {

                dNPC npc = entity.getDenizenNPC();

                if (npc != null) {

                    Equipment trait = npc.getEquipmentTrait();

                    if (equipment.get("hand") != null) {
                        trait.set(0, equipment.get("hand").getItemStack());
                    }
                    if (equipment.get("head") != null) {
                        trait.set(1, equipment.get("head").getItemStack());
                    }
                    if (equipment.get("chest") != null) {
                        trait.set(2, equipment.get("chest").getItemStack());
                    }
                    if (equipment.get("legs") != null) {
                        trait.set(3, equipment.get("legs").getItemStack());
                    }
                    if (equipment.get("boots") != null) {
                        trait.set(4, equipment.get("boots").getItemStack());
                    }
                    if (equipment.get("offhand") != null) {
                        trait.set(5, equipment.get("offhand").getItemStack());
                    }

                    if (npc.isSpawned()) {
                        LivingEntity livingEntity = npc.getLivingEntity();

                        // TODO: Citizens API for this blob?

                        if (livingEntity.getType() == EntityType.HORSE) {
                            if (equipment.get("saddle") != null) {
                                ((Horse) livingEntity).getInventory().setSaddle(equipment.get("saddle").getItemStack());
                            }
                            if (equipment.get("horse_armor") != null) {
                                ((Horse) livingEntity).getInventory().setArmor(equipment.get("horse_armor").getItemStack());
                            }
                        }
                        else if (livingEntity.getType() == EntityType.PIG) {
                            if (equipment.get("saddle") != null) {
                                dItem saddle = equipment.get("saddle");
                                if (saddle.getItemStack().getType() == Material.SADDLE) {
                                    ((Pig) livingEntity).setSaddle(true);
                                }
                                else {
                                    ((Pig) livingEntity).setSaddle(false);
                                }
                            }
                        }
                    }
                }

            }
            else {

                LivingEntity livingEntity = entity.getLivingEntity();

                if (livingEntity != null) {

                    if (livingEntity.getType() == EntityType.HORSE) {
                        if (equipment.get("saddle") != null) {
                            ((Horse) livingEntity).getInventory().setSaddle(equipment.get("saddle").getItemStack());
                        }
                        if (equipment.get("horse_armor") != null) {
                            ((Horse) livingEntity).getInventory().setArmor(equipment.get("horse_armor").getItemStack());
                        }
                    }
                    else if (livingEntity.getType() == EntityType.PIG) {
                        if (equipment.get("saddle") != null) {
                            dItem saddle = equipment.get("saddle");
                            if (saddle.getItemStack().getType() == Material.SADDLE) {
                                ((Pig) livingEntity).setSaddle(true);
                            }
                            else {
                                ((Pig) livingEntity).setSaddle(false);
                            }
                        }
                    }
                    else {

                        if (equipment.get("hand") != null) {
                            NMSHandler.getInstance().getEntityHelper().setItemInHand(livingEntity, equipment.get("hand").getItemStack());
                        }
                        if (equipment.get("head") != null) {
                            livingEntity.getEquipment().setHelmet(equipment.get("head").getItemStack());
                        }
                        if (equipment.get("chest") != null) {
                            livingEntity.getEquipment().setChestplate(equipment.get("chest").getItemStack());
                        }
                        if (equipment.get("legs") != null) {
                            livingEntity.getEquipment().setLeggings(equipment.get("legs").getItemStack());
                        }
                        if (equipment.get("boots") != null) {
                            livingEntity.getEquipment().setBoots(equipment.get("boots").getItemStack());
                        }
                        if (equipment.get("offhand") != null) {
                            NMSHandler.getInstance().getEntityHelper().setItemInOffHand(livingEntity, equipment.get("offhand").getItemStack());
                        }
                    }
                }
            }
        }
    }
}

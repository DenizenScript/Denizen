package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.NPCTag;
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
    // Set the item to 'air' to unequip any slot.
    //
    // @Tags
    // <EntityTag.equipment>
    // <EntityTag.equipment.helmet>
    // <EntityTag.equipment.chestplate>
    // <EntityTag.equipment.leggings>
    // <EntityTag.equipment.boots>
    // <InventoryTag.equipment>
    //
    // @Usage
    // Use to equip a stone block on the player's head.
    // - equip <player> head:stone
    //
    // @Usage
    // Use to equip a iron helmet on two players named Bob and Steve.
    // - equip <[player]>|<[someplayer]> head:iron_helmet
    //
    // @Usage
    // Use to unequip all armor off the player.
    // - equip <player> head:air chest:air legs:air boots:air
    //
    // @Usage
    // Use to equip a saddle on a horse.
    // - equip e@horse saddle:saddle
    //
    // @Usage
    // Use to equip a saddle on a pig.
    // - equip e@pig saddle:saddle
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        Map<String, ItemTag> equipment = new HashMap<>();

        // Initialize necessary fields
        for (Argument arg : scriptEntry.getProcessedArgs()) {

            if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(EntityTag.class)) {

                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry));
            }
            else if (arg.matchesArgumentType(ItemTag.class)
                    && arg.matchesPrefix("head", "helmet")) {
                equipment.put("head", ItemTag.valueOf(arg.getValue(), scriptEntry.entryData.getTagContext()));
            }
            else if (arg.matchesArgumentType(ItemTag.class)
                    && arg.matchesPrefix("chest", "chestplate")) {
                equipment.put("chest", ItemTag.valueOf(arg.getValue(), scriptEntry.entryData.getTagContext()));
            }
            else if (arg.matchesArgumentType(ItemTag.class)
                    && arg.matchesPrefix("legs", "leggings")) {
                equipment.put("legs", ItemTag.valueOf(arg.getValue(), scriptEntry.entryData.getTagContext()));
            }
            else if (arg.matchesArgumentType(ItemTag.class)
                    && arg.matchesPrefix("boots", "feet")) {
                equipment.put("boots", ItemTag.valueOf(arg.getValue(), scriptEntry.entryData.getTagContext()));
            }
            else if (arg.matchesArgumentType(ItemTag.class)
                    && arg.matchesPrefix("saddle")) {
                equipment.put("saddle", ItemTag.valueOf(arg.getValue(), scriptEntry.entryData.getTagContext()));
            }
            else if (arg.matchesArgumentType(ItemTag.class)
                    && arg.matchesPrefix("horse_armor", "horse_armour")) {
                equipment.put("horse_armor", ItemTag.valueOf(arg.getValue(), scriptEntry.entryData.getTagContext()));
            }
            else if (arg.matchesArgumentType(ItemTag.class)
                    && arg.matchesPrefix("offhand")) {
                equipment.put("offhand", ItemTag.valueOf(arg.getValue(), scriptEntry.entryData.getTagContext()));
            }

            // Default to item in hand if no prefix is used
            else if (arg.matchesArgumentType(ItemTag.class)) {
                equipment.put("hand", ItemTag.valueOf(arg.getValue(), scriptEntry.entryData.getTagContext()));
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

        Map<String, ItemTag> equipment = (Map<String, ItemTag>) scriptEntry.getObject("equipment");
        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), ArgumentHelper.debugObj("entities", entities.toString()) +
                    ArgumentHelper.debugObj("equipment", equipment.toString()));
        }

        for (EntityTag entity : entities) {

            if (entity.isGeneric()) {
                Debug.echoError(scriptEntry.getResidingQueue(), "Cannot equip generic entity " + entity.identify() + "!");
            }
            else if (entity.isCitizensNPC()) {

                NPCTag npc = entity.getDenizenNPC();

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
                                ItemTag saddle = equipment.get("saddle");
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
                            ItemTag saddle = equipment.get("saddle");
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
                            livingEntity.getEquipment().setItemInMainHand(equipment.get("hand").getItemStack());
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
                            livingEntity.getEquipment().setItemInOffHand(equipment.get("offhand").getItemStack());
                        }
                    }
                }
            }
        }
    }
}

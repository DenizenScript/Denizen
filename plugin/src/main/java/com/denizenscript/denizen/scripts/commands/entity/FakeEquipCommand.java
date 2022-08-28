package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class FakeEquipCommand extends AbstractCommand {

    public FakeEquipCommand() {
        setName("fakeequip");
        setSyntax("fakeequip [<entity>|...] (for:<player>|...) (duration:<duration>/reset) (hand:<item>) (offhand:<item>) (head:<item>) (chest:<item>) (legs:<item>) (boots:<item>)");
        setRequiredArguments(1, 9);
        isProcedural = false;
    }

    // <--[command]
    // @Name FakeEquip
    // @Syntax fakeequip [<entity>|...] (for:<player>|...) (duration:<duration>/reset) (hand:<item>) (offhand:<item>) (head:<item>) (chest:<item>) (legs:<item>) (boots:<item>)
    // @Required 1
    // @Maximum 9
    // @Short Fake-equips items and armor on a list of entities for players to see without real change.
    // @Group entity
    //
    // @Description
    // This command fake-equips items and armor on a list of entities.
    //
    // The change doesn't happen on-server, and no armor effects will happen from it.
    //
    // The equipment can only be seen by certain players. By default, the linked player is used.
    //
    // The changes will remain in place for as long as the duration is specified (even if the real equipment is changed).
    // The changes can be manually reset early by using the 'reset' argument.
    // If you do not provide a duration, the fake equipment will last until manually reset.
    // This does not persist across server restarts.
    //
    // Set the item to 'air' to unequip any slot.
    //
    // @Tags
    // <EntityTag.equipment>
    // <InventoryTag.equipment>
    //
    // @Usage
    // Use to fake-equip a stone block on the player's head.
    // - fakeequip <player> head:stone duration:10s
    //
    // @Usage
    // Use to fake-equip an iron helmet on two defined players.
    // - fakeequip <[player]>|<[someplayer]> head:iron_helmet duration:1m
    //
    // @Usage
    // Use to fake-unequip all armor off the player.
    // - fakeequip <player> head:air chest:air legs:air boots:air duration:5s
    //
    // @Usage
    // Use to make all players within 30 blocks of an entity see it permanently equip a shield.
    // - fakeequip <[entity]> offhand:shield for:<[entity].find_players_within[30]> duration:0
    //
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        EquipmentOverride equipment = new EquipmentOverride();
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(EntityTag.class)) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry));
            }
            else if (arg.matchesPrefix("duration")
                    && arg.matchesArgumentType(DurationTag.class)) {
                scriptEntry.addObject("duration", arg.asType(DurationTag.class));
            }
            else if (arg.matches("reset")) {
                scriptEntry.addObject("reset", new ElementTag(true));
            }
            else if (arg.matchesPrefix("for")
                    && arg.matchesArgumentList(PlayerTag.class)) {
                scriptEntry.addObject("for", arg.asType(ListTag.class).filter(PlayerTag.class, scriptEntry));
            }
            else if (arg.matchesPrefix("head", "helmet")
                    && arg.matchesArgumentType(ItemTag.class)) {
                equipment.head = arg.asType(ItemTag.class);
            }
            else if (arg.matchesPrefix("chest", "chestplate")
                    && arg.matchesArgumentType(ItemTag.class)) {
                equipment.chest = arg.asType(ItemTag.class);
            }
            else if (arg.matchesPrefix("legs", "leggings")
                    && arg.matchesArgumentType(ItemTag.class)) {
                equipment.legs = arg.asType(ItemTag.class);
            }
            else if (arg.matchesPrefix("boots", "feet")
                    && arg.matchesArgumentType(ItemTag.class)) {
                equipment.boots = arg.asType(ItemTag.class);
            }
            else if (arg.matchesPrefix("offhand")
                    && arg.matchesArgumentType(ItemTag.class)) {
                equipment.offhand = arg.asType(ItemTag.class);
            }
            else if (arg.matchesPrefix("hand")
                    && arg.matchesArgumentType(ItemTag.class)) {
                equipment.hand = arg.asType(ItemTag.class);
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (equipment.isEmpty() && !scriptEntry.hasObject("reset")) {
            throw new InvalidArgumentsException("Must specify equipment!");
        }
        if (!scriptEntry.hasObject("for")) {
            PlayerTag player = Utilities.getEntryPlayer(scriptEntry);
            if (player == null) {
                throw new InvalidArgumentsException("Must specify a for player!");
            }
            scriptEntry.addObject("for", Collections.singletonList(player));
        }
        scriptEntry.addObject("equipment", equipment);
    }

    public static class EquipmentOverride {

        public ItemTag hand, offhand, head, chest, legs, boots;

        public BukkitTask cancelTask;

        public boolean isEmpty() {
            return hand == null && offhand == null && head == null && chest == null && legs == null && boots == null;
        }

        @Override
        public String toString() {
            return "Equipment{hand=" + hand + ",offhand=" + offhand + ",head=" + head + ",chest=" + chest + ",legs=" + legs + ",boots=" + boots + "}";
        }

        public void copyFrom(EquipmentOverride override) {
            hand = override.hand == null ? hand : override.hand;
            offhand = override.offhand == null ? offhand : override.offhand;
            head = override.head == null ? head : override.head;
            chest = override.chest == null ? chest : override.chest;
            legs = override.legs == null ? legs : override.legs;
            boots = override.boots == null ? boots : override.boots;
        }

        public EquipmentOverride getVariantFor(Player player) {
            return this;
        }
    }

    public static EquipmentOverride getOverrideFor(UUID entity, Player player) {
        HashMap<UUID, EquipmentOverride> playerMap = overrides.get(player.getUniqueId());
        if (playerMap == null) {
            playerMap = overrides.get(null);
            if (playerMap == null) {
                return null;
            }
        }
        EquipmentOverride override = playerMap.get(entity);
        if (override == null) {
            return null;
        }
        return override.getVariantFor(player);
    }

    public static HashMap<UUID, HashMap<UUID, EquipmentOverride>> overrides = new HashMap<>();

    @Override
    public void execute(ScriptEntry scriptEntry) {
        NetworkInterceptHelper.enable();
        EquipmentOverride equipment = (EquipmentOverride) scriptEntry.getObject("equipment");
        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        List<PlayerTag> playersFor = (List<PlayerTag>) scriptEntry.getObject("for");
        ElementTag reset = scriptEntry.getElement("reset");
        DurationTag duration = scriptEntry.getObjectTag("duration");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("entities", entities), db("equipment", equipment), reset, duration, db("for", playersFor));
        }
        boolean isReset = reset != null && reset.asBoolean();
        for (PlayerTag player : playersFor) {
            HashMap<UUID, EquipmentOverride> playersMap = overrides.computeIfAbsent(player.getUUID(), (k) -> new HashMap<>());
            for (EntityTag entity : entities) {
                if (entity.isGeneric()) {
                    Debug.echoError(scriptEntry, "Cannot equip generic entity " + entity.identify() + "!");
                    continue;
                }
                LivingEntity livingEntity = entity.getLivingEntity();
                if (livingEntity == null) {
                    Debug.echoError(scriptEntry, "Cannot equip invalid/non-living entity " + entity.identify() + "!");
                    continue;
                }
                EquipmentOverride entityData;
                if (isReset) {
                    entityData = playersMap.remove(entity.getUUID());
                    if (playersMap.isEmpty()) {
                        overrides.remove(player.getUUID());
                    }
                }
                else {
                    entityData = playersMap.computeIfAbsent(entity.getUUID(), (k) -> new EquipmentOverride());
                    entityData.copyFrom(equipment);
                }
                if (entityData != null) {
                    if (entityData.cancelTask != null && !entityData.cancelTask.isCancelled()) {
                        entityData.cancelTask.cancel();
                        entityData.cancelTask = null;
                    }
                    if (duration != null && duration.getTicks() > 0) {
                        entityData.cancelTask = new BukkitRunnable() {
                            @Override
                            public void run() {
                                entityData.cancelTask = null;
                                HashMap<UUID, EquipmentOverride> playersMap = overrides.get(player.getUUID());
                                if (playersMap != null) {
                                    if (playersMap.remove(entity.getUUID()) != null) {
                                        if (playersMap.isEmpty()) {
                                            overrides.remove(player.getUUID());
                                        }
                                        NMSHandler.packetHelper.resetEquipment(player.getPlayerEntity(), livingEntity);
                                    }
                                }
                            }
                        }.runTaskLater(Denizen.getInstance(), duration.getTicks());
                    }
                }
                NMSHandler.packetHelper.resetEquipment(player.getPlayerEntity(), livingEntity);
            }
        }
    }
}

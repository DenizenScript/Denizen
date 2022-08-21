package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class PlayerEquipsArmorScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player equips|unequips armor|helmet|chestplate|leggings|boots
    // player equips|unequips <item>
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Triggers when a player (un)equips armor.
    //
    // @Warning This event is not reliable, and may miss some types of equipment changes or fire when equipment hasn't actually changed.
    //
    // @Context
    // <context.new_item> returns the ItemTag that is now in the slot.
    // <context.old_item> returns the ItemTag that used to be in the slot.
    // <context.slot> returns the name of the slot.
    //
    // @Player Always.
    //
    // -->

    public static HashMap<String, PlayerArmorChangeEvent.SlotType> slotsByName = new HashMap<>();
    public static HashMap<PlayerArmorChangeEvent.SlotType, String> namesBySlot = new HashMap<>();

    public static void registerSlot(String name, PlayerArmorChangeEvent.SlotType slot) {
        slotsByName.put(name, slot);
        namesBySlot.put(slot, name);
    }

    public PlayerEquipsArmorScriptEvent() {
        registerSlot("helmet", PlayerArmorChangeEvent.SlotType.HEAD);
        registerSlot("chestplate", PlayerArmorChangeEvent.SlotType.CHEST);
        registerSlot("leggings", PlayerArmorChangeEvent.SlotType.LEGS);
        registerSlot("boots", PlayerArmorChangeEvent.SlotType.FEET);
        registerCouldMatcher("player (equips|unequips) armor|helmet|chestplate|leggings|boots");
        registerCouldMatcher("player (equips|unequips) <item>");
    }

    public ItemTag oldItem;
    public ItemTag newItem;
    public PlayerArmorChangeEvent.SlotType slot;
    public PlayerTag player;

    @Override
    public boolean matches(ScriptPath path) {
        String type = path.eventArgLowerAt(1);
        String itemCompare = path.eventArgLowerAt(2);
        PlayerArmorChangeEvent.SlotType slotType = slotsByName.get(itemCompare);
        if (slotType != null && slot != slotType) {
            return false;
        }
        if (type.equals("equips")) {
            if (slotType != null) {
                if (newItem.getMaterial().getMaterial() == Material.AIR) {
                    return false;
                }
            }
            else if (!itemCompare.equals("armor") && !newItem.tryAdvancedMatcher(itemCompare)) {
                return false;
            }
        }
        else { // unequips
            if (slotType != null) {
                if (oldItem.getMaterial().getMaterial() == Material.AIR) {
                    return false;
                }
            }
            else if (!itemCompare.equals("armor") && !oldItem.tryAdvancedMatcher(itemCompare)) {
                return false;
            }
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "new_item":
                return newItem;
            case "old_item":
                return oldItem;
            case "slot":
                return new ElementTag(namesBySlot.get(slot));
        }
        return super.getContext(name);
    }

    public String simpleComparisonString(ItemStack stack) {
        if (stack == null) {
            return "null";
        }
        stack = stack.clone();
        stack.setAmount(1);
        stack.setDurability((short) 0);
        return CoreUtilities.toLowerCase(new ItemTag(stack).identify());
    }

    @EventHandler
    public void armorChangeEvent(PlayerArmorChangeEvent event) {
        if (EntityTag.isCitizensNPC(event.getPlayer())) {
            return;
        }
        if (simpleComparisonString(event.getOldItem()).equals(simpleComparisonString(event.getNewItem()))) {
            return;
        }
        newItem = new ItemTag(event.getNewItem());
        oldItem = new ItemTag(event.getOldItem());
        slot = event.getSlotType();
        player = new PlayerTag(event.getPlayer());
        fire(event);
    }
}

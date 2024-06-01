package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

public class PlayerArmorStandManipulateScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player changes armor stand item
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a player modifies an armor stand entity.
    //
    // @Switch from:<item> to only process the event if the item on the armor stand being interacted with matches the specified item matcher.
    // @Switch hand:<hand> to only process the event if the player is using a specific hand to interact with the armor stand. Available only on MC versions 1.19+.
    // @Switch to:<item> to only process the event if the item held by the player matches the specified item matcher.
    // @Switch slot:<slot> to only process the event if the armor stand's item slot that was interacted with is the specified slot.
    // @Switch armor_stand:<entity> to only process the event if the armor stand being interacted with matches the specified entity matcher.
    //
    // @Context
    // <context.armor_stand_item> returns the ItemTag being interacted with on the armor stand.
    // <context.entity> returns an EntityTag of the armor stand.
    // <context.hand> returns an ElementTag of the hand used by the player to interact with the armor stand, can be either HAND or OFF_HAND. Available only on MC versions 1.19+.
    // <context.player_item> returns the ItemTag held by the player.
    // <context.slot> returns an ElementTag of the armor stand's item slot that was interacted with. Valid equipment slot values can be found at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/EquipmentSlot.html>.
    //
    // @Player Always.
    // -->

    public PlayerArmorStandManipulateScriptEvent() {
        registerCouldMatcher("player changes armor stand item");
        registerSwitches("from", "to", "hand", "slot", "armor_stand");
    }

    public PlayerArmorStandManipulateEvent event;
    public EntityTag entity;
    public ItemTag armorStandItem;
    public ItemTag playerItem;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getRightClicked().getLocation())) {
            return false;
        }
        if (!path.tryObjectSwitch("from", armorStandItem)) {
            return false;
        }
        if (!path.tryObjectSwitch("armor_stand", entity)) {
            return false;
        }
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && !runGenericSwitchCheck(path, "hand", event.getHand().name())) {
            return false;
        }
        if (!path.tryObjectSwitch("to", playerItem)) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "slot", event.getSlot().name())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "armor_stand_item" -> armorStandItem;
            case "entity" -> entity;
            case "hand" -> NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) ? new ElementTag(event.getHand()) : null;
            case "player_item" -> playerItem;
            case "slot" -> new ElementTag(event.getSlot());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onPlayerManipulatesArmorStand(PlayerArmorStandManipulateEvent event) {
        this.event = event;
        entity = new EntityTag(event.getRightClicked());
        playerItem = new ItemTag(event.getPlayerItem());
        armorStandItem = new ItemTag(event.getArmorStandItem());
        fire(event);
    }
}

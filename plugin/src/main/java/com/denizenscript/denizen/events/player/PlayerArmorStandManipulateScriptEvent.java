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
    // player edits armor stand
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a player manipulates an armor stand entity.
    //
    // @Context
    // <context.armor_stand_item> returns the ItemTag held by the armor stand.
    // <context.entity> returns an EntityTag of the armor stand.
    // <context.hand> returns an ElementTag of the hand used by the player to interact with the armor stand, can be either HAND or OFF_HAND. Available only on MC versions 1.19+.
    // <context.player_item> returns the ItemTag held by the player.
    // <context.slot> returns an ElementTag of the armor stand's item slot that was interacted with. Valid equipment slot values can be found at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/EquipmentSlot.html>.
    //
    // @Player Always.
    // -->

    public PlayerArmorStandManipulateScriptEvent() {
        registerCouldMatcher("player edits armor stand");
    }

    public PlayerArmorStandManipulateEvent event;
    public EntityTag entity;
    public ItemTag armorStandItem;
    public ItemTag playerItem;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getPlayer().getLocation())) {
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

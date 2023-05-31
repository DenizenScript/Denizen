package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PrePlayerAttackEntityScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player tries to attack <entity>
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Cancellable true
    //
    // @Switch with:<item> to only process the event if the player attacks the entity with the specified item.
    //
    // @Triggers when the player tries to attack an entity. This occurs before any of the damage logic, so cancelling this event will prevent any sort of sounds from being played when attacking.
    //
    // @Context
    // <context.entity> returns the entity that was attacked in this event.
    // <context.will_attack> returns whether this entity would be attacked normally.
    // Entities like falling sand will return false because their entity type does not allow them to be attacked.
    // Note: there may be other factors (invulnerability, etc.) that will prevent this entity from being attacked that this event does not cover.
    //
    // @Player Always.
    //
    // -->

    public PrePlayerAttackEntityScriptEvent() {
        registerCouldMatcher("player tries to attack <entity>");
        registerSwitches("with");
    }

    public PrePlayerAttackEntityEvent event;
    public EntityTag entity;
    public ItemTag item;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(4, entity)) {
            return false;
        }
        if (!path.tryObjectSwitch("with", new ItemTag(event.getPlayer().getEquipment().getItemInMainHand()))) {
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
            case "entity" -> entity;
            case "will_attack" -> new ElementTag(event.willAttack());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onPrePlayerAttackEntity(PrePlayerAttackEntityEvent event) {
        this.event = event;
        entity = new EntityTag(event.getAttacked());
        fire(event);
    }
}

package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingPlaceEvent;

public class PlayerPlacesHangingScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player places <hanging>
    //
    // @Switch item:<item> to only process the event when the hangable item matches the given ItemTag matcher.
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a hanging entity (painting or itemframe) is placed.
    //
    // @Context
    // <context.hanging> returns the EntityTag of the hanging.
    // <context.location> returns the LocationTag of the block the hanging was placed on.
    // <context.item> returns the ItemTag that was placed.
    //
    // @Player Always.
    //
    // -->

    public PlayerPlacesHangingScriptEvent() {
        registerCouldMatcher("player places <hanging>");
        registerSwitches("item");
    }

    public EntityTag hanging;
    public ItemTag item;
    public LocationTag location;
    public HangingPlaceEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        String hangCheck = path.eventArgLowerAt(2);
        if (!hanging.tryAdvancedMatcher(hangCheck)) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!path.tryObjectSwitch("item", item)) {
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
        switch (name) {
            case "hanging": return hanging;
            case "location": return location;
            case "item": return item;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerPlacesHanging(HangingPlaceEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        Entity hangingEntity = event.getEntity();
        EntityTag.rememberEntity(hangingEntity);
        hanging = new EntityTag(hangingEntity);
        location = new LocationTag(event.getBlock().getLocation());
        item = new ItemTag(event.getItemStack());
        this.event = event;
        fire(event);
        EntityTag.forgetEntity(hangingEntity);
    }
}

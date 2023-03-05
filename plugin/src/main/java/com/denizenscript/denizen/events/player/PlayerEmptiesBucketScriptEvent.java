package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

public class PlayerEmptiesBucketScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player empties bucket
    // player empties <item>
    //
    // @Group Player
    //
    // @Location true
    //
    // @Triggers when a player empties a bucket.
    //
    // @Cancellable true
    //
    // @Context
    // <context.item> returns the ItemTag of the bucket being emptied (just material, other properties are lost - use 'player.item_in_hand' if you need full data).
    // <context.location> returns the LocationTag of the block clicked with the bucket.
    // <context.relative> returns the LocationTag of the block in front of the clicked block.
    //
    // @Player Always.
    //
    // -->

    public PlayerEmptiesBucketScriptEvent() {
        registerCouldMatcher("player empties bucket");
        registerCouldMatcher("player empties <item>");
    }


    public ItemTag item;
    public MaterialTag material;
    public LocationTag location;
    public PlayerBucketEmptyEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        String iTest = path.eventArgLowerAt(2);
        if ((!iTest.equals("bucket") && !item.tryAdvancedMatcher(iTest)) || !runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event != null ? EntityTag.getPlayerFrom(event.getPlayer()) : null, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "location":
                return location;
            case "relative":
                return new LocationTag(event.getBlockClicked().getRelative(event.getBlockFace()).getLocation());
            case "item":
                return item;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        location = new LocationTag(event.getBlockClicked().getLocation());
        item = new ItemTag(event.getBucket());
        this.event = event;
        fire(event);
    }
}

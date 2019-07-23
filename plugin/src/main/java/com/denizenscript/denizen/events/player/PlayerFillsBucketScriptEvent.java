package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketFillEvent;

public class PlayerFillsBucketScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player fills bucket
    // player fills <bucket>
    //
    // @Regex ^on player fills [^\s]+$
    // @Switch in <area>
    //
    // @Triggers when a player fills a bucket.
    //
    // @Cancellable true
    //
    // @Context
    // <context.item> returns the ItemTag of the filled bucket.
    // <context.location> returns the LocationTag of the block clicked with the bucket.
    // <context.material> returns the MaterialTag of the LocationTag.
    //
    // -->


    public PlayerFillsBucketScriptEvent() {
        instance = this;
    }

    public static PlayerFillsBucketScriptEvent instance;

    public EntityTag entity;
    public ItemTag item;
    public MaterialTag material;
    public LocationTag location;
    public PlayerBucketFillEvent event;


    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player fills");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String iTest = path.eventArgLowerAt(2);
        return (iTest.equals("bucket") || tryItem(item, iTest))
                && runInCheck(path, location);
    }

    @Override
    public String getName() {
        return "PlayerFillsBucket";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        // TODO: Store the player / npc?
        return new BukkitScriptEntryData(event != null ? EntityTag.getPlayerFrom(event.getPlayer()) : null,
                entity.isNPC() ? entity.getDenizenNPC() : null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("item")) {
            return item;
        }
        else if (name.equals("material")) {
            return material;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        entity = new EntityTag(event.getPlayer());
        location = new LocationTag(event.getBlockClicked().getLocation());
        item = new ItemTag(event.getItemStack());
        material = new MaterialTag(event.getBlockClicked());
        this.event = event;
        fire(event);
    }
}

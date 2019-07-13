package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

public class PlayerEmptiesBucketScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player empties bucket
    // player empties <bucket>
    //
    // @Regex ^on player empties [^\s]+$
    // @Switch in <area>
    //
    // @Triggers when a player empties a bucket.
    //
    // @Cancellable true
    //
    // @Context
    // <context.item> returns the dItem of the bucket being emptied.
    // <context.location> returns the dLocation of the block clicked with the bucket.
    // <context.relative> returns the dLocation of the block in front of the clicked block.
    //
    // -->


    public PlayerEmptiesBucketScriptEvent() {
        instance = this;
    }

    public static PlayerEmptiesBucketScriptEvent instance;

    public dEntity entity;
    public dItem item;
    public dMaterial material;
    public dLocation location;
    public dLocation relative;
    public PlayerBucketEmptyEvent event;


    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player empties");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String iTest = path.eventArgLowerAt(2);
        return (iTest.equals("bucket") || tryItem(item, iTest))
                && runInCheck(path, location);
    }

    @Override
    public String getName() {
        return "PlayerEmptiesBucket";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event != null ? dEntity.getPlayerFrom(event.getPlayer()) : null, null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("relative")) {
            return relative;
        }
        else if (name.equals("item")) {
            return item;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        entity = new dEntity(event.getPlayer());
        location = new dLocation(event.getBlockClicked().getLocation());
        relative = new dLocation(event.getBlockClicked().getRelative(event.getBlockFace()).getLocation());
        item = new dItem(event.getBucket());
        this.event = event;
        fire(event);
    }
}

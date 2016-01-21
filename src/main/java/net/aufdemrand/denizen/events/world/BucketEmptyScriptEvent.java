package net.aufdemrand.denizen.events.world;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

public class BucketEmptyScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player empties bucket (in <area>)
    // player empties <bucket> (in <area>)
    //
    // @Regex ^on player empties [^\s]+( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
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


    public BucketEmptyScriptEvent() {
        instance = this;
    }

    public static BucketEmptyScriptEvent instance;

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
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String iTest = CoreUtilities.getXthArg(2, lower);
        return (iTest.equals("bucket") || tryItem(item, iTest))
                && runInCheck(scriptContainer, s, lower, location);
    }

    @Override
    public String getName() {
        return "BucketEmpty";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerBucketEmptyEvent.getHandlerList().unregister(this);
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
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

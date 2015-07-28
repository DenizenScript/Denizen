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
import org.bukkit.event.player.PlayerBucketFillEvent;

import java.util.HashMap;

public class BucketFillScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player fills bucket (in <area>)
    // player fills <bucket> (in <area>)
    //
    // @Triggers when a player fills a bucket.
    //
    // @Cancellable true
    //
    // @Context
    // <context.item> returns the dItem of the filled bucket.
    // <context.location> returns the dLocation of the block clicked with the bucket.
    // <context.material> returns the dMaterial of the dLocation.
    //
    // -->


    public BucketFillScriptEvent() {
        instance = this;
    }

    public static BucketFillScriptEvent instance;

    public dEntity entity;
    public dItem item;
    public dMaterial material;
    public dLocation location;
    public dLocation relative;
    public PlayerBucketFillEvent event;


    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player fills");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String iTest = CoreUtilities.getXthArg(2, lower);
        return tryItem(item, iTest)
                && runInCheck(scriptContainer, s, lower, location);
    }

    @Override
    public String getName() {
        return "BucketFill";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerBucketFillEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        // TODO: Store the player / npc?
        return new BukkitScriptEntryData(event != null ? dEntity.getPlayerFrom(event.getPlayer()) : null,
                entity.isNPC() ? entity.getDenizenNPC() : null);
    }

    @Override
    public dObject getContext(String name) {
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

    @EventHandler(ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        entity = new dEntity(event.getPlayer());
        location = new dLocation(event.getBlockClicked().getLocation());
        item = new dItem(event.getItemStack());
        material = dMaterial.getMaterialFrom(event.getBlockClicked().getType(), event.getBlockClicked().getData());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

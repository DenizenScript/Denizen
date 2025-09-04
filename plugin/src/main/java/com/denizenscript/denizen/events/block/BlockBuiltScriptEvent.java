package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockCanBuildEvent;

public class BlockBuiltScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <block> being built (on <block>)
    //
    // @Group Block
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when an attempt is made to build a block on another block. Not necessarily caused by players. Does not normally fire when players place blocks. Prefer <@link event player places block> for that.
    //
    // @Context
    // <context.location> returns the LocationTag of the block the player is trying to build on.
    // <context.old_material> returns the MaterialTag of the block the player is trying to build on.
    // <context.new_material> returns the MaterialTag of the block the player is trying to build.
    // <context.buildable> returns whether the block can physically be placed where it was when the event was fired.
    //
    // @Determine
    // "BUILDABLE" to allow the building.
    //
    // @Player when the event is triggered in relation to a player that is causing the block build.
    //
    // -->

    public BlockBuiltScriptEvent() {
        registerCouldMatcher("<block> being built (on <block>)");
        this.<BlockBuiltScriptEvent>registerTextDetermination("buildable", (evt) -> {
            evt.event.setBuildable(true);
        });
    }

    public LocationTag location;
    public MaterialTag oldMaterial;
    public MaterialTag newMaterial;
    public BlockCanBuildEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!path.tryArgObject(4, oldMaterial)) {
            return false;
        }
        if (!path.tryArgObject(0, newMaterial)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public void cancellationChanged() {
        event.setBuildable(!cancelled);
        super.cancellationChanged();
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "location" -> location;
            case "new_material" -> newMaterial;
            case "old_material" -> oldMaterial;
            case "buildable" -> new ElementTag(event.isBuildable());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onBlockBuilt(BlockCanBuildEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        oldMaterial = new MaterialTag(event.getBlock());
        newMaterial = new MaterialTag(event.getBlockData());
        cancelled = !event.isBuildable();
        this.event = event;
        fire(event);
    }
}

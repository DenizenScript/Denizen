package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

import java.lang.reflect.Field;

public class BlockPhysicsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <block> physics
    //
    // @Group Block
    //
    // @Location true
    //
    // @Warning This event may fire very rapidly.
    //
    // @Switch adjacent:<block> to only process the event if the block or an immediately adjacent block (up/down/n/e/s/w) matches the LocationTag matcher specified. This can be useful to prevent blocks from breaking.
    //
    // @Cancellable true
    //
    // @Triggers when a block's physics update.
    //
    // @Context
    // <context.location> returns a LocationTag of the block the physics is affecting.
    // <context.new_material> returns a MaterialTag of what the block is becoming.
    //
    // -->

    public BlockPhysicsScriptEvent() {
        registerCouldMatcher("<block> physics");
        registerSwitches("adjacent");
    }


    public LocationTag location;
    public MaterialTag material;
    public BlockPhysicsEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!path.tryArgObject(0, material)) {
            return false;
        }
        String adjacent = path.switches.get("adjacent");
        if (adjacent != null) {
            if (!material.tryAdvancedMatcher(adjacent)) {
                Block block = location.getBlock();
                if (!new LocationTag(block.getRelative(0, 1, 0).getLocation()).tryAdvancedMatcher(adjacent)
                        && !new LocationTag(block.getRelative(0, -1, 0).getLocation()).tryAdvancedMatcher(adjacent)
                        && !new LocationTag(block.getRelative(1, 0, 0).getLocation()).tryAdvancedMatcher(adjacent)
                        && !new LocationTag(block.getRelative(-1, 0, 0).getLocation()).tryAdvancedMatcher(adjacent)
                        && !new LocationTag(block.getRelative(0, 0, 1).getLocation()).tryAdvancedMatcher(adjacent)
                        && !new LocationTag(block.getRelative(0, 0, -1).getLocation()).tryAdvancedMatcher(adjacent)) {
                    return false;
                }
            }
        }
        return super.matches(path);
    }

    public static Field PHYSICS_EVENT_DATA = ReflectionHelper.getFields(BlockPhysicsEvent.class).getFirstOfType(BlockData.class);

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "location": return location;
            case "new_material":
                try {
                    BlockData data = (BlockData) PHYSICS_EVENT_DATA.get(event);
                    return new MaterialTag(data);
                }
                catch (Throwable ex) {
                    Debug.echoError(ex);
                }
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Material changedType = event.getChangedType();
        if (changedType == Material.REDSTONE_WIRE || changedType == Material.COMPARATOR || changedType == Material.REPEATER) {
            return;
        }
        location = new LocationTag(event.getBlock().getLocation());
        material = new MaterialTag(location.getBlock());
        this.event = event;
        fire(event);
    }
}

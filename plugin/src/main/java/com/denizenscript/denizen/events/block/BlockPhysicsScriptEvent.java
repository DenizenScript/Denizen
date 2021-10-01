package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

import java.lang.reflect.Field;

public class BlockPhysicsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // block physics
    // <material> physics
    //
    // @Regex ^on [^\s]+ physics$
    //
    // @Group Block
    //
    // @Location true
    //
    // @Warning This event may fire very rapidly.
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
        instance = this;
    }

    public static BlockPhysicsScriptEvent instance;

    public LocationTag location;
    public MaterialTag material;
    public BlockPhysicsEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventArgLowerAt(1).equals("physics")) {
            return false;
        }
        if (!couldMatchBlock(path.eventArgLowerAt(0))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!tryMaterial(material, path.eventArgLowerAt(0))) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "BlockPhysics";
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

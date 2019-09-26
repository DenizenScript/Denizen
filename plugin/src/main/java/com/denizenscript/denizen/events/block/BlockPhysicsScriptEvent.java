package com.denizenscript.denizen.events.block;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.blocks.MaterialCompat;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

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
    // @Switch in <area>
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
        return path.eventArgLowerAt(1).equals("physics");
    }

    @Override
    public boolean matches(ScriptPath path) {

        if (!runInCheck(path, location)) {
            return false;
        }

        String mat = path.eventArgLowerAt(0);
        return tryMaterial(material, mat);

    }

    @Override
    public String getName() {
        return "BlockPhysics";
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("new_material")) {
            return new MaterialTag(event.getChangedType());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Material changedType = event.getChangedType();
        if (changedType == Material.REDSTONE_WIRE || MaterialCompat.isComparator(changedType)
                || MaterialCompat.isRepeater(changedType)) {
            return;
        }
        location = new LocationTag(event.getBlock().getLocation());
        material = new MaterialTag(location.getBlock());
        this.event = event;
        fire(event);
    }
}

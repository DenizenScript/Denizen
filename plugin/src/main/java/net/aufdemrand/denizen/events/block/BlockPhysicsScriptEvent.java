package net.aufdemrand.denizen.events.block;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.MaterialCompat;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

public class BlockPhysicsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // block physics (in <area>)
    // <material> physics (in <area>)
    //
    // @Regex ^on [^\s]+ physics( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Warning This event may fire very rapidly.
    //
    // @Cancellable true
    //
    // @Triggers when a block's physics update.
    //
    // @Context
    // <context.location> returns a dLocation of the block the physics is affecting.
    // <context.new_material> returns a dMaterial of what the block is becoming.
    //
    // -->

    public BlockPhysicsScriptEvent() {
        instance = this;
    }

    public static BlockPhysicsScriptEvent instance;

    public dLocation location;
    public dMaterial new_material;
    public dMaterial material;
    public BlockPhysicsEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("physics");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);

        if (!runInCheck(scriptContainer, s, lower, location)) {
            return false;
        }

        String mat = CoreUtilities.getXthArg(0, lower);
        return tryMaterial(material, mat);

    }

    @Override
    public String getName() {
        return "BlockPhysics";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        BlockPhysicsEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("new_material")) {
            return new_material;
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
        location = new dLocation(event.getBlock().getLocation());
        new_material = dMaterial.getMaterialFrom(changedType);
        material = dMaterial.getMaterialFrom(location.getBlock().getType(), location.getBlock().getData());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

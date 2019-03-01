package net.aufdemrand.denizen.events.block;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockCanBuildEvent;

public class BlockBuiltScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // block being built (on <material>) (in <area>)
    // <material> being built (on <material>) (in <area>)
    //
    // @Regex ^on [^\s]+ being built( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when an attempt is made to build a block on another block. Not necessarily caused by players.
    //
    // @Context
    // <context.location> returns the dLocation of the block the player is trying to build on.
    // <context.old_material> returns the dMaterial of the block the player is trying to build on.
    // <context.new_material> Deprecated, returns the dMaterial of the block the player is trying to build.
    //
    // @Determine
    // "BUILDABLE" to allow the building.
    //
    // -->

    public BlockBuiltScriptEvent() {
        instance = this;
    }

    public static BlockBuiltScriptEvent instance;
    public dLocation location;
    public dMaterial old_material;
    public dMaterial new_material;
    public BlockCanBuildEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.contains(" being built");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;

        if (!runInCheck(path, location)) {
            return false;
        }

        String mat1 = CoreUtilities.getXthArg(0, lower);
        if (!tryMaterial(new_material, mat1)) {
            return false;
        }

        String mat2 = CoreUtilities.getXthArg(4, lower);
        if (mat2.length() > 0 && !tryMaterial(old_material, mat2)) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "BlockBuilt";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        BlockCanBuildEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        String lower = CoreUtilities.toLowerCase(determination);
        if (lower.equals("buildable")) {
            cancelled = false;
        }
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
        else if (name.equals("old_material")) {
            return old_material;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onBlockBuilt(BlockCanBuildEvent event) {
        location = new dLocation(event.getBlock().getLocation());
        old_material = new dMaterial(event.getBlock());
        new_material = new dMaterial(event.getMaterial()); // Deprecated because it doesn't have proper data
        cancelled = !event.isBuildable();
        this.event = event;
        fire();
        event.setBuildable(!cancelled);
    }
}

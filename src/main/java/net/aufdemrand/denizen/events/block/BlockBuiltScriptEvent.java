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

import java.util.HashMap;

public class BlockBuiltScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // block being built (on <material>) (in <area>)
    // <material> being built (on <material>) (in <area>)
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
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);

        if (!runInCheck(scriptContainer, s, lower, location)) {
            return false;
        }

        String mat1 = CoreUtilities.getXthArg(0, lower);
        if (!tryMaterial(old_material, mat1)) {
            return false;
        }

        String mat2 = CoreUtilities.getXthArg(4, lower);
        return tryMaterial(new_material, mat2);
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
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("location", location);
        context.put("new_material", new_material); // Deprecated because it doesn't have proper data
        context.put("old_material", old_material);
        return context;
    }

    @EventHandler
    public void onBlockBuilt(BlockCanBuildEvent event) {
        location = new dLocation(event.getBlock().getLocation());
        old_material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());
        new_material = dMaterial.getMaterialFrom(event.getMaterial());  // Deprecated because it doesn't have proper data
        cancelled = !event.isBuildable();
        this.event = event;
        fire();
        event.setBuildable(!cancelled);
    }
}

package net.aufdemrand.denizen.events.world;

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
import org.bukkit.event.block.BlockFromToEvent;

public class LiquidSpreadScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // liquid spreads (in <area>)
    // <liquid block> spreads (in <area>)
    //
    // @Regex ^on [^\s]+ spreads( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when a liquid block spreads or dragon egg moves.
    //
    // @Context
    // <context.destination> returns the dLocation the block spread to.
    // <context.location> returns the dLocation the block spread location.
    // <context.material> returns the dMaterial of the block that spread.
    //
    // -->


    public LiquidSpreadScriptEvent() {
        instance = this;
    }

    public static LiquidSpreadScriptEvent instance;
    public dMaterial material;
    public dLocation location;
    public dLocation destination;
    public BlockFromToEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return CoreUtilities.xthArgEquals(1, lower, "spreads") && !lower.startsWith("block");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String mat = CoreUtilities.getXthArg(0, lower);
        return (mat.equals("liquid") || tryMaterial(material, mat))
                && (runInCheck(scriptContainer, s, lower, location)
                || runInCheck(scriptContainer, s, lower, destination));
    }

    @Override
    public String getName() {
        return "LiquidSpreads";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        BlockFromToEvent.getHandlerList().unregister(this);
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
        else if (name.equals("destination")) {
            return destination;
        }
        else if (name.equals("material")) {
            return material;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onLiquidSpreads(BlockFromToEvent event) {
        destination = new dLocation(event.getToBlock().getLocation());
        location = new dLocation(event.getBlock().getLocation());
        material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

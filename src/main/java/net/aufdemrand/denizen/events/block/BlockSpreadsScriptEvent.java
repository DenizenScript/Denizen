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
import org.bukkit.event.block.BlockSpreadEvent;

import java.util.HashMap;

public class BlockSpreadsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // block spreads (in <area>)
    // <material> spreads (in <area>)
    //
    // @Cancellable true
    //
    // @Triggers when a block spreads based on world conditions,
    //           e.g. when fire spreads, when mushrooms spread
    //
    // @Context
    // <context.location> returns the dLocation of the block that spread.
    // <context.material> returns the dMaterial of the block that spread.
    //
    // -->

    public BlockSpreadsScriptEvent() {
        instance = this;
    }

    public static BlockSpreadsScriptEvent instance;
    public dLocation location;
    public dMaterial material;
    public BlockSpreadEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("spreads") && !lower.startsWith("liquid");
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
        return "BlockSpreads";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        BlockSpreadEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("location", location);
        context.put("material", material);
        return context;
    }

    @EventHandler
    public void onBlockSpreads(BlockSpreadEvent event) {
        location = new dLocation(event.getBlock().getLocation());
        material = dMaterial.getMaterialFrom(event.getSource().getType(), event.getSource().getData());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

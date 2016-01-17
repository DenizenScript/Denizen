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

public class BlockSpreadsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // block spreads (in <area>)
    // <material> spreads (in <area>)
    //
    // @Regex ^on [^\s]+ spreads( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when a block spreads based on world conditions, EG, when fire spreads, or when mushrooms spread.
    //
    // @Context
    // <context.source_location> returns the dLocation of the block that spread.
    // <context.location> returns the dLocation of the new block.
    // <context.material> returns the dMaterial of the block that spread.
    //
    // -->

    public BlockSpreadsScriptEvent() {
        instance = this;
    }

    public static BlockSpreadsScriptEvent instance;
    public dLocation location;
    public dLocation source;
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
    public dObject getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("material")) {
            return material;
        }
        else if (name.equals("source_location")) {
            return source;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onBlockSpreads(BlockSpreadEvent event) {
        source = new dLocation(event.getBlock().getLocation());
        location = new dLocation(event.getBlock().getLocation());
        material = dMaterial.getMaterialFrom(event.getSource().getType(), event.getSource().getData());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

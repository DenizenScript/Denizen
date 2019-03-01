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
import org.bukkit.event.block.BlockFormEvent;

public class BlockFormsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // block forms (in <area>)
    // <block> forms (in <area>)
    //
    // @Regex ^on [^\s]+ forms( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when a block is formed based on world conditions, EG, when snow forms in a snow storm or ice forms in a cold biome.
    //
    // @Context
    // <context.location> returns the dLocation the block that is forming.
    // <context.material> returns the dMaterial of the block that is forming.
    //
    // -->

    public BlockFormsScriptEvent() {
        instance = this;
    }

    public static BlockFormsScriptEvent instance;
    public dLocation location;
    public dMaterial material;
    public BlockFormEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("forms");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;
        if (!runInCheck(path, location)) {
            return false;
        }

        String mat = CoreUtilities.getXthArg(0, lower);
        return tryMaterial(material, mat);
    }

    @Override
    public String getName() {
        return "BlockForms";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        BlockFormEvent.getHandlerList().unregister(this);
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
        return super.getContext(name);
    }

    @EventHandler
    public void onBlockForms(BlockFormEvent event) {

        location = new dLocation(event.getBlock().getLocation());
        material = new dMaterial(event.getNewState());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.scripts.containers.core.BukkitWorldScriptHelper;
import net.aufdemrand.denizencore.events.OldSmartEvent;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RedstoneSmartEvent implements OldSmartEvent, Listener {


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    @Override
    public boolean shouldInitialize(Set<String> events) {

        for (String event : events) {

            Matcher m = Pattern.compile("on redstone recalculated", Pattern.CASE_INSENSITIVE)
                    .matcher(event);

            if (m.matches()) {
                // Straight-forward enough, just pass from any match.
                return true;
            }
        }
        return false;
    }


    @Override
    public void _initialize() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
        dB.log("Loaded Redstone SmartEvent.");
    }


    @Override
    public void breakDown() {
        BlockRedstoneEvent.getHandlerList().unregister(this);
    }

    //////////////
    //  MECHANICS
    ///////////

    // <--[event]
    // @Events
    // redstone recalculated
    //
    // @Warning This event fires very very rapidly!
    //
    // @Triggers when a redstone wire is recalculated.
    // @Context
    // <context.location> returns the location of the block.
    // <context.old_current> returns what the redstone power level was.
    // <context.new_current> returns what the redstone power level is becoming.
    //
    // @Determine
    // "CANCELLED" to stop the block from falling.
    //
    // -->
    @EventHandler
    public void onBlockRedstone(BlockRedstoneEvent event) {
        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("old_current", new Element(event.getOldCurrent()));
        context.put("new_current", new Element(event.getNewCurrent()));
        String determination = BukkitWorldScriptHelper.doEvents(Arrays.asList("redstone recalculated"), null, null, context, true);
        Element det = new Element(determination);
        if (det.isInt())
            event.setNewCurrent(det.asInt());
    }
}

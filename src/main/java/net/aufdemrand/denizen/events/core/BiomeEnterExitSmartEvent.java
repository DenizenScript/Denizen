package net.aufdemrand.denizen.events.core;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.events.OldEventManager;
import net.aufdemrand.denizencore.events.OldSmartEvent;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.block.Biome;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BiomeEnterExitSmartEvent implements OldSmartEvent, Listener {


    ///////////////////
    // SMARTEVENT METHODS
    ///////////////


    ArrayList<String> cuboids_to_watch = new ArrayList<String>();

    @Override
    public boolean shouldInitialize(Set<String> events) {

        // Loop through event names from loaded world script events
        for (String event : events) {

            // Use a regex pattern to narrow down matches
            Matcher m = Pattern.compile("on player (?:enters|exits) (biome|\\w+)", Pattern.CASE_INSENSITIVE)
                    .matcher(event);

            if (m.matches()) {
                // Any match is to be considered valid enough!
                return true;
            }
        }

        return false;

    }


    @Override
    public void _initialize() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
        dB.log("Loaded Biome Enter & Exit SmartEvent.");
    }


    @Override
    public void breakDown() {
        PlayerMoveEvent.getHandlerList().unregister(this);
    }

    //////////////
    //  MECHANICS
    ///////////

    // <--[event]
    // @Events
    // player enters <biome>
    // player exits <biome>
    // player enters biome
    // player exits biome
    //
    // @Regex on player (?:enters|exits) (biome|\w+)
    //
    // @Warning Cancelling this event will fire a similar event immediately after.
    //
    // @Triggers when a player enters or exits a biome.
    // @Context
    // <context.from> returns the block location moved from.
    // <context.to> returns the block location moved to.
    // <context.old_biome> returns an element of the biome being left.
    // <context.new_biome> returns an element of the biome being entered.
    //
    // @Determine
    // "CANCELLED" to stop the player from moving.
    //
    //
    // -->
    @EventHandler
    public void playerMoveEvent(PlayerMoveEvent event) {

        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) return;

        // Compare the two biomes
        Biome from = event.getFrom().getWorld().getBiome(event.getFrom().getBlockX(), event.getFrom().getBlockZ());
        Biome to = event.getTo().getWorld().getBiome(event.getTo().getBlockX(), event.getTo().getBlockZ());
        if (!from.equals(to)) {

            // Create contexts
            Map<String, dObject> context = new HashMap<String, dObject>();
            context.put("from", new dLocation(event.getFrom()));
            context.put("to", new dLocation(event.getFrom()));
            context.put("old_biome", new Element(from.name()));
            context.put("new_biome", new Element(to.name()));

           List<String> determinations = OldEventManager.doEvents(Arrays.asList(
                    "player enters biome", "player exits biome",
                    "player enters " + to.name(), "player exits " + from.name()
                ), new BukkitScriptEntryData(dEntity.getPlayerFrom(event.getPlayer()), null), context, true);

            for (String determination: determinations) {
                if (determination.toUpperCase().startsWith("CANCELLED"))
                    event.setCancelled(true);
            }
        }
    }
}

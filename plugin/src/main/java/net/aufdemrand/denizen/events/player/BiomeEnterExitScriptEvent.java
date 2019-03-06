package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class BiomeEnterExitScriptEvent extends BukkitScriptEvent implements Listener {

    // TODO: in area?
    // <--[event]
    // @Events
    // player enters <biome>
    // player exits <biome>
    // player enters biome
    // player exits biome
    //
    // @Regex ^on player (enters|exits) [^\s]+$
    //
    // @Warning Cancelling this event will fire a similar event immediately after.
    //
    // @Cancellable true
    //
    // @Triggers when a player enters or exits a biome.
    //
    // @Context
    // <context.from> returns the block location moved from.
    // <context.to> returns the block location moved to.
    // <context.old_biome> returns an element of the biome being left.
    // <context.new_biome> returns an element of the biome being entered.
    //
    // -->

    public BiomeEnterExitScriptEvent() {
        instance = this;
    }

    public static BiomeEnterExitScriptEvent instance;

    public dLocation from;
    public dLocation to;
    public Element old_biome;
    public Element new_biome;
    public PlayerMoveEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player enters")
                || lower.startsWith("player exits");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String lower = path.eventLower;
        String biome_test = lower.substring(lower.lastIndexOf(' ') + 1);
        String direction = lower.substring(lower.indexOf(' ') + 1, lower.lastIndexOf(' ')).trim();

        return biome_test.equals("biome")
                || (direction.equals("enters") && biome_test.equals(CoreUtilities.toLowerCase(new_biome.toString())))
                || (direction.equals("exits") && biome_test.equals(CoreUtilities.toLowerCase(old_biome.toString())));
    }

    @Override
    public String getName() {
        return "BiomeEnterExit";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        // TODO: Store the player / npc?
        return new BukkitScriptEntryData(event != null ? dEntity.getPlayerFrom(event.getPlayer()) : null, null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("to")) {
            return to;
        }
        else if (name.equals("from")) {
            return from;
        }
        else if (name.equals("old_biome")) {
            return old_biome;
        }
        else if (name.equals("new_biome")) {
            return new_biome;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerEntersExitsBiome(PlayerMoveEvent event) {
        from = new dLocation(event.getFrom());
        to = new dLocation(event.getTo());
        old_biome = new Element(from.getBlock().getBiome().name());
        new_biome = new Element(to.getBlock().getBiome().name());
        if (old_biome.identify().equals(new_biome.identify())) {
            return;
        }
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

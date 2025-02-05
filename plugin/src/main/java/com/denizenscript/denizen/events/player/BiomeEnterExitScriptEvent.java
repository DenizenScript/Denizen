package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.BiomeTag;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.Registry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class BiomeEnterExitScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player enters|exits biome
    //
    // @Group Player
    //
    // @Switch biome:<name> to only process the event when a specific biome is being entered.
    //
    // @Location true
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
    // <context.old_biome> returns the biome being left.
    // <context.new_biome> returns the biome being entered.
    //
    // @Player Always.
    //
    // -->

    public BiomeEnterExitScriptEvent() {
        registerCouldMatcher("player enters|exits <biome>");
        registerSwitches("biome");
    }


    public LocationTag from;
    public LocationTag to;
    public BiomeTag old_biome;
    public BiomeTag new_biome;
    public PlayerMoveEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!super.couldMatch(path)) {
            return false;
        }
        if (!path.eventArgLowerAt(2).equals("biome") && !couldMatchRegistry(path.eventArgLowerAt(2), Registry.BIOME)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        String biome_test = path.eventArgAt(2);
        String direction = path.eventArgAt(1);

        if (!runInCheck(path, from) && !runInCheck(path, to)) {
            return false;
        }
        BiomeTag biome = direction.equals("enters") ? new_biome : (direction.equals("exits") ? old_biome : null);
        if (biome == null) {
            return false;
        }
        String biomeKey = Utilities.namespacedKeyToString(biome.getBiome().getKey());
        if (!biome_test.equals("biome") && !biome_test.equals(biomeKey)) {
            return false;
        }
        if (!runGenericSwitchCheck(path, "biome", biomeKey)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(EntityTag.getPlayerFrom(event.getPlayer()), null);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "to":
                return to;
            case "from":
                return from;
            case "old_biome":
                return old_biome;
            case "new_biome":
                return new_biome;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerEntersExitsBiome(PlayerMoveEvent event) {
        if (LocationTag.isSameBlock(event.getFrom(), event.getTo())) {
            return;
        }
        if (!Utilities.isLocationYSafe(event.getFrom()) || !Utilities.isLocationYSafe(event.getTo())) {
            return;
        }
        from = new LocationTag(event.getFrom());
        to = new LocationTag(event.getTo());
        old_biome = new BiomeTag(NMSHandler.instance.getBiomeAt(from.getBlock()));
        new_biome = new BiomeTag(NMSHandler.instance.getBiomeAt(to.getBlock()));
        if (old_biome.identify().equals(new_biome.identify())) {
            return;
        }
        this.event = event;
        fire(event);
    }
}

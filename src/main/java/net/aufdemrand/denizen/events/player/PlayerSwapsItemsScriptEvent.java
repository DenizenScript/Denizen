package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

public class PlayerSwapsItemsScriptEvent extends BukkitScriptEvent {

    // <--[event]
    // @Events
    // player swaps items
    //
    // @Regex ^on player swaps items$
    //
    // @Warning This event is a prototype and may change drastically in the future.
    //
    // @Cancellable true
    //
    // @Triggers when a player swaps the items in their main and offhand.
    //
    // -->

    public PlayerSwapsItemsScriptEvent() {
        instance = this;
    }

    public static PlayerSwapsItemsScriptEvent instance;
    public dPlayer player;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player swaps items");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        return true;
    }

    @Override
    public String getName() {
        return "PlayerSwapsItems";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }
}

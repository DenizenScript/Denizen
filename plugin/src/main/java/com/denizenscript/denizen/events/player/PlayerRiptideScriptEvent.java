package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRiptideEvent;

public class PlayerRiptideScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player activates riptide
    //
    // @Regex ^on player activates riptide$
    // @Switch in <area>
    //
    // @Triggers when a player activates the riptide effect.
    //
    // @Context
    // <context.item> returns the ItemTag of the trident.
    //
    // -->

    public PlayerRiptideScriptEvent() {
        instance = this;
    }

    public static PlayerRiptideScriptEvent instance;
    public PlayerRiptideEvent event;
    private ItemTag item;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player activates riptide");
    }

    @Override
    public boolean matches(ScriptPath path) {
        return runInCheck(path, event.getPlayer().getLocation());
    }

    @Override
    public String getName() {
        return "PlayerActivatesRiptide";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new PlayerTag(event.getPlayer()), null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("item")) {
            return item;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerRiptide(PlayerRiptideEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        this.item = new ItemTag(event.getItem());
        this.event = event;
        fire(event);
    }
}

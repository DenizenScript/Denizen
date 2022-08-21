package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.Arrays;

public class PlayerChangesSignScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player changes sign
    // player changes <material>
    //
    // @Regex ^on player changes [^\s]+$
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a player changes a sign.
    //
    // @Context
    // <context.location> returns the LocationTag of the sign.
    // <context.new> returns the new sign text as a ListTag.
    // <context.old> returns the old sign text as a ListTag.
    // <context.material> returns the MaterialTag of the sign.
    //
    // @Determine
    // ListTag to change the lines.
    //
    // @Player Always.
    //
    // -->

    public PlayerChangesSignScriptEvent() {
        instance = this;
    }

    public static PlayerChangesSignScriptEvent instance;
    public LocationTag location;
    public MaterialTag material;
    public SignChangeEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        if (!path.eventLower.startsWith("player changes")) {
            return false;
        }
        String sign = path.eventArgAt(2);
        if  (!sign.equals("sign") && !couldMatchBlock(sign, (m) -> CoreUtilities.toLowerCase(m.name()).endsWith("sign"))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        String mat = path.eventArgLowerAt(2);
        if (!mat.equals("sign") && (!material.tryAdvancedMatcher(mat))) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        if (determination.length() > 0) {
            ListTag new_text = ListTag.valueOf(determination, getTagContext(path));
            for (int i = 0; i < 4 && i < new_text.size(); i++) {
                event.setLine(i, new_text.get(i));
            }
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "location":
                return location;
            case "material":
                return material;
            case "new":
                return new ListTag(Arrays.asList(event.getLines()), true);
            case "old":
                if (event.getBlock().getState() instanceof Sign) {
                    return new ListTag(Arrays.asList(((Sign) event.getBlock().getState()).getLines()), true);
                }
                else {
                    return null;
                }
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerChangesSign(SignChangeEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        BlockState state = event.getBlock().getState();
        if (!(state instanceof Sign)) {
            return;
        }
        material = new MaterialTag(event.getBlock());
        location = new LocationTag(event.getBlock().getLocation());
        this.event = event;
        fire(event);
    }
}

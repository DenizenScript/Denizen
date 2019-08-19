package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.tags.core.EscapeTagBase;
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
    // @Switch in <area>
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
    // ListTag to change the lines (Uses escaping, see <@link language Property Escaping>)
    //
    // -->

    public PlayerChangesSignScriptEvent() {
        instance = this;
    }

    public static PlayerChangesSignScriptEvent instance;
    public LocationTag location;
    public ListTag new_sign;
    public ListTag old_sign;
    public MaterialTag material;
    public ListTag new_text;
    public SignChangeEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String sign = CoreUtilities.getXthArg(2, lower);
        return lower.startsWith("player changes")
                && (sign.equals("sign") || MaterialTag.matches(sign));
    }

    @Override
    public boolean matches(ScriptPath path) {

        String mat = path.eventArgLowerAt(2);
        if (!mat.equals("sign")
                && (!(LocationTag.getBlockStateFor(event.getBlock()) instanceof Sign)
                && (!mat.equals(material.identifyNoIdentifier()) && !mat.equals(material.identifyFullNoIdentifier())))) {
            return false;
        }

        if (!runInCheck(path, location)) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "PlayerChangesSign";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        if (determination.length() > 0 && !isDefaultDetermination(determinationObj)) {
            new_text = ListTag.valueOf(determination);
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(PlayerTag.mirrorBukkitPlayer(event.getPlayer()), null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("material")) {
            return material;
        }
        else if (name.equals("new")) {
            return new_sign;
        }
        else if (name.equals("old")) {
            return old_sign;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerChangesSign(SignChangeEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        BlockState state = LocationTag.getBlockStateFor(event.getBlock());
        if (!(state instanceof Sign)) {
            return;
        }
        Sign sign = (Sign) state;
        material = new MaterialTag(event.getBlock());
        location = new LocationTag(event.getBlock().getLocation());
        old_sign = new ListTag(Arrays.asList(sign.getLines()));
        new_sign = new ListTag(Arrays.asList(event.getLines()));
        new_text = null;
        this.event = event;
        fire(event);
        if (new_text != null) {
            for (int i = 0; i < 4 && i < new_text.size(); i++) {
                event.setLine(i, EscapeTagBase.unEscape(new_text.get(i)));
            }
        }
    }

}

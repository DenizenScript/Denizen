package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.tags.core.EscapeTags;
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
    // <context.location> returns the dLocation of the sign.
    // <context.new> returns the new sign text as a ListTag.
    // <context.old> returns the old sign text as a ListTag.
    // <context.material> returns the dMaterial of the sign.
    //
    // @Determine
    // ListTag to change the lines (Uses escaping, see <@link language Property Escaping>)
    //
    // -->

    public PlayerChangesSignScriptEvent() {
        instance = this;
    }

    public static PlayerChangesSignScriptEvent instance;
    public dLocation location;
    public ListTag new_sign;
    public ListTag old_sign;
    public dMaterial material;
    public ListTag new_text;
    public SignChangeEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String sign = CoreUtilities.getXthArg(2, lower);
        return lower.startsWith("player changes")
                && (sign.equals("sign") || dMaterial.matches(sign));
    }

    @Override
    public boolean matches(ScriptPath path) {

        String mat = path.eventArgLowerAt(2);
        if (!mat.equals("sign")
                && (!(dLocation.getBlockStateFor(event.getBlock()) instanceof Sign)
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
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (determination.length() > 0 && !isDefaultDetermination(determination)) {
            new_text = ListTag.valueOf(determination);
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(dPlayer.mirrorBukkitPlayer(event.getPlayer()), null);
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
        else if (name.equals("cuboids")) {
            Debug.echoError("context.cuboids tag is deprecated in " + getName() + " script event");
            ListTag cuboids = new ListTag();
            for (dCuboid cuboid : dCuboid.getNotableCuboidsContaining(location)) {
                cuboids.add(cuboid.identifySimple());
            }
            return cuboids;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerChangesSign(SignChangeEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        BlockState state = dLocation.getBlockStateFor(event.getBlock());
        if (!(state instanceof Sign)) {
            return;
        }
        Sign sign = (Sign) state;
        material = new dMaterial(event.getBlock());
        location = new dLocation(event.getBlock().getLocation());
        old_sign = new ListTag(Arrays.asList(sign.getLines()));
        new_sign = new ListTag(Arrays.asList(event.getLines()));
        new_text = null;
        this.event = event;
        fire(event);
        if (new_text != null) {
            for (int i = 0; i < 4 && i < new_text.size(); i++) {
                event.setLine(i, EscapeTags.unEscape(new_text.get(i)));
            }
        }
    }

}

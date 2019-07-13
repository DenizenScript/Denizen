package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dPlayer;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;

public class PlayerShearsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player shears entity
    // player shears <entity>
    // player shears <color> sheep
    //
    // @Regex ^on player shears [^\s]+( sheep)?$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a player shears an entity.
    //
    // @Context
    // <context.entity> returns the dEntity of the sheep.
    //
    // -->

    public PlayerShearsScriptEvent() {
        instance = this;
    }

    public static PlayerShearsScriptEvent instance;
    public dEntity entity;
    public PlayerShearEntityEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player shears");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String ent = path.eventArgLowerAt(3).equals("sheep") ? "sheep" : path.eventArgLowerAt(2);

        if (!ent.equals("sheep") && !tryEntity(entity, ent)) {
            return false;
        }

        String color = path.eventArgLowerAt(3).equals("sheep") ? path.eventArgLowerAt(2) : "";
        if (color.length() > 0 && !color.equals(CoreUtilities.toLowerCase(((Sheep) entity.getBukkitEntity()).getColor().name()))) {
            return false;
        }

        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "PlayerShears";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new dPlayer(event.getPlayer()), null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("entity")) {
            return entity.getDenizenObject();
        }
        else if (name.equals("state")) { // NOTE: Deprecated
            return entity;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerShears(PlayerShearEntityEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        entity = new dEntity(event.getEntity());
        this.event = event;
        fire(event);
    }
}

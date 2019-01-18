package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SheepDyeWoolEvent;

public class SheepDyedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // sheep dyed (<color>) (in <area>)
    // player dyes sheep (<color>) (in <area>)
    //
    // @Regex ^on (sheep dyed|player dyes sheep) [^\s]+( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Warning Determine color will not update the clientside, use - wait 1t and adjust <context.entity> color:YOUR_COLOR to force-update.
    //
    // @Triggers when a sheep is dyed by a player.
    //
    // @Context
    // <context.entity> returns the dEntity of the sheep.
    // <context.color> returns an Element of the color the sheep is being dyed.
    //
    // @Determine
    // Element that matches DyeColor to dye it a different color.
    //
    // @Player when a player dyes a sheep, and using the 'player dyes sheep' event.
    //
    // -->

    public SheepDyedScriptEvent() {
        instance = this;
    }

    public static SheepDyedScriptEvent instance;
    public dEntity entity;
    public DyeColor color;
    public SheepDyeWoolEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("sheep dyed") || lower.startsWith("player dyes sheep");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);

        String new_color = cmd.equals("dyes") ? CoreUtilities.getXthArg(3, lower) : CoreUtilities.getXthArg(2, lower);
        if (!new_color.isEmpty() && !new_color.equals(CoreUtilities.toLowerCase(color.toString()))) {
            return false;
        }

        if (!runInCheck(scriptContainer, s, lower, entity.getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "SheepDyed";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        SheepDyeWoolEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (!isDefaultDetermination(determination)) {
            try {
                color = DyeColor.valueOf(determination.toUpperCase());
                return true;
            }
            catch (IllegalArgumentException e) {
            }
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(null, null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("color")) {
            return new Element(color.toString());
        }
        else if (name.equals("entity")) {
            return entity;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onSheepDyed(SheepDyeWoolEvent event) {
        entity = new dEntity(event.getEntity());
        color = DyeColor.valueOf(event.getColor().toString());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
        event.setColor(color);
    }
}

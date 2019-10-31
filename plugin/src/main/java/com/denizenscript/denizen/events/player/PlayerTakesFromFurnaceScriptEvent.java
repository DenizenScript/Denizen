package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceExtractEvent;

public class PlayerTakesFromFurnaceScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player takes item from furnace
    // player takes <item> from furnace
    // player takes <material> from furnace
    //
    // @Regex ^on player takes [^\s]+ from furnace$
    //
    // @Switch in <area>
    //
    // @Triggers when a player takes an item from a furnace.
    // @Context
    // <context.location> returns the LocationTag of the furnace.
    // <context.item> returns the ItemTag taken out of the furnace.
    //
    // @Determine
    // Element(Number) to set the amount of experience the player will get.
    //
    // @Player Always.
    //
    // -->

    public PlayerTakesFromFurnaceScriptEvent() {
        instance = this;
    }

    public static PlayerTakesFromFurnaceScriptEvent instance;
    public LocationTag location;
    public ItemTag item;
    private int xp;
    public FurnaceExtractEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player takes")
                && (CoreUtilities.getXthArg(4, lower).equals("furnace"));
    }

    @Override
    public boolean matches(ScriptPath path) {
        String itemTest = path.eventArgLowerAt(2);

        return tryItem(item, itemTest) && runInCheck(path, location);
    }

    @Override
    public String getName() {
        return "PlayerTakesFromFurnace";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag && ((ElementTag) determinationObj).isInt()) {
            xp = ((ElementTag) determinationObj).asInt();
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
        else if (name.equals("item")) {
            return item;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerTakesFromFurnace(FurnaceExtractEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        item = new ItemTag(event.getItemType(), event.getItemAmount());
        location = new LocationTag(event.getBlock().getLocation());
        xp = event.getExpToDrop();
        this.event = event;
        fire(event);
        event.setExpToDrop(xp);
    }

}

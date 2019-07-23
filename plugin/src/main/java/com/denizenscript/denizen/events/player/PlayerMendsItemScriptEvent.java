package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemMendEvent;

public class PlayerMendsItemScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player mends item
    // player mends <item>
    //
    // @Regex ^on player mends [^\s]+$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when an XP orb is used to repair an item with the Mending enchantment in the player's inventory.
    //
    // @Context
    // <context.item> returns the item that is repaired.
    // <context.repair_amount> returns how much durability the item recovers.
    // <context.xp_orb> returns the XP orb that triggered the event.
    //
    // @Determine
    // Element(Number) to set the amount of durability the item recovers.
    //
    // -->

    public PlayerMendsItemScriptEvent() {
        instance = this;
    }

    public static PlayerMendsItemScriptEvent instance;
    public ItemTag item;
    public EntityTag experienceOrb;
    public ElementTag repairAmount;
    public PlayerItemMendEvent event;
    public LocationTag location;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player mends");
    }

    @Override
    public boolean matches(ScriptPath path) {

        String iItem = path.eventArgLowerAt(2);
        if (!tryItem(item, iItem)) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "PlayerMendsItem";
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        if (ArgumentHelper.matchesInteger(determination)) {
            repairAmount = new ElementTag(determination);
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
        if (name.equals("item")) {
            return item;
        }
        else if (name.equals("repair_amount")) {
            return repairAmount;
        }
        else if (name.equals("xp_orb")) {
            return experienceOrb;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerItemMend(PlayerItemMendEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        item = new ItemTag(event.getItem());
        experienceOrb = new EntityTag(event.getExperienceOrb());
        location = new LocationTag(event.getPlayer().getLocation());
        repairAmount = new ElementTag(event.getRepairAmount());
        this.event = event;
        fire(event);
        event.setRepairAmount(repairAmount.asInt());
    }

}

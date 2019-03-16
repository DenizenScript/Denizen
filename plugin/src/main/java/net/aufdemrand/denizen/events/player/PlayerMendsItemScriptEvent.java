package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
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
    public dItem item;
    public dEntity experienceOrb;
    public Element repairAmount;
    public PlayerItemMendEvent event;
    public dLocation location;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player mends");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String lower = path.eventLower;

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
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (aH.matchesInteger(determination)) {
            repairAmount = new Element(determination);
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(dPlayer.mirrorBukkitPlayer(event.getPlayer()), null);
    }

    @Override
    public dObject getContext(String name) {
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
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        item = new dItem(event.getItem());
        experienceOrb = new dEntity(event.getExperienceOrb());
        location = new dLocation(event.getPlayer().getLocation());
        repairAmount = new Element(event.getRepairAmount());
        this.event = event;
        fire(event);
        event.setRepairAmount(repairAmount.asInt());
    }

}

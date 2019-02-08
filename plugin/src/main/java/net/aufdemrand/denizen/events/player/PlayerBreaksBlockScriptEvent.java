package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class PlayerBreaksBlockScriptEvent extends BukkitScriptEvent implements Listener {

    // TODO: de-collide with breaks item
    // <--[event]
    // @Events
    // player breaks block (in <area>)
    // player breaks <material> (in <area>)
    // player breaks block (in <area>)
    // player breaks <material> (in <area>)
    //
    // @Regex ^on player breaks [^\s]+( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Switch with <item>
    //
    // @Cancellable true
    //
    // @Triggers when a player breaks a block.
    //
    // @Context
    // <context.location> returns the dLocation the block was broken at.
    // <context.material> returns the dMaterial of the block that was broken.
    // <context.cuboids> DEPRECATED.
    // <context.xp> returns how much XP will be dropped.
    //
    // @Determine
    // "NOTHING" to make the block drop no items.
    // dList(dItem) to make the block drop a specified list of items.
    // Element(Number) to set the amount of xp to drop.
    //
    // -->

    public PlayerBreaksBlockScriptEvent() {
        instance = this;
    }

    public static PlayerBreaksBlockScriptEvent instance;
    public dLocation location;
    public dMaterial material;
    public dList cuboids;
    public Element xp;
    public BlockBreakEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player breaks");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String s = path.event;
        String lower = path.eventLower;
        String mat = CoreUtilities.getXthArg(2, lower);
        if (!tryMaterial(material, mat)) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        if (!runWithCheck(path, new dItem(event.getPlayer().getItemInHand()))) {
            return false;
        }
        // Deprecated in favor of with: format
        if (CoreUtilities.xthArgEquals(3, lower, "with")
                && !tryItem(new dItem(event.getPlayer().getItemInHand()), CoreUtilities.getXthArg(4, lower))) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "PlayerBreaksBlock";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        BlockBreakEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        String lower = CoreUtilities.toLowerCase(determination);
        Block block = event.getBlock();
        if (lower.equals("nothing")) {
            cancelled = true;
            block.setType(Material.AIR);
        }
        else if (aH.matchesInteger(determination)) {
            xp = aH.Argument.valueOf(lower).asElement();
        }
        else if (aH.Argument.valueOf(lower).matchesArgumentList(dItem.class)) {
            cancelled = true;
            block.setType(Material.AIR);

            for (dItem newItem : dList.valueOf(determination).filter(dItem.class)) {
                block.getWorld().dropItemNaturally(block.getLocation(), newItem.getItemStack()); // Drop each item
            }
        }
        else {
            return super.applyDetermination(container, determination);
        }
        return true;
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(dPlayer.mirrorBukkitPlayer(event.getPlayer()), null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("material")) {
            return material;
        }
        else if (name.equals("cuboids")) { // NOTE: Deprecated in favor of context.location.cuboids
            if (cuboids == null) {
                cuboids = new dList();
                for (dCuboid cuboid : dCuboid.getNotableCuboidsContaining(location)) {
                    cuboids.add(cuboid.identifySimple());
                }
            }
            return cuboids;
        }
        else if (name.equals("xp")) {
            return xp;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerBreaksBlock(BlockBreakEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());
        location = new dLocation(event.getBlock().getLocation());
        cuboids = null;
        cancelled = event.isCancelled();
        xp = new Element(event.getExpToDrop());
        this.event = event;
        fire();
        event.setCancelled(cancelled);
        event.setExpToDrop(xp.asInt());
    }

}

package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
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

import java.util.HashMap;

public class PlayerBreaksBlockScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player breaks block (in <area>)
    // player breaks <material> (in <area>)
    // player breaks block with <item> (in <area>)
    // player breaks <material> with <item> (in <area>)
    // player breaks block with <material> (in <area>)
    // player breaks <material> with <material> (in <area>)
    //
    // @Cancellable true
    //
    // @Triggers when a player breaks a block.
    //
    // @Context
    // <context.location> returns the dLocation the block was broken at.
    // <context.material> returns the dMaterial of the block that was broken.
    // <context.cuboids> returns a dList of notable cuboids surrounding the block broken. DEPRECATED.
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
        return s.toLowerCase().startsWith("player breaks");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);

        String mat = CoreUtilities.getXthArg(2, lower);
        if (!mat.equals("block") && !mat.equals(material.identifyNoIdentifier())) {
            return false;
        }

        if (!runInCheck(scriptContainer, s, lower, location)) {
            return false;
        }

        if (CoreUtilities.xthArgEquals(3, lower, "with")) {
            String tool = CoreUtilities.getXthArg(4, lower);
            dItem item = new dItem(event.getPlayer().getItemInHand());
            if (!tool.equals(item.identifyNoIdentifier())) {
                return false;
            }
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
        else if (aH.Argument.valueOf(lower).matchesPrimitive(aH.PrimitiveType.Integer)) {
            xp = aH.Argument.valueOf(lower).asElement();
        }
        else if (aH.Argument.valueOf(lower).matchesArgumentList(dItem.class)) {
            cancelled = true;
            block.setType(Material.AIR);

            for (dItem newItem : dList.valueOf(lower).filter(dItem.class)) {
                block.getWorld().dropItemNaturally(block.getLocation(),
                        newItem.getItemStack()); // Drop each item
            }
        }
        else {
            return super.applyDetermination(container, determination);
        }
        return true;
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new dPlayer(event.getPlayer()), null);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("location", location);
        context.put("material", material);
        context.put("cuboids", cuboids);
        context.put("xp", xp);
        return context;
    }

    @EventHandler
    public void onPlayerBreaksBlock(BlockBreakEvent event) {
        material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());
        location = new dLocation(event.getBlock().getLocation());
        cuboids = new dList();
        for (dCuboid cuboid: dCuboid.getNotableCuboidsContaining(location)) {
            cuboids.add(cuboid.identifySimple());
        }
        cancelled = event.isCancelled();
        xp = new Element(event.getExpToDrop());
        this.event = event;
        fire();
        event.setCancelled(cancelled);
        event.setExpToDrop(xp.asInt());
    }

}

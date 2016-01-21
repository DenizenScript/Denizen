package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;

public class ItemEnchantedScriptEvent extends BukkitScriptEvent implements Listener {

    // TODO: Find more appropriate package than 'entity' for this
    // <--[event]
    // @Events
    // item enchanted (in <area>)
    // <item> enchanted (in <area>)
    //
    // @Regex ^on [^\s]+ enchanted( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when an item is enchanted.
    //
    // @Context
    // <context.entity> returns the dEntity of the enchanter (if applicable)
    // <context.location> returns the dLocation of the enchanting table.
    // <context.inventory> returns the dInventory of the enchanting table.
    // <context.item> returns the dItem to be enchanted.
    // <context.button> returns which button was pressed to initiate the enchanting.
    // <context.cost> returns the experience level cost of the enchantment.
    //
    // @Determine
    // Element(Number) to set the experience level cost of the enchantment.
    // -->

    public ItemEnchantedScriptEvent() {
        instance = this;
    }

    public static ItemEnchantedScriptEvent instance;
    public dEntity entity;
    public dLocation location;
    public dInventory inventory;
    public dItem item;
    public Element button;
    public int cost;
    public EnchantItemEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("enchanted");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String itemTest = CoreUtilities.getXthArg(0, lower);
        if (!itemTest.equals("item") && !tryItem(item, itemTest)) {
            return false;
        }

        if (!runInCheck(scriptContainer, s, lower, location)) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "ItemEnchanted";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        EnchantItemEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (aH.matchesInteger(determination)) {
            cost = aH.getIntegerFrom(determination);
        }

        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? entity.getDenizenPlayer() : null,
                entity.isCitizensNPC() ? entity.getDenizenNPC() : null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("location")) {
            return location;
        }
        else if (name.equals("inventory")) {
            return inventory;
        }
        else if (name.equals("item")) {
            return item;
        }
        else if (name.equals("button")) {
            return button;
        }
        else if (name.equals("cost")) {
            return new Element(cost);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onItemEnchanted(EnchantItemEvent event) {
        entity = new dEntity(event.getEnchanter());
        location = new dLocation(event.getEnchantBlock().getLocation());
        inventory = dInventory.mirrorBukkitInventory(event.getInventory());
        item = new dItem(event.getItem());
        button = new Element(event.whichButton());
        cost = event.getExpLevelCost();
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
        event.setExpLevelCost(cost);
    }
}

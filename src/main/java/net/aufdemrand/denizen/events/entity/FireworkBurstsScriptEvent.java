package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.inventory.ItemStack;

public class FireworkBurstsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // firework bursts (in <area>)
    //
    // @Regex ^on firework bursts( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable true
    //
    // @Triggers when a firework bursts (explodes).
    //
    // @Context
    // <context.entity> returns the firework that exploded.
    // <context.item>  returns the firework item.
    // <context.location> returns the dLocation the firework exploded at.
    //
    // -->

    public FireworkBurstsScriptEvent() {
        instance = this;
    }

    public static FireworkBurstsScriptEvent instance;
    public FireworkExplodeEvent event;
    public dEntity entity;
    public dLocation location;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("firework bursts");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        if (!runInCheck(scriptContainer, s, lower, entity.getLocation())) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "FireworkBursts";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        FireworkExplodeEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("location")) {
            return location;
        }
        else if (name.equals("item")) {
            ItemStack itemStack = new ItemStack(Material.FIREWORK);
            itemStack.setItemMeta(event.getEntity().getFireworkMeta());
            return new dItem(itemStack);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onFireworkBursts(FireworkExplodeEvent event) {
        entity = new dEntity(event.getEntity());
        location = new dLocation(entity.getLocation());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

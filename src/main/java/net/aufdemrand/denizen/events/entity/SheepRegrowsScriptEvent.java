package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SheepRegrowWoolEvent;

import java.util.HashMap;

public class SheepRegrowsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // sheep regrows wool (in <area>)
    //
    // @Cancellable true
    //
    // @Triggers when a sheep regrows wool.
    //
    // @Context
    // <context.entity> returns the dEntity of the sheep.
    //
    // -->

    public SheepRegrowsScriptEvent() {
        instance = this;
    }

    public static SheepRegrowsScriptEvent instance;
    public dEntity entity;
    private dLocation location;
    public SheepRegrowWoolEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("sheep regrows wool");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);

        return runInCheck(scriptContainer, s, lower, location);
    }

    @Override
    public String getName() {
        return "SheepRegrows";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        SheepRegrowWoolEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("entity", entity);
        return context;
    }

    @EventHandler
    public void onSheepRegrows(SheepRegrowWoolEvent event) {
        entity = new dEntity(event.getEntity());
        location = new dLocation(entity.getLocation());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

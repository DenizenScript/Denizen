package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.HashMap;
import java.util.List;

public class PlayerFishesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player fishes (<entity>) (while <state>) (in <area>)
    //
    // @Cancellable true
    //
    // @Triggers when a player uses a fishing rod.
    //
    // @Context
    // <context.hook> returns a dEntity of the hook.
    // <context.state> returns an Element of the fishing state.
    // <context.entity> returns a dEntity of the entity that got caught.
    // <context.item> returns a dItem of the item gotten, if any.
    //
    // -->

    public PlayerFishesScriptEvent() {
        instance = this;
    }

    public static PlayerFishesScriptEvent instance;
    public dEntity hook;
    public Element state;
    public dEntity entity;
    public dItem item;
    public PlayerFishEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("player fishes");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String fish = CoreUtilities.getXthArg(2, lower);
        if (entity != null && fish.length() > 0) {
            if(!entity.matchesEntity(fish)) {
                return false;
            }
        }
        List<String> data = CoreUtilities.split(lower, ' ');
        for (int index = 0; index < data.size(); index++) {
            if (data.get(index).equals("while")) {
                if (!data.get(index+1).equalsIgnoreCase(state.asString())){
                    return false;
                }
            }
        }
        if (!runInCheck(scriptContainer, s, lower, hook.getLocation())) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "PlayerFishes";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        PlayerFishEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(dEntity.isPlayer(event.getPlayer()) ? dEntity.getPlayerFrom(event.getPlayer()) :
                dEntity.isPlayer(event.getCaught()) ? dEntity.getPlayerFrom(event.getCaught()) : null,
                dEntity.isCitizensNPC(event.getPlayer()) ? dEntity.getNPCFrom(event.getPlayer()) :
                        dEntity.isCitizensNPC(event.getCaught()) ? dEntity.getNPCFrom(event.getCaught()) : null);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("hook", hook);
        context.put("state", state);
        if (entity != null) {
            context.put("entity", entity.getDenizenObject());
        }
        if (item != null) {
            context.put("item", item);
        }
        return context;
    }

    @EventHandler
    public void onPlayerFishes(PlayerFishEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        hook = new dEntity(event.getHook());
        state = new Element(event.getState().name());
        item = null;
        entity = null;
        if (event.getCaught() != null) {
            entity = new dEntity(event.getCaught());
            if (event.getCaught() instanceof Item) {
                item = new dItem(((Item) event.getCaught()).getItemStack());
            }
        }
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

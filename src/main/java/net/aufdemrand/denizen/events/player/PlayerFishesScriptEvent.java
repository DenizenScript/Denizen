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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.List;

public class PlayerFishesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player fishes (<entity>) (while <state>) (in <area>)
    //
    // @Regex ^on player fishes( [^\s]+)?( while [^\s]+)?( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
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
            if (!entity.matchesEntity(fish)) {
                return false;
            }
        }
        List<String> data = CoreUtilities.split(lower, ' ');
        for (int index = 0; index < data.size(); index++) {
            if (data.get(index).equals("while")) {
                if (!data.get(index + 1).equalsIgnoreCase(state.asString())) {
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
    public dObject getContext(String name) {
        if (name.equals("hook")) {
            return hook;
        }
        else if (name.equals("entity") && entity != null) {
            return entity.getDenizenObject();
        }
        else if (name.equals("item") && item != null) {
            return item;
        }
        else if (name.equals("state")) {
            return state;
        }
        return super.getContext(name);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerFishes(PlayerFishEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        Entity hookEntity = event.getHook();
        dEntity.rememberEntity(hookEntity);
        hook = new dEntity(hookEntity);
        state = new Element(event.getState().name());
        item = null;
        entity = null;
        Entity caughtEntity = event.getCaught();
        if (caughtEntity != null) {
            dEntity.rememberEntity(caughtEntity);
            entity = new dEntity(caughtEntity);
            if (caughtEntity instanceof Item) {
                item = new dItem(((Item) caughtEntity).getItemStack());
            }
        }
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        dEntity.forgetEntity(hookEntity);
        dEntity.forgetEntity(caughtEntity);
        event.setCancelled(cancelled);
    }
}

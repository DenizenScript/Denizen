package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerFishesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player fishes (<entity>) (while <'state'>)
    // player fishes (<item>) (while <'state'>)
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Switch with:<item> to only process the event if the fishing rod is a specified item.
    //
    // @Triggers when a player uses a fishing rod.
    //
    // @Context
    // <context.hook> returns an EntityTag of the hook.
    // <context.state> returns an ElementTag of the fishing state. Valid states: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/player/PlayerFishEvent.State.html>
    // <context.entity> returns an EntityTag of the entity that got caught.
    // <context.item> returns an ItemTag of the item gotten, if any.
    // <context.xp> returns the amount of experience that will drop.
    //
    // @Determine
    // "CAUGHT:<ItemTag>" to change the item that was caught (only if an item was already being caught).
    // "XP:<ElementTag(Number)>" to change how much experience will drop.
    //
    // @Player If the fisher or the caught entity is a player (in most cases, the fisher can be assumed to be a real player).
    // @NPC If the fisher or the caught entity is an NPC.
    //
    // -->

    public PlayerFishesScriptEvent() {
        registerCouldMatcher("player fishes (<entity>) (while <'state'>)");
        registerCouldMatcher("player fishes (<item>) (while <'state'>)");
        registerSwitches("with");
    }

    public EntityTag hook;
    public ElementTag state;
    public EntityTag entity;
    public ItemTag item;
    public PlayerFishEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        String fish = path.eventArgLowerAt(2);
        if (!fish.isEmpty() && !fish.equals("in") && !fish.equals("while")) {
            if (entity == null) {
                return false;
            }
            if (!entity.tryAdvancedMatcher(fish)) {
                if (item == null) {
                    return false;
                }
                if (!item.tryAdvancedMatcher(fish)) {
                    return false;
                }
            }
        }
        String[] data = path.eventArgsLower;
        for (int index = 2; index < data.length; index++) {
            if (data[index].equals("while") && !data[index + 1].equalsIgnoreCase(state.asString())) {
                return false;
            }
        }
        if (!runInCheck(path, hook.getLocation())) {
            return false;
        }
        if (path.switches.containsKey("with")) {
            if (!EntityTag.isPlayer(event.getPlayer())) {
                return false;
            }
            ItemStack held = event.getPlayer().getEquipment().getItemInMainHand();
            if (held.getType() != Material.FISHING_ROD) {
                held = event.getPlayer().getEquipment().getItemInOffHand();
            }
            if (!runWithCheck(path, new ItemTag(held))) {
                return false;
            }
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            String determination = determinationObj.toString();
            String determinationLower = CoreUtilities.toLowerCase(determination);
            if (determinationLower.startsWith("caught:")) {
                item = ItemTag.valueOf(determination.substring("caught:".length()), getTagContext(path));
                if (entity != null && entity.getBukkitEntity() instanceof Item item) {
                    item.setItemStack(item.getItemStack());
                    return true;
                }
            }
            else if (determinationLower.startsWith("xp:")) {
                int newXP = new ElementTag(determination.substring("xp:".length())).asInt();
                event.setExpToDrop(newXP);
                return true;
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(EntityTag.isPlayer(event.getPlayer()) ? EntityTag.getPlayerFrom(event.getPlayer()) :
                EntityTag.isPlayer(event.getCaught()) ? EntityTag.getPlayerFrom(event.getCaught()) : null,
                EntityTag.isCitizensNPC(event.getPlayer()) ? EntityTag.getNPCFrom(event.getPlayer()) :
                        EntityTag.isCitizensNPC(event.getCaught()) ? EntityTag.getNPCFrom(event.getCaught()) : null);
    }

    @Override
    public ObjectTag getContext(String name) {
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
        else if (name.equals("xp")) {
            return new ElementTag(event.getExpToDrop());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerFishes(PlayerFishEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        Entity hookEntity = event.getHook();
        EntityTag.rememberEntity(hookEntity);
        hook = new EntityTag(hookEntity);
        state = new ElementTag(event.getState().toString());
        item = null;
        entity = null;
        Entity caughtEntity = event.getCaught();
        if (caughtEntity != null) {
            EntityTag.rememberEntity(caughtEntity);
            entity = new EntityTag(caughtEntity);
            if (caughtEntity instanceof Item) {
                item = new ItemTag(((Item) caughtEntity).getItemStack());
            }
        }
        this.event = event;
        fire(event);
        EntityTag.forgetEntity(hookEntity);
        EntityTag.forgetEntity(caughtEntity);
    }
}

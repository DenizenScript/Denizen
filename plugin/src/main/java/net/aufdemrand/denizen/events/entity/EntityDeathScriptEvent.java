package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class EntityDeathScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity death (in <area>)
    // entity dies (in <area>)
    // <entity> dies (in <area>)
    // <entity> death (in <area>)
    //
    // @Cancellable true
    //
    // @Regex ^on [^\s]+ (death|dies)( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Triggers when an entity dies.
    //
    // @Context
    // <context.entity> returns the dEntity that died.
    // <context.damager> returns the dEntity damaging the other entity, if any.
    // <context.message> returns an Element of a player's death message.
    // <context.inventory> returns the dInventory of the entity if it was a player.
    // <context.cause> returns an Element of the cause of the death. See <@link language damage cause> for a list of possible damage causes.
    // <context.drops> returns a dList of all pending item drops.
    // <context.xp> returns an Element of the amount of experience to be dropped.
    //
    // @Determine
    // Element to change the death message.
    // "NO_DROPS" to specify that any drops should be removed.
    // "NO_DROPS_OR_XP" to specify that any drops or XP orbs should be removed.
    // "NO_XP" to specify that any XP orbs should be removed.
    // dList(dItem) to specify new items to be dropped.
    // Element(Number) to specify the new amount of XP to be dropped.
    // "KEEP_INV" to specify (if a player death) that the inventory should be kept.
    // "KEEP_LEVEL" to specify (if a player death) that the XP level should be kept.
    // Note that the event can be cancelled to hide a player death message.
    //
    // @Player when the entity that died is a player.
    //
    // @NPC when the entity that died is an NPC.
    //
    // -->
    public EntityDeathScriptEvent() {
        instance = this;
    }

    public static EntityDeathScriptEvent instance;

    public dEntity entity;
    public dObject damager;
    public Element message;
    public dInventory inventory;
    public Element cause;
    public dList drops;
    public Integer xp;
    public boolean changed_drops;
    public boolean keep_inv;
    public boolean keep_level;
    public EntityDeathEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String cmd = CoreUtilities.getXthArg(1, CoreUtilities.toLowerCase(s));
        return cmd.equals("dies") || cmd.equals("death");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String target = CoreUtilities.getXthArg(0, lower);

        if (!tryEntity(entity, target)) {
            return false;
        }

        if (!runInCheck(scriptContainer, s, lower, entity.getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "EntityDies";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        EntityDeathEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        // finish this
        String lower = CoreUtilities.toLowerCase(determination);

        // Deprecated
        if (lower.startsWith("drops ")) {
            lower = lower.substring(6);
            determination = determination.substring(6);
        }

        //Handle no_drops and no_drops_or_xp and just no_xp
        if (lower.startsWith("no_drops")) {
            drops.clear();
            changed_drops = true;
            if (lower.endsWith("_or_xp")) {
                xp = 0;
            }
        }
        else if (lower.equals("no_xp")) {
            xp = 0;
        }
        else if (lower.equals("keep_inv")) {
            keep_inv = true;
        }
        else if (lower.equals("keep_level")) {
            keep_level = true;
        }
        // Change xp value only
        else if (aH.matchesInteger(determination)) {
            xp = aH.Argument.valueOf(lower).asElement().asInt();
        }

        // Change dropped items if dList detected
        else if (aH.Argument.valueOf(lower).matchesArgumentList(dItem.class)) {
            drops.clear();
            changed_drops = true;
            dList drops_list = dList.valueOf(determination);
            drops_list.filter(dItem.class);
            for (String drop : drops_list) {
                dItem item = dItem.valueOf(drop);
                if (item != null) {
                    drops.add(item.identify()); // TODO: Why not just store the dItem in an arraylist?
                }
            }
        }

        else if (determination.equalsIgnoreCase("cancelled")) {
            cancelled = true;
        }

        // String containing new Death Message
        else if (event instanceof PlayerDeathEvent) {
            message = new Element(determination);
        }
        else {
            return super.applyDetermination(container, determination);
        }
        return true;
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? dEntity.getPlayerFrom(event.getEntity()) : null,
                entity.isCitizensNPC() ? dEntity.getNPCFrom(event.getEntity()) : null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("entity")) {
            return entity.getDenizenObject();
        }
        else if (name.equals("damager") && damager != null) {
            return damager;
        }
        else if (name.equals("message") && message != null) {
            return message;
        }
        else if (name.equals("inventory") && inventory != null) {
            return inventory;
        }
        else if (name.equals("cause") && cause != null) {
            return cause;
        }
        else if (name.equals("drops") && drops != null) {
            return drops;
        }
        else if (name.equals("xp") && xp != null) {
            return new Element(xp);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {

        LivingEntity livingEntity = event.getEntity();
        dEntity.rememberEntity(livingEntity);
        entity = new dEntity(livingEntity);

        dPlayer player = null;

        if (entity.isPlayer()) {
            player = entity.getDenizenPlayer();
        }

        damager = null;
        EntityDamageEvent lastDamage = entity.getBukkitEntity().getLastDamageCause();
        if (lastDamage != null) {
            if (lastDamage instanceof EntityDamageByEntityEvent) {
                damager = new dEntity(((EntityDamageByEntityEvent) lastDamage).getDamager()).getDenizenObject();
            }

        }

        message = null;
        inventory = null;
        PlayerDeathEvent subEvent = null;
        if (event instanceof PlayerDeathEvent) {
            subEvent = (PlayerDeathEvent) event;
            message = new Element(subEvent.getDeathMessage());

            // Null check to prevent NPCs from causing an NPE
            if (player != null) {
                inventory = player.getInventory();
            }
            keep_inv = subEvent.getKeepInventory();
            keep_level = subEvent.getKeepLevel();
        }
        cause = null;
        if (event.getEntity().getLastDamageCause() != null) {
            cause = new Element(event.getEntity().getLastDamageCause().getCause().toString());
        }

        drops = new dList();
        for (ItemStack stack : event.getDrops()) {
            if (stack == null) {
                drops.add("i@air");
            }
            else {
                drops.add(new dItem(stack).identify());
            }
        }
        changed_drops = false;
        xp = event.getDroppedExp();
        this.event = event;
        fire();

        event.setDroppedExp(xp);
        if (changed_drops) {
            event.getDrops().clear();
            for (String drop : drops) {
                dItem item = dItem.valueOf(drop);
                if (item != null) {
                    event.getDrops().add(item.getItemStack());
                }
            }
        }
        if (event instanceof PlayerDeathEvent) {
            ((PlayerDeathEvent) event).setKeepInventory(keep_inv);
            ((PlayerDeathEvent) event).setKeepLevel(keep_level);
        }
        if (message != null && subEvent != null) {
            subEvent.setDeathMessage(message.asString());
        }
        if (cancelled && subEvent != null) {
            subEvent.setDeathMessage(null);
        }

        dEntity.forgetEntity(livingEntity);
    }
}

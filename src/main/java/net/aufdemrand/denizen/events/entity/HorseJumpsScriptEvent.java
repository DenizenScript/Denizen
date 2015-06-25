package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.HorseJumpEvent;

import java.util.HashMap;

public class HorseJumpsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // horse jumps
    // (<color>) (<type>) jumps
    //
    // @Cancellable true
    //
    // @Triggers when a horse jumps.
    //
    // @Context
    // <context.entity> returns the dEntity of the horse.
    // <context.color> returns an Element of the horse's color.
    // <context.variant> returns an Element of the horse's variant.
    // <context.power> returns an Element(Decimal) of the jump's power.
    //
    // @Determine
    // Element(Decimal) to set the power of the jump.
    //
    // -->

    public HorseJumpsScriptEvent() {
        instance = this;
    }

    public static HorseJumpsScriptEvent instance;
    public dEntity entity;
    public Element color;
    public Element variant;
    public Float power;
    public HorseJumpEvent event;


    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.equals("horse jumps") || lower.endsWith("jumps");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String arg1 = CoreUtilities.getXthArg(0, lower);
        String arg2 = CoreUtilities.getXthArg(1, lower);
        String tamed = arg2.equals("jumps")? arg1: arg2;

        if (!entity.matchesEntity(tamed)) {
            return false;
        }

        if (CoreUtilities.getXthArg(2, lower).length() > 0) {
            if (!arg1.equals(color.toString().toLowerCase()) || !arg2.equals(variant.toString().toLowerCase())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String getName() {
        return "HorseJumps";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        HorseJumpEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (aH.Argument.valueOf(determination)
                .matchesPrimitive(aH.PrimitiveType.Float)) {
            power = aH.getFloatFrom(determination);
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("entity", entity);
        context.put("color", color);
        context.put("variant", variant);
        context.put("power", new Element(power));
        return context;
    }

    @EventHandler
    public void onHorseJumps(HorseJumpEvent event) {
        entity = new dEntity(event.getEntity());
        color = new Element(event.getEntity().getColor().name());
        variant = new Element(event.getEntity().getVariant().name());
        power = event.getPower();
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
        event.setPower(power);
    }

}

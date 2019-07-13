package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.aH;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Horse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.HorseJumpEvent;

public class HorseJumpsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // horse jumps
    // (<color>) (<type>) jumps
    //
    // @Regex ^on [^\s]+( [^\s]+)? jumps$
    // @Switch in <area>
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
    public boolean matches(ScriptPath path) {
        String arg1 = path.eventArgLowerAt(0);
        String arg2 = path.eventArgLowerAt(1);
        String tamed = arg2.equals("jumps") ? arg1 : arg2;

        if (!tryEntity(entity, tamed) || !tamed.equals(CoreUtilities.toLowerCase(variant.toString()))) {
            return false;
        }

        if (path.eventArgLowerAt(2).equals("jumps") && !arg1.equals(CoreUtilities.toLowerCase(color.toString()))) {
            return false;
        }

        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "HorseJumps";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (aH.matchesDouble(determination)) {
            power = aH.getFloatFrom(determination);
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("color")) {
            return color;
        }
        else if (name.equals("variant")) {
            return variant;
        }
        else if (name.equals("power")) {
            return new Element(power);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onHorseJumps(HorseJumpEvent event) {
        if (event.getEntity() instanceof Horse) {
            entity = new dEntity(event.getEntity());
            color = new Element(((Horse) event.getEntity()).getColor().name());
            variant = new Element(event.getEntity().getVariant().name());
            power = event.getPower();
            this.event = event;
            fire(event);
            event.setPower(power);
        }
    }

}

package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerCareerChangeEvent;

public class VillagerChangesProfessionScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // villager changes profession
    //
    // @Regex ^on villager changes profession$
    //
    // @Group Entity
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Cancellable true
    //
    // @Triggers when a villager changes profession.
    //
    // @Context
    // <context.entity> returns the EntityTag of the villager.
    // <context.profession> returns the name of the new profession.
    // <context.reason> returns the reason for the change. <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/VillagerCareerChangeEvent.ChangeReason.html>
    //
    // @Determine
    // ElementTag to change the profession. Valid professions: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Villager.Profession.html>
    // -->

    public VillagerChangesProfessionScriptEvent() {
        instance = this;
    }

    public static VillagerChangesProfessionScriptEvent instance;
    public EntityTag entity;
    public VillagerCareerChangeEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("villager changes profession");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "VillagerChangesProfession";
    }


    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        try {
            Villager.Profession newProfession = Villager.Profession.valueOf(determination.toUpperCase());
            event.setProfession(newProfession);
            return true;
        }
        catch (IllegalArgumentException e) {
            Debug.echoError("Invalid profession specified: " + determination);
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("reason")) {
            return new ElementTag(event.getReason().toString());
        }
        else if (name.equals("profession")) {
            return new ElementTag(event.getProfession().toString());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onVillagerChangesProfession(VillagerCareerChangeEvent event) {
        this.event = event;
        this.entity = new EntityTag(event.getEntity());
        fire(event);
    }
}

package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
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
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a villager changes profession.
    //
    // @Context
    // <context.entity> returns the EntityTag of the villager.
    // <context.profession> returns the name of the new profession. <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Villager.Profession.html>
    // <context.reason> returns the reason for the change. <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/VillagerCareerChangeEvent.ChangeReason.html>
    //
    // @Determine
    // ElementTag to change the profession.
    // -->

    public VillagerChangesProfessionScriptEvent() {
    }

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
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (exactMatchesEnum(determinationObj.toString(), Villager.Profession.values())) {
            Villager.Profession newProfession = Villager.Profession.valueOf(determinationObj.toString().toUpperCase());
            event.setProfession(newProfession);
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "entity":
                return entity;
            case "reason":
                return new ElementTag(event.getReason().toString());
            case "profession":
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

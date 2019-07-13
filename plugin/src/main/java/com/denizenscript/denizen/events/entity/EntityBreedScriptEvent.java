package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dItem;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;

public class EntityBreedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity breeds
    // <entity> breeds
    //
    // @Regex ^on [^\s]+ breeds$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when two entities breed.
    //
    // @Context
    // <context.breeder> returns the dEntity responsible for breeding, if it exists.
    // <context.child> returns the child dEntity.
    // <context.mother> returns the parent dEntity creating the child. The child will spawn at the mother's location.
    // <context.father> returns the other parent dEntity.
    // <context.item> returns the dItem used to initiate breeding, if it exists.
    // <context.experience> returns the amount of experience granted by breeding.
    //
    // @Determine
    // Element(Number) to set the amount of experience granted by breeding.
    //
    // -->

    public EntityBreedScriptEvent() {
        instance = this;
    }

    public static EntityBreedScriptEvent instance;
    private dEntity entity;
    private dEntity breeder;
    private dEntity father;
    private dEntity mother;
    private dItem item;
    private int experience;
    public EntityBreedEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.getXthArg(1, CoreUtilities.toLowerCase(s)).equals("breeds");
    }

    @Override
    public boolean matches(ScriptPath path) {

        if (!tryEntity(entity, path.eventArgLowerAt(0))) {
            return false;
        }

        return runInCheck(path, entity.getLocation());
    }

    @Override
    public String getName() {
        return "EntityBreeds";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (ArgumentHelper.matchesInteger(determination)) {
            experience = ArgumentHelper.getIntegerFrom(determination);
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(null, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("child")) {
            return entity.getDenizenObject();
        }
        else if (name.equals("breeder")) {
            return breeder.getDenizenObject();
        }
        else if (name.equals("father")) {
            return father.getDenizenObject();
        }
        else if (name.equals("mother")) {
            return mother.getDenizenObject();
        }
        else if (name.equals("item")) {
            return item;
        }
        else if (name.equals("experience")) {
            return new ElementTag(experience);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityBreeds(EntityBreedEvent event) {
        Entity entity = event.getEntity();
        this.entity = new dEntity(entity);
        breeder = new dEntity(event.getBreeder());
        father = new dEntity(event.getFather());
        mother = new dEntity(event.getMother());
        item = new dItem(event.getBredWith());
        experience = event.getExperience();
        boolean wasCancelled = event.isCancelled();
        this.event = event;
        dEntity.rememberEntity(entity);
        fire(event);
        dEntity.forgetEntity(entity);
        event.setExperience(experience);

        // Prevent entities from continuing to breed with each other
        if (cancelled && !wasCancelled) {
            NMSHandler.getInstance().getEntityHelper().setBreeding((Animals) father.getLivingEntity(), false);
            NMSHandler.getInstance().getEntityHelper().setBreeding((Animals) mother.getLivingEntity(), false);
        }
    }
}

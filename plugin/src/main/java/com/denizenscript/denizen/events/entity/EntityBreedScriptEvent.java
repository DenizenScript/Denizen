package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;

public class EntityBreedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // <entity> breeds
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when two entities breed.
    //
    // @Context
    // <context.breeder> returns the EntityTag responsible for breeding, if it exists.
    // <context.child> returns the child EntityTag.
    // <context.mother> returns the parent EntityTag creating the child. The child will spawn at the mother's location.
    // <context.father> returns the other parent EntityTag.
    // <context.item> returns the ItemTag used to initiate breeding, if it exists.
    // <context.experience> returns the amount of experience granted by breeding.
    //
    // @Determine
    // ElementTag(Number) to set the amount of experience granted by breeding.
    //
    // -->

    public EntityBreedScriptEvent() {
        registerCouldMatcher("<entity> breeds");
    }

    private EntityTag entity;
    private EntityTag breeder;
    private EntityTag father;
    private EntityTag mother;
    private ItemTag item;
    private int experience;
    public EntityBreedEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(0, entity)) {
            return false;
        }
        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag element && element.isInt()) {
            experience = element.asInt();
            event.setExperience(experience);
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("child")) {
            return entity.getDenizenObject();
        }
        else if (name.equals("breeder") && breeder != null) {
            return breeder.getDenizenObject();
        }
        else if (name.equals("father")) {
            return father.getDenizenObject();
        }
        else if (name.equals("mother")) {
            return mother.getDenizenObject();
        }
        else if (name.equals("item") && item != null) {
            return item;
        }
        else if (name.equals("experience")) {
            return new ElementTag(experience);
        }
        return super.getContext(name);
    }

    @Override
    public void cancellationChanged() {
        // Prevent entities from continuing to breed with each other
        if (cancelled) {
            ((Animals) father.getLivingEntity()).setLoveModeTicks(0);
            ((Animals) mother.getLivingEntity()).setLoveModeTicks(0);
        }
        super.cancellationChanged();
    }

    @EventHandler
    public void onEntityBreeds(EntityBreedEvent event) {
        Entity entity = event.getEntity();
        this.entity = new EntityTag(entity);
        breeder = event.getBreeder() == null ? null : new EntityTag(event.getBreeder());
        father = new EntityTag(event.getFather());
        mother = new EntityTag(event.getMother());
        item = event.getBredWith() == null ? null : new ItemTag(event.getBredWith());
        experience = event.getExperience();
        this.event = event;
        EntityTag.rememberEntity(entity);
        fire(event);
        EntityTag.forgetEntity(entity);
    }
}

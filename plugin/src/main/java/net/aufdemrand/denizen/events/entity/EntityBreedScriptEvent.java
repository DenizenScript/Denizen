package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;

public class EntityBreedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity breeds (in <area>)
    // <entity> breeds (in <area>)
    //
    // @Regex ^on [^\s]+ breeds( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
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
        String lower = path.eventLower;

        if (!tryEntity(entity, CoreUtilities.getXthArg(0, lower))) {
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
        if (aH.matchesInteger(determination)) {
            experience = aH.getIntegerFrom(determination);
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(null, null);
    }

    @Override
    public dObject getContext(String name) {
        switch (name) {
            case "child":
                return entity;
            case "breeder":
                return breeder;
            case "father":
                return father;
            case "mother":
                return mother;
            case "item":
                return item;
            case "experience":
                return new Element(experience);
            default:
                return super.getContext(name);
        }
    }

    @EventHandler
    public void onEntityBreeds(EntityBreedEvent event) {
        Entity entity = event.getEntity();
        this.entity = new dEntity(entity);
        breeder = new dEntity(event.getBreeder());
        father = new dEntity(event.getFather());
        mother = new dEntity(event.getMother());
        item  = new dItem(event.getBredWith());
        experience = event.getExperience();
        cancelled = event.isCancelled();
        boolean wasCancelled = cancelled;
        this.event = event;
        dEntity.rememberEntity(entity);
        fire();
        dEntity.forgetEntity(entity);
        event.setCancelled(cancelled);
        event.setExperience(experience);

        // Prevent entities from continuing to breed with each other
        if (cancelled && !wasCancelled) {
            NMSHandler.getInstance().getEntityHelper().setBreeding((Animals) father.getLivingEntity(), false);
            NMSHandler.getInstance().getEntityHelper().setBreeding((Animals) mother.getLivingEntity(), false);
        }
    }
}

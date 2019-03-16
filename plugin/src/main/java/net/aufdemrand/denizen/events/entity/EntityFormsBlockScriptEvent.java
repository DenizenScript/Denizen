package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.EntityBlockFormEvent;

public class EntityFormsBlockScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity forms block
    // entity forms <block>
    // <entity> forms block
    // <entity> forms <block>
    //
    // @Regex ^on [^\s]+ forms [^\s]+$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a block is formed by an entity.
    // For example, when a snowman forms snow.
    //
    // @Context
    // <context.location> returns the dLocation the block.
    // <context.material> returns the dMaterial of the block.
    // <context.entity> returns the dEntity that formed the block.
    //
    // -->

    public EntityFormsBlockScriptEvent() {
        instance = this;
    }

    public static EntityFormsBlockScriptEvent instance;
    public dMaterial material;
    public dLocation location;
    public dEntity entity;
    public EntityBlockFormEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return CoreUtilities.getXthArg(1, lower).equals("forms");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String lower = path.eventLower;

        if (!tryEntity(entity, path.eventArgLowerAt(0))) {
            return false;
        }

        if (!tryMaterial(material, path.eventArgLowerAt(2))) {
            return false;
        }

        if (!runInCheck(path, location)) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "EntityFormsBlock";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? dEntity.getPlayerFrom(event.getEntity()) : null,
                entity.isCitizensNPC() ? dEntity.getNPCFrom(event.getEntity()) : null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("material")) {
            return material;
        }
        else if (name.equals("entity")) {
            return entity;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityFormsBlock(EntityBlockFormEvent event) {
        location = new dLocation(event.getBlock().getLocation());
        material = new dMaterial(event.getBlock());
        entity = new dEntity(event.getEntity());
        this.event = event;
        fire(event);
    }
}

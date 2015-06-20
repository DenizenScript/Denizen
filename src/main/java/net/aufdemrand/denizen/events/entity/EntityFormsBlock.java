package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.EntityBlockFormEvent;

import java.util.HashMap;

public class EntityFormsBlock extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // entity forms block (in <notable cuboid>)
    // entity forms <block> (in <notable cuboid>)
    // <entity> forms block (in <notable cuboid>)
    // <entity> forms <block> (in <notable cuboid>)
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

    public EntityFormsBlock() {
        instance = this;
    }
    public static EntityFormsBlock instance;
    public dMaterial material;
    public dLocation location;
    public dEntity entity;
    public EntityBlockFormEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String entTest = CoreUtilities.getXthArg(1, lower);
        String matTest = CoreUtilities.getXthArg(2, lower);
        return CoreUtilities.getXthArg(1, lower).equals("forms")
                && (entTest.equals("entity") || dEntity.matches(entTest))
                && (matTest.equals("block") || dMaterial.matches(matTest));
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);

        if (!entity.matchesEntity(CoreUtilities.getXthArg(0, lower))) {
            return false;
        }

        String mat = CoreUtilities.getXthArg(2, lower);
        if (!mat.equals("block")
                && !mat.equals(material.identifyNoIdentifier()) && !mat.equals(material.identifySimpleNoIdentifier())) {
            return false;
        }

        if (!runInCheck(scriptContainer, s, lower, location)) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "EntityFormsBlock";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        EntityBlockFormEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity.isPlayer() ? dEntity.getPlayerFrom(event.getEntity()): null,
                entity.isCitizensNPC() ? dEntity.getNPCFrom(event.getEntity()): null);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("location", location);
        context.put("material", material);
        context.put("entity", entity);
        return context;
    }

    @EventHandler
    public void onEntityFormsBlock(EntityBlockFormEvent event) {
        location = new dLocation(event.getBlock().getLocation());
        material = dMaterial.getMaterialFrom(event.getBlock().getType(), event.getBlock().getData());
        entity = new dEntity(event.getEntity());
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
    }
}

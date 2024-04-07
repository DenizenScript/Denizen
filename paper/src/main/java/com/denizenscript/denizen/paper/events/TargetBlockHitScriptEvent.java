package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import io.papermc.paper.event.block.TargetHitEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


public class TargetBlockHitScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // targetblock hit
    //
    // @Location true
    //
    // @Group Paper
    //
    // @Plugin Paper
    //
    // @Triggers when a target block is hit by a projectile such as an arrow.
    //
    // @Context
    // <context.projectile> returns an EntityTag of the projectile.
    // <context.shooter> returns a EntityTag of the shooter of the projectile.
    // <context.hit_block> returns a LocationTag of the block
    // <context.hit_face> returns a LocationTag of the face
    // <context.strength> returns a ElementTag of the emitted redstone strength
    //
    // @Player when the shooter is a player.
    //
    // @NPC when the shooter is a npc.
    // -->

    public TargetBlockHitScriptEvent() {
        registerCouldMatcher("targetblock hit");
    }

    public TargetHitEvent event;
    public LocationTag hitBlock;
    public EntityTag projectile;
    public EntityTag shooter;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, hitBlock)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "projectile" -> projectile.getDenizenObject();
            case "hit_block" -> hitBlock;
            case "hit_face" -> event.getHitBlockFace() != null ? new LocationTag(event.getHitBlockFace().getDirection()) : null;
            case "shooter" -> shooter != null ? shooter.getDenizenObject() : null;
            case "strength" ->  new ElementTag(event.getSignalStrength());
            default -> super.getContext(name);
        };
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(shooter);
    }

    @EventHandler
    public void onProjectileHit(TargetHitEvent event) {
        this.event = event;
        projectile = new EntityTag(event.getEntity());
        hitBlock = new LocationTag(event.getHitBlock().getLocation());
        shooter = projectile.getShooter();
        fire(event);
    }
}

package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class ProjectileHitsBlockScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // projectile hits block
    // projectile hits <material>
    // <projectile> hits block
    // <projectile> hits <material>
    //
    // @Regex ^on [^\s]+ hits [^\s]+$
    //
    // @Synonyms fish hook lands on block, arrow hits block, snowball hits block
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a projectile hits a block.
    //
    // @Context
    // <context.projectile> returns the EntityTag of the projectile.
    // <context.shooter> returns the EntityTag of the shooter, if there is one.
    // <context.location> returns a LocationTag of the block that was hit.
    // <context.hit_face> returns a LocationTag vector of the hit normal (like '0,1,0' if the projectile hit the top of the block).
    //
    // @Player when the entity that shot the projectile is a player.
    //
    // @NPC when the entity that shot the projectile is an NPC.
    //
    // -->

    // <--[event]
    // @Events
    // entity shoots block
    // entity shoots <material>
    // <entity> shoots block
    // <entity> shoots <material>
    //
    // @Switch with:<projectile>
    //
    // @Regex ^on [^\s]+ shoots [^\s]+$
    //
    // @Group Entity
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a projectile shot by an entity hits a block.
    //
    // @Context
    // <context.projectile> returns the EntityTag of the projectile.
    // <context.shooter> returns the EntityTag of the shooter, if there is one.
    // <context.location> returns the LocationTag of the block that was hit.
    // <context.hit_face> returns a LocationTag vector of the hit normal (like '0,1,0' if the projectile hit the top of the block).
    //
    // @Player when the entity that shot the projectile is a player.
    //
    // @NPC when the entity that shot the projectile is an NPC.
    //
    // -->
    public ProjectileHitsBlockScriptEvent() {
    }

    public EntityTag projectile;
    public EntityTag shooter;
    public LocationTag location;
    private MaterialTag material;
    public ProjectileHitEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        String cmd = path.eventArgLowerAt(1);
        if (!cmd.equals("hits") && !cmd.equals("shoots")) {
            return false;
        }
        if (!couldMatchEntity(path.eventArgLowerAt(0))) {
            return false;
        }
        if (!couldMatchBlock(path.eventArgLowerAt(2))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean matches(ScriptPath path) {
        String cmd = path.eventArgLowerAt(1);
        String pTest = "";
        if (cmd.equals("hits")) {
            pTest = path.eventArgLowerAt(0);
        }
        else if (cmd.equals("shoots")) {
            if (shooter == null || !shooter.tryAdvancedMatcher(path.eventArgLowerAt(0))) {
                return false;
            }
            if (path.eventArgLowerAt(3).equals("with")) {
                pTest = path.eventArgLowerAt(4);
            }
        }
        if (!pTest.isEmpty() && !pTest.equals("projectile") && !projectile.tryAdvancedMatcher(pTest)) {
            return false;
        }
        if (!path.tryObjectSwitch("with", projectile)) {
            return false;
        }
        if (!material.tryAdvancedMatcher(path.eventArgLowerAt(2))) {
            return false;
        }
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);

    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(shooter);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("projectile")) {
            return projectile.getDenizenObject();
        }
        else if (name.equals("location")) {
            return location;
        }
        else if (name.equals("hit_face") && event.getHitBlockFace() != null) {
            return new LocationTag(event.getHitBlockFace().getDirection());
        }
        else if (name.equals("shooter") && shooter != null) {
            return shooter.getDenizenObject();
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onProjectileHits(ProjectileHitEvent event) {
        projectile = new EntityTag(event.getEntity());
        if (projectile.getLocation() == null) {
            return; // No, I can't explain how or why this would ever happen... nonetheless, it appears it does happen sometimes.
        }
        if (Double.isNaN(projectile.getLocation().getDirection().normalize().getX())) {
            return; // I can't explain this one either. It also chooses to happen whenever it pleases.
        }
        Block block = event.getHitBlock();
        if (block == null) {
            return;
        }
        material = new MaterialTag(block);
        shooter = projectile.getShooter();
        location = new LocationTag(block.getLocation());
        this.event = event;
        fire(event);
    }
}

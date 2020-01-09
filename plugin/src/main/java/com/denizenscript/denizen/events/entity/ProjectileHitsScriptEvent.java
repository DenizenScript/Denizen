package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class ProjectileHitsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // projectile hits block
    // projectile hits <material>
    // <projectile> hits block
    // <projectile> hits <material>
    //
    // @Regex ^on [^\s]+ hits [^\s]+$
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Triggers when a projectile hits a block.
    //
    // @Context
    // <context.projectile> returns the EntityTag of the projectile.
    // <context.shooter> returns the EntityTag of the shooter, if there is one.
    // <context.location> returns the LocationTag of the block that was hit.
    //
    // -->

    // <--[event]
    // @Events
    // entity shoots block
    // entity shoots <material> (with <projectile>)
    // <entity> shoots block
    // <entity> shoots <material> (with <projectile>)
    //
    // @Regex ^on [^\s]+ shoots [^\s]+$
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Triggers when a projectile shot by an entity hits a block.
    //
    // @Context
    // <context.projectile> returns the EntityTag of the projectile.
    // <context.shooter> returns the EntityTag of the shooter, if there is one.
    // <context.location> returns the LocationTag of the block that was hit.
    //
    // -->
    public ProjectileHitsScriptEvent() {
        instance = this;
    }

    public static ProjectileHitsScriptEvent instance;
    public EntityTag projectile;
    public EntityTag shooter;
    public LocationTag location;
    private MaterialTag material;
    public ProjectileHitEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("hits") || cmd.equals("shoots");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String cmd = path.eventArgLowerAt(1);
        String pTest = "";

        if (cmd.equals("hits")) {
            pTest = path.eventArgLowerAt(0);
        }
        else if (cmd.equals("shoots")) {
            if (shooter == null || !tryEntity(shooter, path.eventArgLowerAt(0))) {
                return false;
            }
            if (path.eventArgLowerAt(3).equals("with")) {
                pTest = path.eventArgLowerAt(4);
            }
        }
        if (!pTest.isEmpty() && !pTest.equals("projectile") && !tryEntity(projectile, pTest)) {
            return false;
        }

        if (!tryMaterial(material, path.eventArgLowerAt(2))) {
            return false;
        }

        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);

    }

    @Override
    public String getName() {
        return "ProjectileHits";
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

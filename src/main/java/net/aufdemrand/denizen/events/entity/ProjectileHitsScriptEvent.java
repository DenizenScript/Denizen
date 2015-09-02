package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.BlockIterator;

public class ProjectileHitsScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // projectile hits block (in <area>)
    // projectile hits <material> (in <area>)
    // <projectile> hits block (in <area>)
    // <projectile> hits <material> (in <area>)
    //
    // @Regex ^on [^\s]+ hits [^\s]+( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Cancellable false
    //
    // @Triggers when a projectile hits a block.
    //
    // @Context
    // <context.projectile> returns the dEntity of the projectile.
    // <context.shooter> returns the dEntity of the shooter, if there is one.
    // <context.location> returns the dLocation of the block that was hit.
    //
    // -->
    // <--[event]
    // @Events
    // entity shoots block (in <area>)
    // entity shoots <material> (with <projectile>) (in <area>)
    // <entity> shoots block (in <area>)
    // <entity> shoots <material> (with <projectile>) (in <area>)
    //
    // @Triggers when a projectile shot by an entity hits a block.
    //
    // @Context
    // <context.projectile> returns the dEntity of the projectile.
    // <context.shooter> returns the dEntity of the shooter, if there is one.
    // <context.location> returns the dLocation of the block that was hit.
    //
    // -->
    public ProjectileHitsScriptEvent() {
        instance = this;
    }

    public static ProjectileHitsScriptEvent instance;
    public dEntity projectile;
    public dEntity shooter;
    public dLocation location;
    private dMaterial material;
    public ProjectileHitEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("hits") || cmd.equals("shoots");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        String pTest = cmd.equals("hits") ? CoreUtilities.getXthArg(0, lower) : CoreUtilities.getXthArg(4, lower);

        if (pTest.length() > 0 && !projectile.matchesEntity(pTest)) {
            return false;
        }

        String mat = CoreUtilities.getXthArg(2, lower);
        if (!tryMaterial(material, mat)) {
            return false;
        }

        String sTest = cmd.equals("shoots") ? CoreUtilities.getXthArg(0, lower) : "";
        if (shooter != null && sTest.length() > 0 && !shooter.matchesEntity(sTest)) {
            return false;
        }

        if (!runInCheck(scriptContainer, s, lower, location)) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "ProjectileHits";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        ProjectileHitEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(shooter != null && shooter.isPlayer() ? dEntity.getPlayerFrom(event.getEntity()) : null,
                shooter != null && shooter.isCitizensNPC() ? dEntity.getNPCFrom(event.getEntity()) : null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("projectile")) {
            return projectile;
        }
        else if (name.equals("location")) {
            return location;
        }
        else if (name.equals("shooter") && shooter != null) {
            return shooter;
        }
        return super.getContext(name);
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileHits(ProjectileHitEvent event) {
        projectile = new dEntity(event.getEntity());
        if (projectile.getLocation() == null)
            return; // No, I can't explain how or why this would ever happen... nonetheless, it appears it does happen sometimes.

        if (Double.isNaN(projectile.getLocation().getDirection().normalize().getX()))
            return; // I can't explain this one either. It also chooses to happen whenever it pleases.

        Block block = null;
        try {
            BlockIterator bi = new BlockIterator(projectile.getLocation().getWorld(),
                    projectile.getLocation().toVector(), projectile.getLocation().getDirection().normalize(), 0, 4);
            while (bi.hasNext()) {
                block = bi.next();
                if (block.getTypeId() != 0) {
                    break;
                }
            }
        }
        catch (IllegalStateException ex) {
            // This happens because it can. Also not explainable whatsoever.
            // As this error happens on no fault of the user, display no error message... just cancel the event.
            return;
        }

        if (block == null) {
            return;
        }
        material = dMaterial.getMaterialFrom(block.getType(), block.getData());
        shooter = projectile.getShooter();
        location = new dLocation(block.getLocation());
        this.event = event;
        fire();
    }
}

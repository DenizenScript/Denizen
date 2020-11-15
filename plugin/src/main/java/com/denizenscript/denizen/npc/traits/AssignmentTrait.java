package com.denizenscript.denizen.npc.traits;

import com.denizenscript.denizen.events.bukkit.ScriptReloadEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.containers.core.AssignmentScriptContainer;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Paginator;
import net.citizensnpcs.util.Messages;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AssignmentTrait extends Trait {

    @Persist
    private String assignment = "";

    public AssignmentTrait() {
        super("assignment");
    }

    /**
     * Checks to see if the NPCs assignment is still a valid script on load of NPC.
     */
    @Override
    public void load(DataKey key) throws NPCLoadException {
        if (hasAssignment()) {
            Debug.echoError("Missing assignment '" + assignment + "' for NPC '" + npc.getName() + "/" + npc.getId() + "! Perhaps the script has been removed?");
        }
        npc.getOrAddTrait(ConstantsTrait.class).rebuildAssignmentConstants();
    }

    // <--[action]
    // @Actions
    // assignment
    //
    // @Triggers when the NPC receives an assignment via '/npc assign --set Name'.
    //
    // @Context
    // None
    //
    // -->

    /**
     * Sets the NPCs Assignment Script and fires an 'On Assignment:' action. Can specify a player for
     * context with the action.
     *
     * @param assignment the name of the Assignment Script, case in-sensitive
     * @param player     the player adding the assignment, can be null
     * @return false if the assignment is invalid
     */
    public boolean setAssignment(String assignment, PlayerTag player) {
        cachedContainer = null;
        if (ScriptRegistry.containsScript(assignment, AssignmentScriptContainer.class)) {
            this.assignment = assignment.toUpperCase();
            // Add Constants/Trigger trait if not already added to the NPC.
            if (!npc.hasTrait(ConstantsTrait.class)) {
                npc.addTrait(ConstantsTrait.class);
            }
            if (!npc.hasTrait(TriggerTrait.class)) {
                npc.addTrait(TriggerTrait.class);
            }
            if (Settings.healthTraitEnabledByDefault()) {
                if (!npc.hasTrait(HealthTrait.class)) {
                    npc.addTrait(HealthTrait.class);
                }
            }
            // Reset Constants
            npc.getOrAddTrait(ConstantsTrait.class).rebuildAssignmentConstants();
            // 'On Assignment' action.
            new NPCTag(npc).action("assignment", player);
            return true;
        }
        else {
            return false;
        }
    }

    private AssignmentScriptContainer cachedContainer = null;

    public AssignmentScriptContainer getAssignment() {
        if (cachedContainer != null) {
            return cachedContainer;
        }
        if (assignment == null || assignment.equals("")) {
            return null;
        }
        ScriptContainer script = ScriptRegistry.getScriptContainer(assignment);
        if (script instanceof AssignmentScriptContainer) {
            cachedContainer = (AssignmentScriptContainer) script;
        }
        return cachedContainer;
    }

    @EventHandler
    public void onReload(ScriptReloadEvent event) {
        cachedContainer = null;
    }

    /**
     * Checks to see if this NPC currently has an assignment.
     *
     * @return true if NPC has an assignment and it is valid
     */
    public boolean hasAssignment() {
        if (assignment == null || assignment.equals("")) {
            return false;
        }
        return ScriptRegistry.containsScript(assignment);
    }

    // <--[action]
    // @Actions
    // remove assignment
    //
    // @Triggers when the NPC loses its assignment.
    //
    // @Context
    // None
    //
    // -->

    /**
     * Removes the current assignment and fires an 'On Remove Assignment:' action. Can specify a player for
     * context with the action.
     *
     * @param player the player removing the assignment, can be null
     */
    public void removeAssignment(PlayerTag player) {
        new NPCTag(npc).action("remove assignment", player);
        cachedContainer = null;
        assignment = "";
    }

    public void describe(CommandSender sender, int page) throws CommandException {
        AssignmentScriptContainer assignmentScript = ScriptRegistry.getScriptContainer(assignment);
        Paginator paginator = new Paginator().header("Assignment");
        paginator.addLine("<e>Current assignment: " + (hasAssignment() ? this.assignment : "None.") + "");
        paginator.addLine("");
        if (!hasAssignment()) {
            paginator.sendPage(sender, page);
            return;
        }
        // Interact Scripts
        boolean entriesPresent = false;
        paginator.addLine(ChatColor.GRAY + "Interact Scripts:");
        paginator.addLine("<e>Key: <a>Priority  <b>Name");
        if (assignmentScript.contains("INTERACT SCRIPTS")) {
            entriesPresent = true;
            for (String scriptEntry : assignmentScript.getStringList("INTERACT SCRIPTS")) {
                paginator.addLine("<b>" + scriptEntry);
            }
        }
        if (!entriesPresent) {
            paginator.addLine("<c>No Interact Scripts assigned.");
        }
        paginator.addLine("");
        if (!entriesPresent) {
            if (!paginator.sendPage(sender, page)) {
                throw new CommandException(Messages.COMMAND_PAGE_MISSING);
            }
            return;
        }
        // Scheduled Activities
        entriesPresent = false;
        paginator.addLine(ChatColor.GRAY + "Scheduled Scripts:");
        paginator.addLine("<e>Key: <a>Time  <b>Name");
        if (assignmentScript.contains("SCHEDULED ACTIVITIES")) {
            entriesPresent = true;
            for (String scriptEntry : assignmentScript.getStringList("SCHEDULED ACTIVITIES")) {
                paginator.addLine("<a>" + scriptEntry.split(" ")[0] + "<b> " + scriptEntry.split(" ", 2)[1]);
            }
        }
        if (!entriesPresent) {
            paginator.addLine("<c>No scheduled scripts activities.");
        }
        paginator.addLine("");
        // Actions
        entriesPresent = false;
        paginator.addLine(ChatColor.GRAY + "Actions:");
        paginator.addLine("<e>Key: <a>Action name  <b>Script Size");
        if (assignmentScript.contains("ACTIONS")) {
            entriesPresent = true;
        }
        if (entriesPresent) {
            for (StringHolder action : assignmentScript.getConfigurationSection("ACTIONS").getKeys(false)) {
                paginator.addLine("<a>" + action.str + " <b>" + assignmentScript.getStringList("ACTIONS." + action.str).size());
            }
        }
        else {
            paginator.addLine("<c>No actions defined in the assignment.");
        }
        paginator.addLine("");
        if (!paginator.sendPage(sender, page)) {
            throw new CommandException(Messages.COMMAND_PAGE_MISSING, page);
        }
    }

    @Override
    public void onSpawn() {
        entityId = npc.getEntity().getUniqueId();
    }

    // <--[action]
    // @Actions
    // death
    // death by entity
    // death by <entity>
    // death by block
    // death by <cause>
    //
    // @Triggers when the NPC dies.
    //
    // @Context
    // <context.killer> returns the entity that killed the NPC (if any)
    // <context.shooter> returns the shooter of the killing projectile (if any)
    // <context.damage> returns the last amount of damage applied (if any)
    // <context.death_cause> returns the last damage cause (if any)
    //
    // -->
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent deathEvent) {
        if (!deathEvent.getEntity().getUniqueId().equals(entityId)) {
            return;
        }
        EntityDamageEvent event = deathEvent.getEntity().getLastDamageCause();
        String deathCause = event == null ? "unknown" : CoreUtilities.toLowerCase(event.getCause().toString()).replace('_', ' ');
        Map<String, ObjectTag> context = new HashMap<>();
        context.put("damage", new ElementTag(event == null ? 0 : event.getDamage()));
        context.put("death_cause", new ElementTag(deathCause));
        PlayerTag player = null;
        if (event instanceof EntityDamageByEntityEvent) {
            Entity killerEntity = ((EntityDamageByEntityEvent) event).getDamager();
            context.put("killer", new EntityTag(killerEntity).getDenizenObject());
            if (killerEntity instanceof Player) {
                player = PlayerTag.mirrorBukkitPlayer((Player) killerEntity);
            }
            else if (killerEntity instanceof Projectile) {
                ProjectileSource shooter = ((Projectile) killerEntity).getShooter();
                if (shooter instanceof LivingEntity) {
                    context.put("shooter", new EntityTag((LivingEntity) shooter).getDenizenObject());
                    if (shooter instanceof Player) {
                        player = PlayerTag.mirrorBukkitPlayer((Player) shooter);
                    }
                    new NPCTag(npc).action("death by " + ((LivingEntity) shooter).getType().toString(), player, context);
                }
            }
            new NPCTag(npc).action("death by entity", player, context);
            new NPCTag(npc).action("death by " + killerEntity.getType().toString(), player, context);
        }
        else if (event instanceof EntityDamageByBlockEvent) {
            new NPCTag(npc).action("death by block", null, context);
        }
        new NPCTag(npc).action("death", player, context);
        new NPCTag(npc).action("death by " + deathCause, player, context);
    }

    private UUID entityId;

    // <--[action]
    // @Actions
    // hit
    // hit on <entity>
    //
    // @Triggers when the NPC hits an enemy.
    //
    // @Context
    // None
    //
    // -->
    // <--[action]
    // @Actions
    // kill
    // kill of <entity>
    //
    // @Triggers when the NPC kills an enemy.
    //
    // @Context
    // None
    //
    // -->
    @EventHandler(priority = EventPriority.MONITOR)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!npc.isSpawned()) {
            return;
        }
        if (event.getDamager() != npc.getEntity()) {
            if (event.getDamager() instanceof Projectile) {
                if (((Projectile) event.getDamager()).getShooter() != npc.getEntity()) {
                    return;
                }
            }
            else {
                return;
            }
        }
        PlayerTag player = null;
        if (event.getEntity() instanceof Player) {
            player = PlayerTag.mirrorBukkitPlayer((Player) event.getEntity());
        }
        // TODO: Context containing the entity hit
        new NPCTag(npc).action("hit", player);
        new NPCTag(npc).action("hit on " + event.getEntityType().name(), player);
        if (event.getEntity() instanceof LivingEntity) {
            if (((LivingEntity) event.getEntity()).getHealth() - event.getFinalDamage() <= 0) {
                new NPCTag(npc).action("kill", player);
                new NPCTag(npc).action("kill of " + event.getEntityType().name(), player);
            }
        }
    }
}

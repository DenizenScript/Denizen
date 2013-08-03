package net.aufdemrand.denizen.listeners.core;

import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.listeners.AbstractListener;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dCuboid;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizen.utilities.depends.WorldGuardUtilities;

import net.citizensnpcs.api.CitizensAPI;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.EventHandler;

import java.util.Arrays;
import java.util.List;


public class KillListenerInstance extends AbstractListener implements Listener {

    public static enum KillType { NPC, PLAYER, GROUP, ENTITY }

    //
    // The type of Kill
    //
    KillType type = null;

    //
    // The targets
    //
    dList targets;

    //
    // The counters
    //
    int required = 1;
    int kills_so_far = 0;

    //
    // Modifiers
    //
    String region = null;
    dCuboid cuboid = null;


    @Override
    public String report() {
        // Called by the '/denizen listener --report id' command, meant to give information
        // to server-operators about the current status of this listener.
        return player.getName() + " currently has quest listener '" + id
                + "' active and must kill " + Arrays.toString(targets.toArray())
                + " '" + type.name() + "'(s). Current progress '" + kills_so_far + "/" + required + "'.";
    }


    @Override
    public void onBuild(List<aH.Argument> args) {

        // Build the listener from script arguments. onBuild() is called when a new listener is
        // made with the LISTEN command. All arguments except type, id, and script
        // are passed through to here.
        for (aH.Argument arg : args) {

            if (arg.matchesEnum(KillType.values()) && type == null)
                this.type = KillType.valueOf(arg.getValue().toUpperCase());

            else if (arg.matchesPrefix("qty, q")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer))
                this.required = aH.getIntegerFrom(arg.getValue());

            else if (arg.matchesPrefix("region, r"))
                this.region = arg.getValue();

            else if (arg.matchesPrefix("cuboid, c")
                    && arg.matchesArgumentType(dCuboid.class))
                this.cuboid = arg.asType(dCuboid.class);

            else if (arg.matchesPrefix("targets, target, t, name, names"))
                targets = (dList) arg.asType(dList.class);

        }

        if (type == null) {
            dB.echoError("Missing TYPE argument! Valid: NPC, ENTITY, PLAYER, GROUP");
            cancel();
        }

        // At this point, constructed() is called.
    }


    @Override
    public void onLoad() {

        // Build the listener from saved data. id and type are saved automatically.
        // onBuild() will not be called, this should handle everything onBuild() would with the
        // saved data from onSave().
        type = KillType.valueOf(((String) get("Type")));
        targets = new dList((List<String>) get("Targets"));
        required = (Integer) get("Quantity");
        kills_so_far = (Integer) get("Current Kills");
        region = (String) get("Region");
        cuboid = dCuboid.valueOf((String) get("Cuboid"));

        // At this point, constructed() is called.
    }


    @Override
    public void onSave() {

        // If the player leaves the game while a listener is in progress, save the information
        // so that it can be rebuilt onLoad(). id and type are done automatically.
        store("Type", type.name());
        store("Targets", targets);
        store("Quantity", required);
        store("Current Kills", kills_so_far);
        store("Region", region);
        if (cuboid != null) store("Cuboid", cuboid.identify());

        // At this point, deconstructed() is called.
    }


    @Override
    public void onFinish() {

        // Nothing to do here for now, but this is called when the quest listener is
        // finished, after the script is run, and right before deconstructed().

        // At this point, deconstructed() is called.
    }


    @Override
    public void onCancel() {

        // Nothing to do here for now, but this is called when the quest listener is
        // cancelled, right before deconstructed().

        // At this point, deconstructed() is called.
    }


    @Override
    public void constructed() {

        // Called after build and load methods. Perfect place to register
        // any bukkit events!
        denizen.getServer().getPluginManager().registerEvents(this, denizen);

        // Report to the console
        dB.log(report());
    }


    @Override
    public void deconstructed() {

        // Called when the instance is deconstructed due to either it being
        // saved, finished, or cancelled.
        // This is the perfect place to unregister any bukkit events so it
        // can be cleanly removed from memory.
        EntityDeathEvent.getHandlerList().unregister(this);
    }


    @EventHandler
    public void listen(EntityDeathEvent event) {

        // Only continue if the event is an event for the player that owns this listener.
        if (event.getEntity().getKiller() != player.getPlayerEntity()) return;

        // If REGION argument specified, check. If not in region, don't count kill!
        if (region != null)
            if (!WorldGuardUtilities.inRegion(player.getLocation(), region)) return;
        // Same with the CUBOID argument...
        if (cuboid != null)
            if (!cuboid.isInsideCuboid(player.getLocation())) return;

        // Check type!
        if (type == KillType.ENTITY) {

            dEntity ent = new dEntity(event.getEntity());
            dB.log("Entity killed: " + ent.identify());
            boolean count_it = false;

            for (String target : targets) {
                if (dEntity.valueOf(target) != null)
                    if (ent.comparesTo(dEntity.valueOf(target)) == 1)
                        count_it = true;
            }

            if (count_it
                    || targets.contains("*")) {
                kills_so_far++;
                dB.log(player.getName() + " killed a "
                        + ent.identify() + ". Current progress '"
                        + kills_so_far + "/" + required + "'.");
                check();
            }

        } else if (type == KillType.NPC) {
            if (CitizensAPI.getNPCRegistry().isNPC(event.getEntity()))
                if (targets.contains(CitizensAPI.getNPCRegistry().getNPC(event.getEntity()).getName().toUpperCase())
                        || targets.contains("*")
                        || targets.contains(String.valueOf(CitizensAPI.getNPCRegistry().getNPC(event.getEntity()).getId() ))) {
                    kills_so_far++;
                    dB.log(player.getName() + " killed "
                            + String.valueOf(CitizensAPI.getNPCRegistry().getNPC(event.getEntity()).getId())
                            + "/" + CitizensAPI.getNPCRegistry().getNPC(event.getEntity()).getName()
                            + ". Current progress '" + kills_so_far + "/" + required + "'.");
                    check();
                }

        } else if (type == KillType.PLAYER) {
            if (event.getEntityType() == EntityType.PLAYER)
                if (targets.contains(((Player) event.getEntity()).getName().toUpperCase())
                        || targets.contains("*")
                        || targets.isEmpty()) {
                    kills_so_far++;
                    dB.log(player.getName() + " killed "
                            + ((Player) event.getEntity()).getName().toUpperCase()
                            + ". Current progress '" + kills_so_far + "/" + required + "'.");
                    check();
                }

        } else if (type == KillType.GROUP) {
            if (event.getEntityType() == EntityType.PLAYER)
                for (String group : Depends.permissions.getPlayerGroups((Player) event.getEntity()))
                    if (targets.contains(group.toUpperCase())) {
                        kills_so_far++;
                        dB.log(player.getName() + " killed " + ((Player) event.getEntity()).getName().toUpperCase() + " of group " + group + ".");
                        check();
                        break;
                    }
        }


    }


    public void check() {

        // Check current kills vs. required kills; finish() if necessary.
        if (kills_so_far >= required)
            finish();
    }

    @EventHandler
    public void listenTag(ReplaceableTagEvent event) {

        if (!event.matches("LISTENER")) return;
        if (!event.getType().equalsIgnoreCase(id)) return;

        if (event.getValue().equalsIgnoreCase("region")) {
            event.setReplaced(region);
        }

        else if (event.getValue().equalsIgnoreCase("required")) {
            event.setReplaced(String.valueOf(required));
        }

        else if (event.getValue().equalsIgnoreCase("currentkills")) {
            event.setReplaced(String.valueOf(kills_so_far));
        }

        else if (event.getValue().equalsIgnoreCase("targets")) {
            String targetList = "";
            for (String curTar : targets){
                targetList = targetList + curTar + ", ";
                targetList = targetList.substring(0, targetList.length() - 1);
            }
            event.setReplaced(targetList);
        }
    }
}

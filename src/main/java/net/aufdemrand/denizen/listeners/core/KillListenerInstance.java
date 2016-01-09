package net.aufdemrand.denizen.listeners.core;

import net.aufdemrand.denizen.listeners.AbstractListener;
import net.aufdemrand.denizen.objects.dCuboid;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.tags.ReplaceableTagEvent;
import net.aufdemrand.denizencore.tags.TagManager;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Arrays;
import java.util.List;


public class KillListenerInstance extends AbstractListener implements Listener {

    public static enum KillType {NPC, PLAYER, GROUP, ENTITY}

    //
    // The type of Kill
    //
    KillType type = null;

    //
    // The targets
    //
    dList targets;

    //
    // The names
    //
    dList names;

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
                + " '" + type.name() + "'(s) named " + Arrays.toString(names.toArray()) + ". Current progress '" + kills_so_far + "/" + required + "'.";
    }


    @Override
    public void onBuild(List<aH.Argument> args) {
        // Build the listener from script arguments. onBuild() is called when a new listener is
        // made with the LISTEN command. All arguments except type, id, and script
        // are passed through to here.
        for (aH.Argument arg : args) {

            if (arg.matchesEnum(KillType.values()) && type == null) {
                this.type = KillType.valueOf(arg.getValue().toUpperCase());
            }

            else if (arg.matchesPrefix("qty, q")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                this.required = aH.getIntegerFrom(arg.getValue());
            }

            else if (arg.matchesPrefix("region, r")) {
                this.region = arg.getValue();
            }

            else if (arg.matchesPrefix("cuboid, c")
                    && arg.matchesArgumentType(dCuboid.class)) {
                this.cuboid = arg.asType(dCuboid.class);
            }

            else if (arg.matchesPrefix("targets, target, t, name, names")) {
                targets = arg.asType(dList.class);
            }
            else if (arg.matchesPrefix("mobnames, mn")) {
                names = arg.asType(dList.class);
            }

        }

        if (targets == null) {
            targets = new dList("*");
        }

        if (names == null) {
            names = new dList("*");
        }

        if (type == null) {
            dB.echoError("Missing TYPE argument! Valid: NPC, ENTITY, PLAYER, GROUP");
            cancel();
        }
        else if (type == KillType.NPC && Depends.citizens == null) {
            dB.echoError("Invalid TYPE argument 'NPC': Citizens2 is not enabled.");
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
        names = new dList((List<String>) get("Names"));
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
        store("Names", names);
        store("Quantity", required);
        store("Current Kills", kills_so_far);
        store("Region", region);
        if (cuboid != null) {
            store("Cuboid", cuboid.identify());
        }

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
        TagManager.registerTagEvents(this);

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
        TagManager.unregisterTagEvents(this);
    }


    @EventHandler
    public void listen(EntityDeathEvent event) {
        // Only continue if the event is an event for the player that owns this listener.
        if (event.getEntity().getKiller() != player.getPlayerEntity()) {
            return;
        }

        // If REGION argument specified, check. If not in region, don't count kill!
        if (region != null) {
            //if (!WorldGuardUtilities.inRegion(player.getLocation(), region)) return;
        }

        // Same with the CUBOID argument...
        if (cuboid != null) {
            if (!cuboid.isInsideCuboid(player.getLocation())) {
                return;
            }
        }

        //
        // ENTITY type Kill Listener
        //
        if (type == KillType.ENTITY) {
            // Get entity killed
            dEntity ent = new dEntity(event.getEntity());
            boolean count_it = false;
            // Check targets, if any match entity killed, count_it!
            for (String target : targets) {
                if (dEntity.valueOf(target) != null) {
                    if (ent.comparesTo(dEntity.valueOf(target)) == 1) {
                        count_it = true;
                    }
                }
            }
            boolean right_name = false;
            for (String name : names) {
                if (ChatColor.stripColor(ent.getName()).contains(name)) {
                    right_name = true;
                }
            }
            // If an entity was found, or targets is '*', increment the
            // kills_so_far
            if (count_it || targets.contains("*")) {
                if (right_name || names.contains("*")) {
                    kills_so_far++;
                    dB.log(player.getName() + " killed a "
                            + ent.identify() + ". Current progress '"
                            + kills_so_far + "/" + required + "'.");
                    // Check the number of kills so far
                    check();
                }
            }
        }

        //
        // NPC type Kill Listener
        //
        else if (type == KillType.NPC) {
            // If a NPC wasn't killed, return.
            if (!CitizensAPI.getNPCRegistry().isNPC(event.getEntity())) {
                return;
            }
            // Get the NPC killed
            dNPC npc = dNPC.mirrorCitizensNPC(CitizensAPI.getNPCRegistry().getNPC(event.getEntity()));
            boolean count_it = false;

            // Check targets, if any match entity killed, count_it!
            for (String target : targets) {
                // Check against a physical NPC object (n@7, n@18, etc.)
                if (dNPC.valueOf(target) != null) {
                    if (dNPC.valueOf(target).getId() == npc.getId()) {
                        count_it = true;
                    }
                }
                // Cannot be 'else if' since dNPC.valueOf will return true for names now, as well as ids.
                // Check against name of NPC (n@fullwall, aufdemrand, etc)... object notation is optional.
                if (npc.getName().equalsIgnoreCase(target.toLowerCase().replace("n@", ""))) {
                    count_it = true;
                }
            }
            // If NPC was matched or targets contains '*', increment
            // the kills so far.
            if (count_it || targets.contains("*")) {
                kills_so_far++;
                dB.log(player.getName() + " killed "
                        + npc.toString() + ". Current progress '"
                        + kills_so_far + "/" + required + "'.");
                // Check the number of kills so far
                check();
            }
        }

        //
        // PLAYER type Kill Listener
        //
        else if (type == KillType.PLAYER) {
            // Check to make sure entity is a Player, and not a NPC
            if (event.getEntityType() != EntityType.PLAYER) {
                return;
            }
            if (Depends.citizens != null && CitizensAPI.getNPCRegistry().isNPC(event.getEntity())) {
                return;
            }

            // Get player killed
            dPlayer player = dPlayer.mirrorBukkitPlayer((Player) event.getEntity());
            boolean count_it = false;
            // Check targets, if any match entity killed, count_it!
            for (String target : targets) {
                if (dPlayer.valueOf(target) != null) {
                    if (dPlayer.valueOf(target).getName().equalsIgnoreCase(player.getName())) {
                        count_it = true;
                    }
                }
            }
            // If an entity was found, or targets is '*', increment the
            // kills_so_far
            if (count_it || targets.contains("*")) {
                kills_so_far++;
                dB.log(player.getName() + " killed "
                        + player.getName() + ". Current progress '"
                        + kills_so_far + "/" + required + "'.");
                // Check the number of kills so far
                check();
            }
        }

        //
        // GROUP type Kill Listener
        //
        else if (type == KillType.GROUP) {
            // Require the entity to be a Player
            if (event.getEntityType() == EntityType.PLAYER)
            // Iterate through groups on the Player
            {
                for (String group : Depends.permissions.getPlayerGroups((Player) event.getEntity()))
                // If a group matches, count it!
                {
                    if (targets.contains(group.toUpperCase())) {
                        kills_so_far++;
                        dB.log(player.getName() + " killed " + ((Player) event.getEntity()).getName().toUpperCase() + " of group " + group + ".");
                        check();
                        break;
                    }
                }
            }
        }
    }


    public void check() {
        // Check current kills vs. required kills; finish() if necessary.
        if (kills_so_far >= required) {
            finish();
        }
    }


    @TagManager.TagEvents
    public void listenTag(ReplaceableTagEvent event) {

        if (!event.matches("LISTENER")) {
            return;
        }
        if (!event.getType().equalsIgnoreCase(id)) {
            return;
        }

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
            for (String curTar : targets) {
                targetList = targetList + curTar + ", ";
                targetList = targetList.substring(0, targetList.length() - 1);
            }
            event.setReplaced(targetList);
        }

        else if (event.getValue().equalsIgnoreCase("mobnames")) {
            String namesList = "";
            for (String curNam : names) {
                namesList = namesList + curNam + ", ";
                namesList = namesList.substring(0, namesList.length() - 1);
            }
            event.setReplaced(namesList);
        }
    }
}

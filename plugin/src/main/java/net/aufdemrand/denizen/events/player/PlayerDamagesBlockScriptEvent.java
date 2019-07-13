package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.debugging.dB;
import com.denizenscript.denizencore.objects.dList;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;

public class PlayerDamagesBlockScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player damages block
    // player damages <material>
    //
    // @Regex ^on player damages [^\s]+$
    // @Switch in <area>
    //
    // @Cancellable true
    //
    // @Triggers when a block is damaged by a player.
    //
    // @Context
    // <context.location> returns the dLocation the block that was damaged.
    // <context.material> returns the dMaterial of the block that was damaged.
    //
    // @Determine
    // "INSTABREAK" to make the block get broken instantly.
    //
    // -->

    public PlayerDamagesBlockScriptEvent() {
        instance = this;
    }

    public static PlayerDamagesBlockScriptEvent instance;
    public dLocation location;
    public dMaterial material;
    public Boolean instabreak;
    public BlockDamageEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String mat = CoreUtilities.getXthArg(2, lower);
        return lower.startsWith("player damages")
                && (mat.equals("block") || dMaterial.matches(mat));
    }

    @Override
    public boolean matches(ScriptPath path) {

        String mat = path.eventArgLowerAt(2);
        if (!tryMaterial(material, mat)) {
            return false;
        }

        if (!runInCheck(path, location)) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "PlayerDamagesBlock";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (CoreUtilities.toLowerCase(determination).equals("instabreak")) {
            instabreak = true;
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(dPlayer.mirrorBukkitPlayer(event.getPlayer()), null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("location")) {
            return location;
        }
        else if (name.equals("material")) {
            return material;
        }
        else if (name.equals("cuboids")) {
            dB.echoError("context.cuboids tag is deprecated in " + getName() + " script event");
            dList cuboids = new dList();
            for (dCuboid cuboid : dCuboid.getNotableCuboidsContaining(location)) {
                cuboids.add(cuboid.identifySimple());
            }
            return cuboids;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerDamagesBlock(BlockDamageEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        material = new dMaterial(event.getBlock());
        location = new dLocation(event.getBlock().getLocation());
        instabreak = event.getInstaBreak();
        this.event = event;
        fire(event);
        event.setInstaBreak(instabreak);
    }

}

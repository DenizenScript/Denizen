package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.debugging.dB;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.dList;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlayerPlacesBlockScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player places block
    // player places <material>
    //
    // @Regex ^on player places [^\s]+$
    //
    // @Switch in <area>
    // @Switch using HAND/OFF_HAND
    //
    // @Cancellable true
    //
    // @Triggers when a player places a block.
    //
    // @Context
    // <context.location> returns the dLocation of the block that was placed.
    // <context.material> returns the dMaterial of the block that was placed.
    // <context.old_material> returns the dMaterial of the block that was replaced.
    // <context.item_in_hand> returns the dItem of the item in hand.
    // <context.hand> returns the name of the hand that the block was in (HAND or OFF_HAND).
    //
    // -->

    public PlayerPlacesBlockScriptEvent() {
        instance = this;
    }

    public static PlayerPlacesBlockScriptEvent instance;
    public dLocation location;
    public dMaterial material;
    public Element hand;
    public dItem item_in_hand;
    public BlockPlaceEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String mat = CoreUtilities.getXthArg(2, lower);
        return lower.startsWith("player places")
                && (!mat.equals("hanging") && !mat.equals("painting") && !mat.equals("item_frame") && !mat.equals("leash_hitch"));
    }

    @Override
    public boolean matches(ScriptPath path) {

        String mat = path.eventArgLowerAt(2);
        if (!tryItem(item_in_hand, mat) && !tryMaterial(material, mat)) {
            return false;
        }

        if (!runGenericSwitchCheck(path, "using", hand.asString())) {
            return false;
        }

        if (!runInCheck(path, location)) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "PlayerPlacesBlock";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
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
        else if (name.equals("old_material")) {
            return new dMaterial(event.getBlockReplacedState());
        }
        else if (name.equals("item_in_hand")) {
            return item_in_hand;
        }
        else if (name.equals("hand")) {
            return hand;
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
    public void onPlayerPlacesBlock(BlockPlaceEvent event) {
        if (dEntity.isNPC(event.getPlayer())) {
            return;
        }
        hand = new Element(event.getHand().name());
        material = new dMaterial(event.getBlock());
        location = new dLocation(event.getBlock().getLocation());
        item_in_hand = new dItem(event.getItemInHand());
        this.event = event;
        fire(event);
    }

}

package net.aufdemrand.denizen.listeners.core;

import net.aufdemrand.denizen.listeners.AbstractListener;
import net.aufdemrand.denizen.objects.dCuboid;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.tags.ReplaceableTagEvent;
import net.aufdemrand.denizencore.tags.TagManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlockListenerInstance extends AbstractListener implements Listener {

    public static enum BlockType {BUILD, COLLECT, BREAK}

    //
    //The type of action
    //
    BlockType type = null;

    //
    //The blocks
    //
    dList blocks;

    //
    //The counters
    //
    Integer required = 1;
    Integer blocks_so_far = 0;

    //
    //Modifiers
    //
    String region = null;
    dCuboid cuboid = null;


    @Override
    public void onBuild(List<aH.Argument> args) {

        for (aH.Argument arg : args) {

            if (arg.matchesEnum(BlockType.values()) && type == null) {
                type = BlockType.valueOf(arg.getValue().toUpperCase());
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
            else if (arg.matchesPrefix("blocks, block, b, name, names")) {
                blocks = arg.asType(dList.class);
            }

        }

        if (blocks == null) {
            blocks = new dList("*");
        }

        if (type == null) {
            dB.echoError("Missing TYPE argument! Valid: BUILD, COLLECT, BREAK");
            cancel();
        }
    }

    @Override
    public void onSave() {
        store("Type", type.name());
        store("Blocks", blocks);
        store("Quantity", required);
        store("Current Blocks", blocks_so_far);
        store("Region", region);
        if (cuboid != null) {
            store("Cuboid", cuboid.identify());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onLoad() {
        type = BlockType.valueOf(((String) get("Type")));
        blocks = new dList((List<String>) get("Blocks"));
        required = (Integer) get("Quantity");
        blocks_so_far = (Integer) get("Current Blocks");
        region = (String) get("Region");
        cuboid = dCuboid.valueOf((String) get("Cuboid"));
    }

    @Override
    public void onFinish() {
    }

    @Override
    public void onCancel() {
    }

    @Override
    public String report() {
        return player.getName() + " currently has quest listener '" + id
                + "' active and must " + type.name() + " " + Arrays.toString(blocks.toArray())
                + "'(s). Current progress '" + blocks_so_far + "/" + required + "'.";
    }

    @Override
    public void constructed() {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
        TagManager.registerTagEvents(this);
    }

    @Override
    public void deconstructed() {
        BlockBreakEvent.getHandlerList().unregister(this);
        PlayerPickupItemEvent.getHandlerList().unregister(this);
        BlockPlaceEvent.getHandlerList().unregister(this);
        TagManager.unregisterTagEvents(this);
    }

    public void check() {
        if (blocks_so_far >= required) {
            finish();
        }
    }

    @EventHandler
    public void listenBreak(BlockBreakEvent event) {
        //Check if event references proper player.
        if (event.getPlayer() != player.getPlayerEntity()) {
            return;
        }

        //Check if region is specified, and if so, is the player in it.
        if (region != null) {
            //if (!WorldGuardUtilities.inRegion(player.getPlayerEntity().getLocation(), region)) return;
        }

        // Same with the CUBOID argument...
        if (cuboid != null) {
            if (!cuboid.isInsideCuboid(player.getLocation())) {
                return;
            }
        }

        //Type BREAK
        if (type == BlockType.BREAK) {
            dB.log("...BREAK listener");
            //if catch-all specified, count it!
            if (blocks.contains("*")) {
                blocks_so_far++;
                dB.log(ChatColor.YELLOW + "// " + player.getName()
                        + " broke a(n) " + event.getBlock().getType().toString()
                        + ".");
                check();
                return;
            }

            //check if block is specified and if so, count it!
            for (String item_value : blocks) {
                dB.log("...checking value: " + item_value);
                dMaterial mat = dMaterial.valueOf(item_value);

                if (event.getBlock().getState().getType() == mat.getMaterial() &&
                        event.getBlock().getState().getData().equals(mat.getMaterialData())) {
                    blocks_so_far++;
                    dB.log(ChatColor.YELLOW + "// " + player.getName()
                            + " broke a(n) " + event.getBlock().getType().toString()
                            + ".");
                    check();
                }
            }
        }
    }


    List<Integer> itemsCollected = new ArrayList<Integer>();

    @EventHandler
    public void listenCollect(PlayerPickupItemEvent event) {
        //Check if event references proper player.
        if (event.getPlayer() != player.getPlayerEntity()) {
            return;
        }

        //Check if region is specified, and if so, is the player in it.
        if (region != null) {
            //if (!WorldGuardUtilities.inRegion(player.getPlayerEntity().getLocation(), region)) return;
        }

        // Same with the CUBOID argument...
        if (cuboid != null) {
            if (!cuboid.isInsideCuboid(player.getLocation())) {
                return;
            }
        }

        //Type COLLECT
        if (type == BlockType.COLLECT) {

            //if catch-all specified, count it!
            if (blocks.contains("*")) {
                blocks_so_far++;
                dB.log(ChatColor.YELLOW + "// " + player.getName()
                        + " collected a(n) " + event.getItem().getItemStack().getType().toString()
                        + ".");
                check();
                return;
            }

            //check if block is specified and if so, count it!
            for (String item_value : blocks) {
                dMaterial mat = dMaterial.valueOf(item_value);

                if (event.getItem().getItemStack().getType() == mat.getMaterial() &&
                        event.getItem().getItemStack().getData().equals(mat.getMaterialData())) {
                    //If the specific item has been collected before, dont count it
                    if (itemsCollected.contains(event.getItem().getEntityId())) {
                        return;
                    }
                    else {
                        itemsCollected.add(event.getItem().getEntityId());
                    }

                    blocks_so_far++;
                    dB.log(ChatColor.YELLOW + "// " + player.getName()
                            + " collected a(n) " + event.getItem().getItemStack().getType().toString()
                            + ".");
                    check();
                }
            }
        }
    }

    @EventHandler
    public void listenBucket(PlayerBucketFillEvent event) {
        //Check if event references proper player.
        if (event.getPlayer() != player.getPlayerEntity()) {
            return;
        }

        //Check if region is specified, and if so, is the player in it.
        if (region != null) {
            //if (!WorldGuardUtilities.inRegion(player.getPlayerEntity().getLocation(), region)) return;
        }

        // Same with the CUBOID argument...
        if (cuboid != null) {
            if (!cuboid.isInsideCuboid(player.getLocation())) {
                return;
            }
        }

        //Type COLLECT
        if (type == BlockType.COLLECT) {
            //if catch-all specified, count it!
            if (blocks.contains("*")) {
                blocks_so_far++;
                dB.log(ChatColor.YELLOW + "// "
                        + player.getName() + " collected a "
                        + event.getBucket().name() + ".");
                check();
                return;
            }

            //check if block is specified and if so, count it!
            for (String item_value : blocks) {
                dMaterial mat = dMaterial.valueOf(item_value);

                if (event.getBucket() == mat.getMaterial()) {
                    blocks_so_far++;
                    dB.log(ChatColor.YELLOW + "// "
                            + player.getName() + " collected a "
                            + event.getBucket().name() + ".");

                    check();
                }
            }
        }
    }

    List<Location> blocksPlaced = new ArrayList<Location>();

    @EventHandler
    public void listenPlace(BlockPlaceEvent event) {
        //Check if event references proper player.
        if (event.getPlayer() != player.getPlayerEntity()) {
            return;
        }

        //Check if region is specified, and if so, is the player in it.
        if (region != null) {
            //if (!WorldGuardUtilities.inRegion(player.getPlayerEntity().getLocation(), region)) return;
        }

        // Same with the CUBOID argument...
        if (cuboid != null) {
            if (!cuboid.isInsideCuboid(player.getLocation())) {
                return;
            }
        }

        //Type BUILD
        if (type == BlockType.BUILD) {

            //if catch-all specified, count it!
            if (blocks.contains("*")) {
                blocks_so_far++;
                dB.log(ChatColor.YELLOW + "// " + player.getName()
                        + " placed a(n) " + event.getBlock().getType().toString()
                        + ".");
                check();
                return;
            }

            //check if block is specified and if so, count it!
            for (String item_value : blocks) {
                dMaterial mat = dMaterial.valueOf(item_value);

                if (event.getBlock().getState().getType() == mat.getMaterial() &&
                        event.getBlock().getState().getData().equals(mat.getMaterialData())) {
                    blocks_so_far++;
                    dB.log(ChatColor.YELLOW + "// " + player.getName()
                            + " placed a(n) " + event.getBlock().getType().toString()
                            + ".");
                    check();
                }
            }
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
            event.setReplaced(required.toString());
        }
        else if (event.getValue().equalsIgnoreCase("blocks_so_far")) {
            event.setReplaced(blocks_so_far.toString());
        }
        else if (event.getValue().equalsIgnoreCase("blocks")) {
            String blockList = "";
            for (String curTar : blocks) {
                blockList = blockList + curTar + ", ";
                blockList = blockList.substring(0, blockList.length() - 1);
            }
            event.setReplaced(blockList);
        }
    }
}

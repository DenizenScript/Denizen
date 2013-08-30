package net.aufdemrand.denizen.listeners.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.listeners.AbstractListener;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dCuboid;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.WorldGuardUtilities;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class BlockListenerInstance extends AbstractListener implements Listener {

    enum BlockType { BUILD, COLLECT, BREAK }
    
    //
    //The type of action
    //
    BlockType type;
    
    //
    //The blocks
    //
    List<String> blocks = new ArrayList<String>();
    
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
    		if (type == null && arg.matchesEnum(BlockType.values()))
                type = BlockType.valueOf(arg.getValue().toUpperCase());
    		
    		else if (arg.matchesPrefix("qty, q")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer))
                this.required = aH.getIntegerFrom(arg.getValue());

            else if (arg.matchesPrefix("region, r"))
                this.region = arg.getValue();

            else if (arg.matchesPrefix("cuboid, c")
                    && arg.matchesArgumentType(dCuboid.class))
                this.cuboid = arg.asType(dCuboid.class);
            
            else if (arg.matchesPrefix("blocks, block, b, name, names"))
                blocks = arg.asType(dList.class);
            
    		if (blocks == null)
                blocks = new dList("*");

            if (type == null) {
                dB.echoError("Missing TYPE argument! Valid: BUILD, COLLECT, BREAK");
                cancel();
            }
    	}
    }

    @Override
    public void onSave() {
        store("Type", type.name());
        store("Blocks", blocks);
        store("Quantity", required);
        store("Current Blocks", blocks_so_far);
        store("Region", region);
        if (cuboid != null) store("Cuboid", cuboid.identify());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onLoad() {
        type = BlockType.valueOf(((String) get("Type")));
        blocks = (List<String>) get("Blocks");
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
                + "' active and must " + type.name()+ " " + Arrays.toString(blocks.toArray())
                + "'(s). Current progress '" + blocks_so_far + "/" + required + "'.";
    }

    @Override
    public void constructed() {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    @Override
    public void deconstructed() {
        BlockBreakEvent.getHandlerList().unregister(this);
        PlayerPickupItemEvent.getHandlerList().unregister(this);
        BlockPlaceEvent.getHandlerList().unregister(this);
    }
    
    public void check() {
        if (blocks_so_far >= required) {
            finish();
        }
    }

    @EventHandler
    public void listenBreak(BlockBreakEvent event) {
    	//Check if event references proper player.
    	if (event.getPlayer() != player.getPlayerEntity()) return;
        
    	//Check if region is specified, and if so, is the player in it.
        if (region != null)
            if (!WorldGuardUtilities.inRegion(player.getPlayerEntity().getLocation(), region)) return;
        
        //Type BREAK
        if (type == BlockType.BREAK) {
        	//If the block matches, count it!!
            if (blocks.contains(event.getBlock().getType().toString())
                || blocks.contains(String.valueOf(event.getBlock().getTypeId()))
                || blocks.contains("*")) {
                    blocks_so_far++;
                    dB.echoDebug(ChatColor.YELLOW + "// " + player.getName() + " broke a " + event.getBlock().getType().toString() + ".");
                    check();
            }
        }
    }


    List<Integer> itemsCollected = new ArrayList<Integer>();
    @EventHandler
    public void listenCollect(PlayerPickupItemEvent event) {
    	//Check if event references proper player.
    	if (event.getPlayer() != player.getPlayerEntity()) return;

    	//Check if region is specified, and if so, is the player in it.
        if (region != null)
            if (!WorldGuardUtilities.inRegion(player.getPlayerEntity().getLocation(), region)) return;
        
        //Type COLLECT    
        if (type == BlockType.COLLECT) {
        	//If the block matches, count it!!
            if (blocks.contains(event.getItem().getItemStack().getType().toString())
                || blocks.contains(String.valueOf(event.getItem().getItemStack().getTypeId()))
                || blocks.contains("*")) {
            		//If the specific item has been collected before, dont count it
                    if (itemsCollected.contains(event.getItem().getEntityId()))
                        return;
                    else itemsCollected.add(event.getItem().getEntityId());
                    
                    blocks_so_far = blocks_so_far + event.getItem().getItemStack().getAmount();
                    dB.echoDebug(ChatColor.YELLOW + "// " + player.getName() + " collected a " + event.getItem().getItemStack().getType().toString() + ".");
                    check();
            }
        }
    }
    
    @EventHandler
    public void listenBucket(PlayerBucketFillEvent event) {
    	//Check if event references proper player.
        if (event.getPlayer() != player.getPlayerEntity()) return;

    	//Check if region is specified, and if so, is the player in it.
        if (region != null)
            if (!WorldGuardUtilities.inRegion(player.getPlayerEntity().getLocation(), region)) return;
        
        //Type COLLECT
        if (type == BlockType.COLLECT) {
        	//If the block matches, count it!!
            if (blocks.contains(event.getBucket().name().toUpperCase())
                || blocks.contains(String.valueOf(event.getBucket().name().toUpperCase()))
                || blocks.contains("*")) {
                	blocks_so_far++;
                    dB.echoDebug(ChatColor.YELLOW + "// " + player.getName() + " collected a " + event.getBucket().name() + ".");
                    check();
            }
        }
    }

    List<Location> blocksPlaced = new ArrayList<Location>();
    @EventHandler
    public void listenPlace(BlockPlaceEvent event) {
    	//Check if event references proper player.
        if (event.getPlayer() != player.getPlayerEntity()) return;

    	//Check if region is specified, and if so, is the player in it.
        if (region != null)
            if (!WorldGuardUtilities.inRegion(player.getPlayerEntity().getLocation(), region)) return;
        
        //Type BUILD
        if (type == BlockType.BUILD) {
        	//If the block matches, count it!!
            if (blocks.contains(event.getBlock().getType().toString())
                || blocks.contains(String.valueOf(event.getBlock().getTypeId()))
                || blocks.contains("*")) {
            		//If a block has already been placed at that location, dont count it.
                    if (blocksPlaced.contains(event.getBlock().getLocation()))
                        return;
                    else blocksPlaced.add(event.getBlock().getLocation());
                    
                    blocks_so_far++;
                    dB.echoDebug(ChatColor.YELLOW + "// " + player.getName() + " placed a " + event.getBlock().getType().toString() + ".");
                    check();
            }
        }
    }
    
    @EventHandler
    public void listenTag(ReplaceableTagEvent event) {
        
        if (!event.matches("LISTENER")) return;
        if (!event.getType().equalsIgnoreCase(id)) return;
        
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
            for (String curTar : blocks){
                blockList = blockList + curTar + ", ";
                blockList = blockList.substring(0, blockList.length() - 1);
            }
            event.setReplaced(blockList);
        }
    }

}

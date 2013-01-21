package net.aufdemrand.denizen.utilities;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

public class DenizenPlayer {

    public static void chat(Player player, NPC npc, String message) {


    }

    public static NPC getTargetNPC(Player player, int range) {
        BlockIterator iterator = new BlockIterator(player.getWorld(), player
                .getLocation().toVector(), player.getEyeLocation()
                .getDirection(), 0, range);
        NPC target = null;
        while (iterator.hasNext()) {
            Block item = iterator.next();
            for (Entity entity : player.getNearbyEntities(range, range, range)) {
                int acc = 2;
                for (int x = -acc; x < acc; x++)
                    for (int z = -acc; z < acc; z++)
                        for (int y = -acc; y < acc; y++)
                            if (entity.getLocation().getBlock()
                                    .getRelative(x, y, z).equals(item)) {
                                if (CitizensAPI.getNPCRegistry().isNPC(entity))
                                return target = CitizensAPI.getNPCRegistry().getNPC(entity);
                            }
            }
        }
        return target;
    }



}

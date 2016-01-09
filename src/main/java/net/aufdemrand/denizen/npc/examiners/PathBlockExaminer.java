package net.aufdemrand.denizen.npc.examiners;

import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.objects.dNPC;
import net.citizensnpcs.api.astar.pathfinder.BlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.BlockSource;
import net.citizensnpcs.api.astar.pathfinder.PathPoint;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.util.*;


public class PathBlockExaminer implements BlockExaminer {

    // Default pathing materials
    static final List<dMaterial> default_pathing_materials =
            Arrays.asList(dMaterial.valueOf("gravel"), dMaterial.valueOf("dirt"),
                    dMaterial.valueOf("cobblestone"));

    List<dMaterial> pathing_materials = new ArrayList<dMaterial>(default_pathing_materials);

    // Unfortunately for this to work properly, it needs
    // to have a NPC attached to the Examiner.
    public PathBlockExaminer(dNPC npc, List<dMaterial> filter) {
        if (filter != null) {
            pathing_materials = filter;
        }

        this.npc = npc;
    }

    dNPC npc;

    @Override
    public float getCost(BlockSource source, PathPoint point) {

        // TODO: Understand cost weight :D

        Vector pos = point.getVector();
        Material above = source.getMaterialAt(pos.clone().add(UP));
        Material below = source.getMaterialAt(pos.clone().add(DOWN));
        Material in = source.getMaterialAt(pos);

        // Discourage walking up a Z level
        if (point.getVector().getBlockY() > npc.getEntity().getLocation().getBlockY()) {
            return 5f;
        }

        // Encourage materials that are not in the filter
        for (dMaterial mat : pathing_materials) {
            // Discourage walking through web
            if (above == Material.WEB || in == Material.WEB) {
                return 1F;
            }

            // Discourage liquid
            if (isLiquid(above, below, in)) {
                return 1F;
            }

            // Encourage pathing materials
            if (below == mat.getMaterial()) {
                return 0F;
            }
        }

        return .5f;
    }

    @Override
    public PassableState isPassable(BlockSource source, PathPoint point) {
        Vector pos = point.getVector();
        Material above = source.getMaterialAt(pos.clone().add(UP));
        Material below = source.getMaterialAt(pos.clone().add(DOWN));
        Material in = source.getMaterialAt(pos);
        if (!below.isBlock() || !canStandOn(below)) {
            return PassableState.UNPASSABLE;
        }
        if (!canStandIn(above) || !canStandIn(in)) {
            return PassableState.UNPASSABLE;
        }
        return PassableState.PASSABLE;
    }

    public static boolean canStandIn(Material... mat) {
        return PASSABLE.containsAll(Arrays.asList(mat));
    }

    public static boolean canStandOn(Block block) {
        Block up = block.getRelative(BlockFace.UP);
        return canStandOn(block.getType()) && canStandIn(up.getType())
                && canStandIn(up.getRelative(BlockFace.UP).getType());
    }

    public static boolean canStandOn(Material mat) {
        return !UNWALKABLE.contains(mat) && !PASSABLE.contains(mat);
    }

    private static boolean contains(Material[] search, Material... find) {
        for (Material haystack : search) {
            for (Material needle : find) {
                if (haystack == needle) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isLiquid(Material... materials) {
        return contains(materials, Material.WATER, Material.STATIONARY_WATER, Material.LAVA, Material.STATIONARY_LAVA);
    }

    private static final Vector DOWN = new Vector(0, -1, 0);
    private static final Set<Material> PASSABLE = EnumSet.of(Material.AIR, Material.DEAD_BUSH, Material.DETECTOR_RAIL,
            Material.DIODE, Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.FENCE_GATE,
            Material.ITEM_FRAME, Material.LADDER, Material.LEVER, Material.LONG_GRASS, Material.CARPET,
            Material.MELON_STEM, Material.NETHER_FENCE, Material.PUMPKIN_STEM, Material.POWERED_RAIL, Material.RAILS,
            Material.RED_ROSE, Material.RED_MUSHROOM, Material.REDSTONE, Material.REDSTONE_TORCH_OFF,
            Material.REDSTONE_TORCH_OFF, Material.REDSTONE_WIRE, Material.SIGN, Material.SIGN_POST, Material.SNOW,
            Material.STRING, Material.STONE_BUTTON, Material.SUGAR_CANE_BLOCK, Material.TRIPWIRE, Material.VINE,
            Material.WALL_SIGN, Material.WHEAT, Material.WATER, Material.WEB, Material.WOOD_BUTTON,
            Material.WOODEN_DOOR, Material.STATIONARY_WATER);
    private static final Set<Material> UNWALKABLE = EnumSet.of(Material.AIR, Material.LAVA, Material.STATIONARY_LAVA,
            Material.CACTUS);
    private static final Vector UP = new Vector(0, 1, 0);
}



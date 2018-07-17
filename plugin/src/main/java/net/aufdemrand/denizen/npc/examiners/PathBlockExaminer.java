package net.aufdemrand.denizen.npc.examiners;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.NMSVersion;
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
            if (above == COBWEB || in == COBWEB) {
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
        return contains(materials, combine(WATER, LAVA));
    }

    // TODO: 1.13 - ...everything below.
    private static final Material COBWEB;
    private static final Material[] WATER;
    private static final Material[] LAVA;
    private static final Material[] REPEATER;
    private static final Material[] FENCE_GATE;
    private static final Material TALL_GRASS;
    private static final Material[] REDSTONE_TORCH;
    private static final Material[] CARPET;
    private static final Material ROSE_RED;
    private static final Material RAIL;
    private static final Material[] WOOD_BUTTON;
    private static final Material[] WOODEN_DOOR;
    private static final Material SUGAR_CANE;
    private static final Material[] SIGN;

    private static Material[] combine(Material[] first, Material... second) {
        int firstLen = first.length;
        int secondLen = second.length;
        Material[] ret = new Material[firstLen + secondLen];
        System.arraycopy(first, 0, ret, 0, firstLen);
        System.arraycopy(second, 0, ret, firstLen, secondLen);
        return ret;
    }

    static {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R1)) {
            COBWEB = Material.COBWEB;
            WATER = new Material[] { Material.WATER };
            LAVA = new Material[] { Material.LAVA };
            REPEATER = new Material[] { Material.REPEATER };
            FENCE_GATE = new Material[] { Material.ACACIA_FENCE_GATE, Material.BIRCH_FENCE_GATE,
                    Material.DARK_OAK_FENCE_GATE, Material.JUNGLE_FENCE_GATE,
                    Material.OAK_FENCE_GATE, Material.SPRUCE_FENCE_GATE };
            TALL_GRASS = Material.TALL_GRASS;
            REDSTONE_TORCH = new Material[] { Material.REDSTONE_TORCH };
            CARPET = new Material[] { Material.BLACK_CARPET, Material.BLUE_CARPET, Material.BROWN_CARPET,
                    Material.CYAN_CARPET, Material.GRAY_CARPET, Material.GREEN_CARPET, Material.LIME_CARPET,
                    Material.LIGHT_BLUE_CARPET, Material.LIGHT_GRAY_CARPET, Material.MAGENTA_CARPET,
                    Material.ORANGE_CARPET, Material.PINK_CARPET, Material.PURPLE_CARPET, Material.RED_CARPET,
                    Material.WHITE_CARPET, Material.YELLOW_CARPET };
            ROSE_RED = Material.ROSE_RED;
            RAIL = Material.RAIL;
            WOOD_BUTTON = new Material[] { Material.ACACIA_BUTTON, Material.BIRCH_BUTTON,
                    Material.DARK_OAK_BUTTON, Material.JUNGLE_BUTTON,
                    Material.OAK_BUTTON, Material.SPRUCE_BUTTON };
            WOODEN_DOOR = new Material[] { Material.ACACIA_DOOR, Material.BIRCH_DOOR,
                    Material.DARK_OAK_DOOR, Material.JUNGLE_DOOR,
                    Material.OAK_DOOR, Material.SPRUCE_DOOR };
            SUGAR_CANE = Material.SUGAR_CANE;
            SIGN = new Material[] { Material.SIGN, Material.WALL_SIGN };
        }
        else {
            COBWEB = Material.valueOf("WEB");
            WATER = new Material[] { Material.valueOf("WATER"), Material.valueOf("STATIONARY_WATER") };
            LAVA = new Material[] { Material.valueOf("LAVA"), Material.valueOf("STATIONARY_LAVA") };
            REPEATER = new Material[] { Material.valueOf("DIODE"), Material.valueOf("DIODE_BLOCK_OFF"), Material.valueOf("DIODE_BLOCK_ON") };
            FENCE_GATE = new Material[] { Material.valueOf("FENCE_GATE") };
            TALL_GRASS = Material.valueOf("LONG_GRASS");
            REDSTONE_TORCH = new Material[] { Material.valueOf("REDSTONE_TORCH_ON"), Material.valueOf("REDSTONE_TORCH_OFF") };
            CARPET = new Material[] { Material.valueOf("CARPET") };
            ROSE_RED = Material.valueOf("RED_ROSE");
            RAIL = Material.valueOf("RAILS");
            WOOD_BUTTON = new Material[] { Material.valueOf("WOOD_BUTTON") };
            WOODEN_DOOR = new Material[] { Material.valueOf("WOODEN_DOOR") };
            SUGAR_CANE = Material.valueOf("SUGAR_CANE_BLOCK");
            SIGN = new Material[] { Material.valueOf("SIGN_POST"), Material.valueOf("WALL_SIGN") };
        }
    }

    private static final Vector DOWN = new Vector(0, -1, 0);
    private static final Set<Material> PASSABLE = EnumSet.of(Material.AIR, combine(REPEATER, combine(FENCE_GATE,
            combine(WATER, combine(CARPET, combine(WOOD_BUTTON, combine(REDSTONE_TORCH, combine(WOODEN_DOOR, combine(SIGN,
                    COBWEB, TALL_GRASS, ROSE_RED, RAIL, SUGAR_CANE, Material.DEAD_BUSH, Material.DETECTOR_RAIL,
                    Material.ITEM_FRAME, Material.LADDER, Material.LEVER, Material.MELON_STEM,
                    Material.PUMPKIN_STEM, Material.POWERED_RAIL, Material.RED_MUSHROOM, Material.REDSTONE,
                    Material.REDSTONE_WIRE, Material.SNOW, Material.STRING, Material.STONE_BUTTON,
                    Material.TRIPWIRE, Material.VINE, Material.WHEAT)))))))));
    private static final Set<Material> UNWALKABLE = EnumSet.of(Material.AIR, combine(LAVA, Material.CACTUS));
    private static final Vector UP = new Vector(0, 1, 0);
}



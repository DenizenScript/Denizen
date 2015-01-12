package net.aufdemrand.denizen.utilities.blocks;

import org.bukkit.Material;

public class SafeBlock {
    // Quick util function to decide whether
    // A block is 'safe' (Can be spawned inside of) - air, tallgrass, etc.
    // Credit to Mythan for compiling the initial list
    public static boolean blockIsSafe(Material mat) {
        switch (mat) {
            case LEVER:
            case WOOD_BUTTON:
            case STONE_BUTTON:
            case REDSTONE_WIRE:
            case SAPLING:
            case SIGN_POST:
            case WALL_SIGN:
            case SNOW:
            case TORCH:
            case DETECTOR_RAIL:
            case ACTIVATOR_RAIL:
            case RAILS:
            case POWERED_RAIL:
            case NETHER_WARTS:
            case NETHER_STALK:
            case VINE:
            case SUGAR_CANE_BLOCK:
            case CROPS:
            case LONG_GRASS:
            case RED_MUSHROOM:
            case BROWN_MUSHROOM:
            case DEAD_BUSH:
            case REDSTONE_TORCH_OFF:
            case REDSTONE_TORCH_ON:
            case AIR:
            case YELLOW_FLOWER:
            case RED_ROSE:
                return true;
            default:
                return false;
        }
    }
}

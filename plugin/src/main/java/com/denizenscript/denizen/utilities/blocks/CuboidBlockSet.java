package com.denizenscript.denizen.utilities.blocks;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.interfaces.BlockData;
import com.denizenscript.denizen.objects.CuboidTag;
import com.denizenscript.denizen.scripts.commands.world.SchematicCommand;
import com.denizenscript.denizen.utilities.DenizenAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

public class CuboidBlockSet implements BlockSet {

    public CuboidBlockSet() {
    }

    public CuboidBlockSet(CuboidTag cuboid, Location center) {
        Location low = cuboid.pairs.get(0).low;
        Location high = cuboid.pairs.get(0).high;
        x_width = (int) ((high.getX() - low.getX()) + 1);
        y_length = (int) ((high.getY() - low.getY()) + 1);
        z_height = (int) ((high.getZ() - low.getZ()) + 1);
        center_x = (int) (center.getX() - low.getX());
        center_y = (int) (center.getY() - low.getY());
        center_z = (int) (center.getZ() - low.getZ());
        blocks = new BlockData[(int) (x_width * y_length * z_height)];
        int index = 0;
        for (int x = 0; x < x_width; x++) {
            for (int y = 0; y < y_length; y++) {
                for (int z = 0; z < z_height; z++) {
                    blocks[index++] = NMSHandler.getBlockHelper().getBlockData(low.clone().add(x, y, z).getBlock());
                }
            }
        }
    }

    public void buildDelayed(CuboidTag cuboid, Location center, Runnable runme) {
        Location low = cuboid.pairs.get(0).low;
        Location high = cuboid.pairs.get(0).high;
        x_width = (int) ((high.getX() - low.getX()) + 1);
        y_length = (int) ((high.getY() - low.getY()) + 1);
        z_height = (int) ((high.getZ() - low.getZ()) + 1);
        center_x = (int) (center.getX() - low.getX());
        center_y = (int) (center.getY() - low.getY());
        center_z = (int) (center.getZ() - low.getZ());
        final long goal = (long) (x_width * y_length * z_height);
        new BukkitRunnable() {
            int index = 0;
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                while (index < goal) {
                    long z = index % ((long) (z_height));
                    long y = ((index - z) % ((long) (y_length * z_height))) / ((long) z_height);
                    long x = (index - y - z) / ((long) (y_length * z_height));
                    blocks[index] = NMSHandler.getBlockHelper().getBlockData(low.clone().add(x, y, z).getBlock());
                    index++;
                    if (System.currentTimeMillis() - start > 50) {
                        SchematicCommand.noPhys = false;
                        return;
                    }
                }
                if (runme != null) {
                    runme.run();
                }
                cancel();

            }
        }.runTaskTimer(DenizenAPI.getCurrentInstance(), 1, 1);
    }

    public BlockData[] blocks = null;

    public int x_width;

    public int y_length;

    public int z_height;

    public int center_x;

    public int center_y;

    public int center_z;

    @Override
    public BlockData[] getBlocks() {
        return blocks;
    }

    public CuboidTag getCuboid(Location loc) {
        Location low = loc.clone().subtract(center_x, center_y, center_z);
        Location high = low.clone().add(x_width, y_length, z_height);
        return new CuboidTag(low, high);
    }

    public void setBlockSingle(BlockData block, int x, int y, int z, InputParams input) {
        if (!input.noAir || block.getMaterial() != Material.AIR) {
            block.setBlock(input.centerLocation.clone().add(x - center_x, y - center_y, z - center_z).getBlock(), false);
        }
    }

    @Override
    public void setBlocksDelayed(final Runnable runme, final InputParams input) {
        final long goal = (long) (x_width * y_length * z_height);
        new BukkitRunnable() {
            int index = 0;
            @Override
            public void run() {
                SchematicCommand.noPhys = true;
                long start = System.currentTimeMillis();
                while (index < goal) {
                    int z = index % (z_height);
                    int y = ((index - z) % (y_length * z_height)) / z_height;
                    int x = (index - y - z) / (y_length * z_height);
                    setBlockSingle(blocks[index], x, y, z, input);
                    index++;
                    if (System.currentTimeMillis() - start > 50) {
                        SchematicCommand.noPhys = false;
                        return;
                    }
                }
                SchematicCommand.noPhys = false;
                if (runme != null) {
                    runme.run();
                }
                cancel();

            }
        }.runTaskTimer(DenizenAPI.getCurrentInstance(), 1, 1);
    }

    @Override
    public void setBlocks(InputParams input) {
        SchematicCommand.noPhys = true;
        int index = 0;
        for (int x = 0; x < x_width; x++) {
            for (int y = 0; y < y_length; y++) {
                for (int z = 0; z < z_height; z++) {
                    setBlockSingle(blocks[index], x, y, z, input);
                    index++;
                }
            }
        }
        SchematicCommand.noPhys = false;
    }

    public void rotateOne() {
        BlockData[] bd = new BlockData[blocks.length];
        int index = 0;
        int cx = center_x;
        center_x = center_z;
        center_z = cx;
        for (int x = 0; x < z_height; x++) {
            for (int y = 0; y < y_length; y++) {
                for (int z = (int) x_width - 1; z >= 0; z--) {
                    bd[index++] = blockAt(z, y, x);
                }
            }
        }
        int xw = x_width;
        x_width = z_height;
        z_height = xw;
        blocks = bd;
    }

    public void flipX() {
        BlockData[] bd = new BlockData[blocks.length];
        int index = 0;
        center_x = x_width - center_x;
        for (int x = (int) x_width - 1; x >= 0; x--) {
            for (int y = 0; y < y_length; y++) {
                for (int z = 0; z < z_height; z++) {
                    bd[index++] = blockAt(x, y, z);
                }
            }
        }
        blocks = bd;
    }

    public void flipY() {
        BlockData[] bd = new BlockData[blocks.length];
        int index = 0;
        center_x = x_width - center_x;
        for (int x = 0; x < x_width; x++) {
            for (int y = (int) y_length - 1; y >= 0; y--) {
                for (int z = 0; z < z_height; z++) {
                    bd[index++] = blockAt(x, y, z);
                }
            }
        }
        blocks = bd;
    }

    public void flipZ() {
        BlockData[] bd = new BlockData[blocks.length];
        int index = 0;
        center_x = x_width - center_x;
        for (int x = 0; x < x_width; x++) {
            for (int y = 0; y < y_length; y++) {
                for (int z = (int) z_height - 1; z >= 0; z--) {
                    bd[index++] = blockAt(x, y, z);
                }
            }
        }
        blocks = bd;
    }

    public BlockData blockAt(double X, double Y, double Z) {
        return blocks[(int) (Z + Y * z_height + X * z_height * y_length)];
        // This calculation should produce the same result as the below nonsense:
        /*
        int index = 0;
        for (int x = 0; x < x_width; x++) {
            for (int y = 0; y < y_length; y++) {
                for (int z = 0; z < z_height; z++) {
                    if (x == X && y == Y && z == Z) {
                        return blocks.get(index);
                    }
                    index++;
                }
            }
        }
        return null;
        */
    }
}

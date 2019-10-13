package com.denizenscript.denizen.utilities.blocks;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.interfaces.BlockData;
import com.denizenscript.denizen.objects.CuboidTag;
import com.denizenscript.denizen.scripts.commands.world.SchematicCommand;
import com.denizenscript.denizen.utilities.DenizenAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class CuboidBlockSet implements BlockSet {

    public CuboidBlockSet() {
    }

    public CuboidBlockSet(CuboidTag cuboid, Location center) {
        Location low = cuboid.pairs.get(0).low;
        Location high = cuboid.pairs.get(0).high;
        x_width = (high.getX() - low.getX()) + 1;
        y_length = (high.getY() - low.getY()) + 1;
        z_height = (high.getZ() - low.getZ()) + 1;
        center_x = center.getX() - low.getX();
        center_y = center.getY() - low.getY();
        center_z = center.getZ() - low.getZ();
        for (int x = 0; x < x_width; x++) {
            for (int y = 0; y < y_length; y++) {
                for (int z = 0; z < z_height; z++) {
                    blocks.add(NMSHandler.getBlockHelper().getBlockData(low.clone().add(x, y, z).getBlock()));
                }
            }
        }
    }

    public List<BlockData> blocks = new ArrayList<>();

    public double x_width;

    public double y_length;

    public double z_height;

    public double center_x;

    public double center_y;

    public double center_z;

    @Override
    public List<BlockData> getBlocks() {
        return blocks;
    }

    public CuboidTag getCuboid(Location loc) {
        Location low = loc.clone().subtract(center_x, center_y, center_z);
        Location high = low.clone().add(x_width, y_length, z_height);
        return new CuboidTag(low, high);
    }

    public class IntHolder {
        public long theInt = 0;
    }

    @Override
    public void setBlocksDelayed(final Location loc, final Runnable runme, final boolean noAir) {
        final IntHolder index = new IntHolder();
        final long goal = (long) (x_width * y_length * z_height);
        new BukkitRunnable() {
            @Override
            public void run() {
                SchematicCommand.noPhys = true;
                long start = System.currentTimeMillis();
                while (index.theInt < goal) {
                    long z = index.theInt % ((long) (z_height));
                    long y = ((index.theInt - z) % ((long) (y_length * z_height))) / ((long) z_height);
                    long x = (index.theInt - y - z) / ((long) (y_length * z_height));
                    if (!noAir || blocks.get((int) index.theInt).getMaterial() != Material.AIR) {
                        blocks.get((int) index.theInt).setBlock(loc.clone().add(x - center_x, y - center_y, z - center_z).getBlock(), false);
                    }
                    index.theInt++;
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
    public void setBlocks(Location loc, boolean noAir) {
        SchematicCommand.noPhys = true;
        int index = 0;
        for (int x = 0; x < x_width; x++) {
            for (int y = 0; y < y_length; y++) {
                for (int z = 0; z < z_height; z++) {
                    if (!noAir || blocks.get(index).getMaterial() != Material.AIR) {
                        blocks.get(index).setBlock(loc.clone().add(x - center_x, y - center_y, z - center_z).getBlock(), false);
                    }
                    index++;
                }
            }
        }
        SchematicCommand.noPhys = false;
    }

    public void rotateOne() {
        List<BlockData> bd = new ArrayList<>();
        double cx = center_x;
        center_x = center_z;
        center_z = cx;
        for (int x = 0; x < z_height; x++) {
            for (int y = 0; y < y_length; y++) {
                for (int z = (int) x_width - 1; z >= 0; z--) {
                    bd.add(blockAt(z, y, x));
                }
            }
        }
        double xw = x_width;
        x_width = z_height;
        z_height = xw;
        blocks = bd;
    }

    public void flipX() {
        List<BlockData> bd = new ArrayList<>();
        center_x = x_width - center_x;
        for (int x = (int) x_width - 1; x >= 0; x--) {
            for (int y = 0; y < y_length; y++) {
                for (int z = 0; z < z_height; z++) {
                    bd.add(blockAt(x, y, z));
                }
            }
        }
        blocks = bd;
    }

    public void flipY() {
        List<BlockData> bd = new ArrayList<>();
        center_x = x_width - center_x;
        for (int x = 0; x < x_width; x++) {
            for (int y = (int) y_length - 1; y >= 0; y--) {
                for (int z = 0; z < z_height; z++) {
                    bd.add(blockAt(x, y, z));
                }
            }
        }
        blocks = bd;
    }

    public void flipZ() {
        List<BlockData> bd = new ArrayList<>();
        center_x = x_width - center_x;
        for (int x = 0; x < x_width; x++) {
            for (int y = 0; y < y_length; y++) {
                for (int z = (int) z_height - 1; z >= 0; z--) {
                    bd.add(blockAt(x, y, z));
                }
            }
        }
        blocks = bd;
    }

    public BlockData blockAt(double X, double Y, double Z) {
        return blocks.get((int) (Z + Y * z_height + X * z_height * y_length));
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

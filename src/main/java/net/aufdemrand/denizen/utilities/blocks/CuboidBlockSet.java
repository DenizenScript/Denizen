package net.aufdemrand.denizen.utilities.blocks;

import net.aufdemrand.denizen.objects.dCuboid;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class CuboidBlockSet implements BlockSet {

    public CuboidBlockSet() {
    }

    public CuboidBlockSet(dCuboid cuboid, Location center) {
        Location low = cuboid.pairs.get(0).low;
        Location high = cuboid.pairs.get(0).high;
        cuboid = new dCuboid(low, high);
        List<dLocation> locs = cuboid.getBlocks_internal(null);
        x_width = high.getX() - low.getX();
        y_length = high.getY() - low.getY();
        z_height = high.getZ() - low.getZ();
        center_x = center.getX() - low.getX();
        center_y = center.getY() - low.getY();
        center_z = center.getZ() - low.getZ();
        for (int i = 0; i < locs.size(); i++) {
            blocks.add(new BlockData(locs.get(i).getBlock()));
        }
    }

    public List<BlockData> blocks = new ArrayList<BlockData>();

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

    public dCuboid getCuboid(Location loc) {
        Location low = loc.clone().subtract(center_x, center_y, center_z);
        Location high = low.clone().add(x_width, y_length, z_height);
        return new dCuboid(low, high);
    }

    @Override
    public void setBlocks(Location loc) {
        getCuboid(loc).setBlocks_internal(blocks);
    }

    @Override
    public String toCompressedFormat() {
        StringBuilder sb = new StringBuilder(blocks.size() * 20);
        sb.append(x_width).append(':').append(y_length).append(':').append(z_height).append(':');
        sb.append(center_x).append(':').append(center_y).append(':').append(center_z).append('\n');
        for (BlockData block: blocks) {
            sb.append(block.toCompressedFormat()).append('\n');
        }
        return sb.toString();
    }

    public BlockData blockAt(double x, double y, double z) {
        return getCuboid(new Location(null, 0, 0, 0)).getBlockAt(x, y, z, blocks);
    }

    public static CuboidBlockSet fromCompressedString(String str) {
        CuboidBlockSet cbs = new CuboidBlockSet();
        List<String> split = CoreUtilities.split(str, '\n');
        List<String> details = CoreUtilities.split(split.get(0), ':');
        cbs.x_width = Double.parseDouble(details.get(0));
        cbs.y_length = Double.parseDouble(details.get(1));
        cbs.z_height = Double.parseDouble(details.get(2));
        cbs.center_x = Double.parseDouble(details.get(3));
        cbs.center_y = Double.parseDouble(details.get(4));
        cbs.center_z = Double.parseDouble(details.get(5));
        split.remove(0);
        for (String read: split) {
            if (read.length() > 0) {
                cbs.blocks.add(BlockData.fromCompressedString(read));
            }
        }
        return cbs;
    }
}

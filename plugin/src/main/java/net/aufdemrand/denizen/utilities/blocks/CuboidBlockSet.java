package net.aufdemrand.denizen.utilities.blocks;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.interfaces.BlockData;
import net.aufdemrand.denizen.nms.util.jnbt.*;
import net.aufdemrand.denizen.objects.dCuboid;
import net.aufdemrand.denizen.scripts.commands.world.SchematicCommand;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CuboidBlockSet implements BlockSet {

    public CuboidBlockSet() {
    }

    public CuboidBlockSet(dCuboid cuboid, Location center) {
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
                    blocks.add(NMSHandler.getInstance().getBlockHelper().getBlockData(low.clone().add(x, y, z).getBlock()));
                }
            }
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
                    if (!noAir || blocks.get((int)index.theInt).getMaterial() != Material.AIR) {
                        blocks.get((int) index.theInt).setBlock(loc.clone().add(x - center_x, y - center_y, z - center_z).getBlock());
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
                        blocks.get(index).setBlock(loc.clone().add(x - center_x, y - center_y, z - center_z).getBlock());
                    }
                    index++;
                }
            }
        }
        SchematicCommand.noPhys = false;
    }

    public void rotateOne() {
        List<BlockData> bd = new ArrayList<BlockData>();
        double cx = center_x;
        center_x = center_z;
        center_z = cx;
        for (int x = 0; x < z_height; x++) {
            for (int y = 0; y < y_length; y++) {
                for (int z = (int)x_width - 1; z >= 0; z--) {
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
        List<BlockData> bd = new ArrayList<BlockData>();
        center_x = x_width - center_x;
        for (int x = (int)x_width - 1; x >= 0; x--) {
            for (int y = 0; y < y_length; y++) {
                for (int z = 0; z < z_height; z++) {
                    bd.add(blockAt(x, y, z));
                }
            }
        }
        blocks = bd;
    }

    public void flipY() {
        List<BlockData> bd = new ArrayList<BlockData>();
        center_x = x_width - center_x;
        for (int x = 0; x < x_width; x++) {
            for (int y = (int)y_length - 1; y >= 0; y--) {
                for (int z = 0; z < z_height; z++) {
                    bd.add(blockAt(x, y, z));
                }
            }
        }
        blocks = bd;
    }

    public void flipZ() {
        List<BlockData> bd = new ArrayList<BlockData>();
        center_x = x_width - center_x;
        for (int x = 0; x < x_width; x++) {
            for (int y = 0; y < y_length; y++) {
                for (int z = (int)z_height - 1; z >= 0; z--) {
                    bd.add(blockAt(x, y, z));
                }
            }
        }
        blocks = bd;
    }

    public BlockData blockAt(double X, double Y, double Z) {
        return blocks.get((int)(Z + Y * z_height + X * z_height * y_length));
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

    public static CuboidBlockSet fromMCEditStream(InputStream is) {
        CuboidBlockSet cbs = new CuboidBlockSet();
        try {
            NBTInputStream nbtStream = new NBTInputStream(new GZIPInputStream(is));
            NamedTag rootTag = nbtStream.readNamedTag();
            nbtStream.close();
            if (!rootTag.getName().equals("Schematic")) {
                throw new Exception("Tag 'Schematic' does not exist or is not first!");
            }
            CompoundTag schematicTag = (CompoundTag) rootTag.getTag();
            Map<String, Tag> schematic = schematicTag.getValue();
            short width = (getChildTag(schematic, "Width", ShortTag.class).getValue());
            short length = (getChildTag(schematic, "Length", ShortTag.class).getValue());
            short height = (getChildTag(schematic, "Height", ShortTag.class).getValue());
            int originX = 0;
            int originY = 0;
            int originZ = 0;
            try {
                originX = getChildTag(schematic, "DenizenOriginX", IntTag.class).getValue();
                originY = getChildTag(schematic, "DenizenOriginY", IntTag.class).getValue();
                originZ = getChildTag(schematic, "DenizenOriginZ", IntTag.class).getValue();
            }
            catch (Exception e) {
                // Default origin, why not
            }
            cbs.x_width = width;
            cbs.z_height = length;
            cbs.y_length = height;
            cbs.center_x = originX;
            cbs.center_y = originY;
            cbs.center_z = originZ;
            // Disregard Offset
            String materials = getChildTag(schematic, "Materials", StringTag.class).getValue();
            if (!materials.equals("Alpha")) {
                throw new Exception("Schematic file is not an Alpha schematic!");
            }
            byte[] blockId = getChildTag(schematic, "Blocks", ByteArrayTag.class).getValue();
            byte[] blockData = getChildTag(schematic, "Data", ByteArrayTag.class).getValue();
            byte[] addId = new byte[0];
            short[] blocks = new short[blockId.length];
            if (schematic.containsKey("AddBlocks")) {
                addId = getChildTag(schematic, "AddBlocks", ByteArrayTag.class).getValue();
            }
            for (int index = 0; index < blockId.length; index++) {
                if ((index >> 1) >= addId.length) {
                    blocks[index] = (short) (blockId[index] & 0xFF);
                }
                else {
                    if ((index & 1) == 0) {
                        blocks[index] = (short) (((addId[index >> 1] & 0x0F) << 8) + (blockId[index] & 0xFF));
                    }
                    else {
                        blocks[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (blockId[index] & 0xFF));
                    }
                }
            }
            List<Tag> tileEntities = getChildTag(schematic, "TileEntities", ListTag.class).getValue();
            Map<BlockVector, Map<String, Tag>> tileEntitiesMap = new HashMap<BlockVector, Map<String, Tag>>();
            for (Tag tag : tileEntities) {
                if (!(tag instanceof CompoundTag)) {
                    continue;
                }
                CompoundTag t = (CompoundTag) tag;
                int x = 0;
                int y = 0;
                int z = 0;
                Map<String, Tag> values = new HashMap<String, Tag>();
                for (Map.Entry<String, Tag> entry : t.getValue().entrySet()) {
                    if (entry.getKey().equals("x")) {
                        if (entry.getValue() instanceof IntTag) {
                            x = ((IntTag) entry.getValue()).getValue();
                        }
                    }
                    else if (entry.getKey().equals("y")) {
                        if (entry.getValue() instanceof IntTag) {
                            y = ((IntTag) entry.getValue()).getValue();
                        }
                    }
                    else if (entry.getKey().equals("z")) {
                        if (entry.getValue() instanceof IntTag) {
                            z = ((IntTag) entry.getValue()).getValue();
                        }
                    }
                    values.put(entry.getKey(), entry.getValue());
                }
                BlockVector vec = new BlockVector(x, y, z);
                tileEntitiesMap.put(vec, values);
            }
            org.bukkit.util.Vector vec = new Vector(width, height, length);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    for (int z = 0; z < length; z++) {
                        int index = y * width * length + z * width + x;
                        BlockVector pt = new BlockVector(x, y, z);
                        BlockData block = NMSHandler.getInstance().getBlockHelper().getBlockData(blocks[index], blockData[index]);
                        if (tileEntitiesMap.containsKey(pt)) {
                            CompoundTag otag = NMSHandler.getInstance().createCompoundTag(tileEntitiesMap.get(pt));
                            block.setCompoundTag(otag);
                        }
                        cbs.blocks.add(block);
                    }
                }
            }
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        return cbs;
    }

    private static <T extends Tag> T getChildTag(Map<String, Tag> items, String key,
                                                 Class<T> expected) throws Exception {
        if (!items.containsKey(key)) {
            throw new Exception("Schematic file is missing a '" + key + "' tag");
        }
        Tag tag = items.get(key);
        if (!expected.isInstance(tag)) {
            throw new Exception(key + " tag is not of tag type " + expected.getName());
        }
        return expected.cast(tag);
    }

    // Thanks to WorldEdit for sample code
    public void saveMCEditFormatToStream(OutputStream os) {
        try {
            HashMap<String, Tag> schematic = new HashMap<String, Tag>();
            schematic.put("Width", new ShortTag((short) (x_width)));
            schematic.put("Length", new ShortTag((short) (z_height)));
            schematic.put("Height", new ShortTag((short) (y_length)));
            schematic.put("Materials", new StringTag("Alpha"));
            schematic.put("DenizenOriginX", new IntTag((int) center_x));
            schematic.put("DenizenOriginY", new IntTag((int) center_y));
            schematic.put("DenizenOriginZ", new IntTag((int) center_z));
            schematic.put("WEOriginX", new IntTag((int) center_x));
            schematic.put("WEOriginY", new IntTag((int) center_y));
            schematic.put("WEOriginZ", new IntTag((int) center_z));
            schematic.put("WEOffsetX", new IntTag(0));
            schematic.put("WEOffsetY", new IntTag(0));
            schematic.put("WEOffsetZ", new IntTag(0));
            byte[] blocks = new byte[(int) ((x_width) * (y_length) * (z_height))];
            byte[] addBlocks = null;
            byte[] blockData = new byte[blocks.length];
            ArrayList<Tag> tileEntities = new ArrayList<Tag>();
            int indexer = 0;
            for (int x = 0; x < x_width; x++) {
                for (int y = 0; y < y_length; y++) {
                    for (int z = 0; z < z_height; z++) {
                        int index = (int) (y * (x_width) * (z_height) + z * (x_width) + x);
                        BlockData bd = this.blocks.get(indexer);//blockAt(x, y, z);
                        indexer++;
                        if (bd.getMaterial().getId() > 255) {
                            if (addBlocks == null) {
                                addBlocks = new byte[(blocks.length >> 1) + 1];
                            }
                            addBlocks[index >> 1] = (byte) (((index & 1) == 0) ?
                                    addBlocks[index >> 1] & 0xF0 | (bd.getMaterial().getId() >> 8) & 0xF
                                    : addBlocks[index >> 1] & 0xF | ((bd.getMaterial().getId() >> 8) & 0xF) << 4);
                        }
                        blocks[index] = (byte) bd.getMaterial().getId();
                        blockData[index] = bd.getData();

                        CompoundTag rawTag = bd.getCompoundTag();
                        if (rawTag != null) {
                            HashMap<String, Tag> values = new HashMap<String, Tag>();
                            for (Map.Entry<String, Tag> entry : rawTag.getValue().entrySet()) {
                                values.put(entry.getKey(), entry.getValue());
                            }
                            // TODO: ??? -> values.put("id", new StringTag(null)); // block.getNbtId()
                            values.put("x", new IntTag(x));
                            values.put("y", new IntTag(y));
                            values.put("z", new IntTag(z));
                            CompoundTag tileEntityTag = NMSHandler.getInstance().createCompoundTag(values);
                            tileEntities.add(tileEntityTag);
                        }
                    }
                }
            }
            schematic.put("Blocks", new ByteArrayTag(blocks));
            schematic.put("Data", new ByteArrayTag(blockData));
            schematic.put("Entities", new ListTag(CompoundTag.class, new ArrayList<Tag>()));
            schematic.put("TileEntities", new ListTag(CompoundTag.class, tileEntities));
            if (addBlocks != null) {
                schematic.put("AddBlocks", new ByteArrayTag(addBlocks));
            }
            CompoundTag schematicTag = NMSHandler.getInstance().createCompoundTag(schematic);
            NBTOutputStream stream = new NBTOutputStream(new GZIPOutputStream(os));
            stream.writeNamedTag("Schematic", schematicTag);
            os.flush();
            stream.close();
        }
        catch (Exception e) {
            dB.echoError(e);
        }
    }
}

package com.denizenscript.denizen.utilities.blocks;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.scripts.commands.world.SchematicCommand;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class CuboidBlockSet implements BlockSet {

    public CuboidBlockSet() {
    }

    public CuboidBlockSet(CuboidTag cuboid, Location center, boolean copyFlags) {
        hasFlags = copyFlags;
        Location low = cuboid.pairs.get(0).low;
        Location high = cuboid.pairs.get(0).high;
        x_width = (int) ((high.getX() - low.getX()) + 1);
        y_length = (int) ((high.getY() - low.getY()) + 1);
        z_height = (int) ((high.getZ() - low.getZ()) + 1);
        center_x = (int) (center.getX() - low.getX());
        center_y = (int) (center.getY() - low.getY());
        center_z = (int) (center.getZ() - low.getZ());
        blocks = new FullBlockData[(x_width * y_length * z_height)];
        int index = 0;
        for (int x = 0; x < x_width; x++) {
            for (int y = 0; y < y_length; y++) {
                for (int z = 0; z < z_height; z++) {
                    blocks[index++] = new FullBlockData(low.clone().add(x, y, z).getBlock(), copyFlags);
                }
            }
        }
    }

    public void buildDelayed(CuboidTag cuboid, Location center, Runnable runme, long maxDelayMs, boolean copyFlags) {
        hasFlags = copyFlags;
        Location low = cuboid.pairs.get(0).low;
        Location high = cuboid.pairs.get(0).high;
        x_width = (int) ((high.getX() - low.getX()) + 1);
        y_length = (int) ((high.getY() - low.getY()) + 1);
        z_height = (int) ((high.getZ() - low.getZ()) + 1);
        center_x = (int) (center.getX() - low.getX());
        center_y = (int) (center.getY() - low.getY());
        center_z = (int) (center.getZ() - low.getZ());
        final long goal = (long) (x_width * y_length * z_height);
        blocks = new FullBlockData[(x_width * y_length * z_height)];
        new BukkitRunnable() {
            int index = 0;
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                while (index < goal) {
                    long z = index % ((long) (z_height));
                    long y = ((index - z) % ((long) (y_length * z_height))) / ((long) z_height);
                    long x = (index - y - z) / ((long) (y_length * z_height));
                    blocks[index] = new FullBlockData(low.clone().add(x, y, z).getBlock(), copyFlags);
                    index++;
                    if (System.currentTimeMillis() - start > maxDelayMs) {
                        SchematicCommand.noPhys = false;
                        return;
                    }
                }
                if (runme != null) {
                    runme.run();
                }
                cancel();

            }
        }.runTaskTimer(Denizen.getInstance(), 1, 1);
    }

    public FullBlockData[] blocks = null;

    public boolean hasFlags = false;

    public int x_width;

    public int y_length;

    public int z_height;

    public int center_x;

    public int center_y;

    public int center_z;

    public ListTag entities = null;

    public CuboidBlockSet duplicate() {
        CuboidBlockSet result = new CuboidBlockSet();
        result.blocks = blocks.clone();
        result.hasFlags = hasFlags;
        result.x_width = x_width;
        result.y_length = y_length;
        result.z_height = z_height;
        result.center_x = center_x;
        result.center_y = center_y;
        result.center_z = center_z;
        if (entities != null) {
            result.entities = entities.duplicate();
        }
        return result;
    }

    @Override
    public FullBlockData[] getBlocks() {
        return blocks;
    }

    public CuboidTag getCuboid(Location loc) {
        Location low = loc.clone().subtract(center_x, center_y, center_z);
        Location high = low.clone().add(x_width - 1, y_length - 1, z_height - 1); // Note: -1 because CuboidTag implicitly includes an extra block by design.
        return new CuboidTag(low, high);
    }

    public static BlockFace rotateFaceOne(BlockFace face) {
        switch (face) {
            case NORTH:
                return BlockFace.WEST;
            case EAST:
                return BlockFace.NORTH;
            case SOUTH:
                return BlockFace.EAST;
            case WEST:
                return BlockFace.SOUTH;
            default:
                return BlockFace.SELF;
        }
    }

    public static HashSet<EntityType> copyTypes = new HashSet<>(Arrays.asList(EntityType.PAINTING, EntityType.ITEM_FRAME, EntityType.ARMOR_STAND));

    public void buildEntities(CuboidTag cuboid, Location center) {
        entities = new ListTag();
        for (Entity ent : cuboid.getWorld().getEntities()) {
            if (cuboid.isInsideCuboid(ent.getLocation())) {
                if (copyTypes.contains(ent.getType())) {
                    EntityTag entTag = new EntityTag(ent);
                    if (entTag.isPlayer() || entTag.isNPC()) {
                        continue;
                    }
                    MapTag data = new MapTag();
                    data.putObject("entity", entTag.describe(null));
                    data.putObject("rotation", new ElementTag(0));
                    Vector offset = ent.getLocation().toVector().subtract(center.toVector());
                    data.putObject("offset", new LocationTag((String) null, offset.getX(), offset.getY(), offset.getZ(), ent.getLocation().getYaw(), ent.getLocation().getPitch()));
                    entities.addObject(data);
                }
            }
        }
    }

    public void pasteEntities(LocationTag relative) {
        if (entities == null) {
            return;
        }
        for (MapTag data : entities.filter(MapTag.class, CoreUtilities.noDebugContext)) {
            try {
                LocationTag offset = data.getObject("offset").asType(LocationTag.class, CoreUtilities.noDebugContext);
                int rotation = data.getObject("rotation").asElement().asInt();
                EntityTag entity = data.getObject("entity").asType(EntityTag.class, CoreUtilities.noDebugContext);
                if (entity == null || offset == null) {
                    continue;
                }
                entity = entity.duplicate();
                offset = offset.clone();
                if (rotation != 0) {
                    ArrayList<Mechanism> mechs = new ArrayList<>(entity.getWaitingMechanisms().size());
                    for (Mechanism mech : entity.getWaitingMechanisms()) {
                        if (mech.getName().equals("rotation")) {
                            String rotationName = mech.getValue().asString();
                            BlockFace face = BlockFace.valueOf(rotationName.toUpperCase());
                            for (int i = 0; i < rotation; i += 90) {
                                face = rotateFaceOne(face);
                            }
                            offset.add(face.getDirection().multiply(0.1)); // Compensate for hanging locations being very stupid
                            mechs.add(new Mechanism("rotation", new ElementTag(face.name()), CoreUtilities.noDebugContext));
                        }
                        else {
                            mechs.add(new Mechanism(mech.getName(), mech.value, CoreUtilities.noDebugContext));
                        }
                    }
                    entity.mechanisms = mechs;
                }
                else {
                    for (Mechanism mechanism : entity.mechanisms) {
                        mechanism.context = CoreUtilities.noDebugContext;
                    }
                }
                Location spawnLoc = relative.clone().add(offset);
                spawnLoc.setYaw(offset.getYaw() - rotation);
                spawnLoc.setPitch(offset.getPitch());
                entity.spawnAt(spawnLoc);
            }
            catch (Exception ex) {
                Debug.echoError(ex);
            }
        }
    }

    public void setBlockSingle(FullBlockData block, int x, int y, int z, InputParams input) {
        if (input.noAir && block.data.getMaterial() == Material.AIR) {
            return;
        }
        if (block.data.getMaterial() == Material.STRUCTURE_VOID) {
            return;
        }
        int finalY = input.centerLocation.getBlockY() + y - center_y;
        if (!Utilities.isLocationYSafe(finalY, input.centerLocation.getWorld())) {
            return;
        }
        Block destBlock = input.centerLocation.clone().add(x - center_x, y - center_y, z - center_z).getBlock();
        if (input.mask != null && !input.mask.contains(destBlock.getType())) {
            return;
        }
        if (input.fakeTo == null) {
            block.set(destBlock, false);
        }
        else {
            FakeBlock.showFakeBlockTo(input.fakeTo, new LocationTag(destBlock.getLocation()), new MaterialTag(block.data), input.fakeDuration, false);
        }
    }

    @Override
    public void setBlocksDelayed(final Runnable runme, final InputParams input, long maxDelayMs) {
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
                    if (System.currentTimeMillis() - start > maxDelayMs) {
                        SchematicCommand.noPhys = false;
                        return;
                    }
                }
                SchematicCommand.noPhys = false;
                cancel();
                if (runme != null) {
                    runme.run();
                }
            }
        }.runTaskTimer(Denizen.getInstance(), 1, 1);
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

    public void rotateEntitiesOne() {
        if (entities == null) {
            return;
        }
        ListTag outEntities = new ListTag();
        for (MapTag data : entities.filter(MapTag.class, CoreUtilities.noDebugContext)) {
            LocationTag offset = data.getObject("offset").asType(LocationTag.class, CoreUtilities.noDebugContext);
            int rotation = data.getObject("rotation").asElement().asInt();
            offset = new LocationTag((String) null, offset.getZ(), offset.getY(), -offset.getX() + 1, offset.getYaw(), offset.getPitch());
            rotation += 90;
            while (rotation >= 360) {
                rotation -= 360;
            }
            data = data.duplicate();
            data.putObject("offset", offset);
            data.putObject("rotation", new ElementTag(rotation));
            outEntities.addObject(data);
        }
        entities = outEntities;
    }

    public void rotateOne() {
        rotateEntitiesOne();
        FullBlockData[] bd = new FullBlockData[blocks.length];
        int index = 0;
        int cx = center_x;
        center_x = center_z;
        center_z = x_width - 1 - cx;
        for (int x = 0; x < z_height; x++) {
            for (int y = 0; y < y_length; y++) {
                for (int z = x_width - 1; z >= 0; z--) {
                    bd[index++] = blockAt(z, y, x).rotateOne();
                }
            }
        }
        int xw = x_width;
        x_width = z_height;
        z_height = xw;
        blocks = bd;
    }

    public void flipX() {
        FullBlockData[] bd = new FullBlockData[blocks.length];
        int index = 0;
        center_x = x_width - center_x;
        for (int x = x_width - 1; x >= 0; x--) {
            for (int y = 0; y < y_length; y++) {
                for (int z = 0; z < z_height; z++) {
                    bd[index++] = blockAt(x, y, z);
                }
            }
        }
        blocks = bd;
    }

    public void flipY() {
        FullBlockData[] bd = new FullBlockData[blocks.length];
        int index = 0;
        center_x = x_width - center_x;
        for (int x = 0; x < x_width; x++) {
            for (int y = y_length - 1; y >= 0; y--) {
                for (int z = 0; z < z_height; z++) {
                    bd[index++] = blockAt(x, y, z);
                }
            }
        }
        blocks = bd;
    }

    public void flipZ() {
        FullBlockData[] bd = new FullBlockData[blocks.length];
        int index = 0;
        center_x = x_width - center_x;
        for (int x = 0; x < x_width; x++) {
            for (int y = 0; y < y_length; y++) {
                for (int z = z_height - 1; z >= 0; z--) {
                    bd[index++] = blockAt(x, y, z);
                }
            }
        }
        blocks = bd;
    }

    public FullBlockData blockAt(double X, double Y, double Z) {
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

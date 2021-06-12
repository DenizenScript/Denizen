package com.denizenscript.denizen.nms.v1_17.impl.network.handlers;

import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.blocks.FakeBlock;
import com.denizenscript.denizen.utilities.debugging.Debug;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacket;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.bukkit.craftbukkit.v1_17_R1.block.data.CraftBlockData;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class FakeBlockHelper {

    public static Field BITMASK_MAPCHUNK = ReflectionHelper.getFields(ClientboundLevelChunkPacket.class).get("c");
    public static Field DATA_MAPCHUNK = ReflectionHelper.getFields(ClientboundLevelChunkPacket.class).get("f");
    public static Field BLOCKENTITIES_MAPCHUNK = ReflectionHelper.getFields(ClientboundLevelChunkPacket.class).get("g");
    public static Field BIOMESTORAGE_MAPCHUNK = ReflectionHelper.getFields(ClientboundLevelChunkPacket.class).get("e");

    public static BlockState getNMSState(FakeBlock block) {
        return ((CraftBlockData) block.material.getModernData()).getState();
    }

    public static boolean anyBlocksInSection(List<FakeBlock> blocks, int y) {
        int minY = y << 4;
        int maxY = (y << 4) + 16;
        for (FakeBlock block : blocks) {
            int blockY = block.location.getBlockY();
            if (blockY >= minY && blockY < maxY) {
                return true;
            }
        }
        return false;
    }

    public static int indexInPalette(BlockState data) {
        return LevelChunkSection.GLOBAL_BLOCKSTATE_PALETTE.idFor(data);
    }

    public static int blockArrayIndex(int x, int y, int z) {
        return y * (16 * 16) + z * 16 + x;
    }

    public static int getPaletteSubId(int[] palette, int id) {
        for (int i = 0; i < palette.length; i++) {
            if (palette[i] == id) {
                return i;
            }
        }
        return -1;
    }

    public static ClientboundLevelChunkPacket handleMapChunkPacket(ClientboundLevelChunkPacket originalPacket, List<FakeBlock> blocks) {
        try {
            ClientboundLevelChunkPacket packet = new ClientboundLevelChunkPacket(DenizenNetworkManagerImpl.copyPacket(originalPacket));
            // TODO: properly update HeightMap?
            int bitmask = BITMASK_MAPCHUNK.getInt(packet);
            byte[] data = (byte[]) DATA_MAPCHUNK.get(packet);
            FriendlyByteBuf serial = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
            FriendlyByteBuf outputSerial = new FriendlyByteBuf(Unpooled.buffer(data.length));
            boolean isFull = true;//packet.f();
            List<net.minecraft.nbt.CompoundTag> blockEntities = new ArrayList<>((List<net.minecraft.nbt.CompoundTag>) BLOCKENTITIES_MAPCHUNK.get(packet));
            BLOCKENTITIES_MAPCHUNK.set(packet, blockEntities);
            ListIterator<CompoundTag> iterator = blockEntities.listIterator();
            while (iterator.hasNext()) {
                net.minecraft.nbt.CompoundTag blockEnt = iterator.next();
                int x = blockEnt.getInt("x");
                int y = blockEnt.getInt("y");
                int z = blockEnt.getInt("z");
                for (FakeBlock block : blocks) {
                    LocationTag loc = block.location;
                    if (loc.getBlockX() == x && loc.getBlockY() == y && loc.getBlockZ() == z) {
                        iterator.remove();
                        break;
                    }
                }
            }
            for (FakeBlock block : blocks) {
                LocationTag loc = block.location;
                net.minecraft.nbt.CompoundTag newCompound = new net.minecraft.nbt.CompoundTag();
                newCompound.putInt("x", loc.getBlockX());
                newCompound.putInt("y", loc.getBlockY());
                newCompound.putInt("z", loc.getBlockZ());
                newCompound.putString("id", block.material.getMaterial().getKey().toString());
                blockEntities.add(newCompound);
            }
            for (int y = 0; y < 16; y++) {
                if ((bitmask & (1 << y)) != 0) {
                    int blockCount = serial.readShort();
                    int width = serial.readUnsignedByte();
                    int paletteLen = serial.readVarInt();
                    int[] palette = new int[paletteLen];
                    for (int p = 0; p < paletteLen; p++) {
                        palette[p] = serial.readVarInt();
                    }
                    int dataLen = serial.readVarInt();
                    long[] blockListHelper = new long[dataLen];
                    for (int i = 0; i < blockListHelper.length; i++) {
                        blockListHelper[i] = serial.readLong();
                    }
                    outputSerial.writeShort(blockCount);
                    if (!anyBlocksInSection(blocks, y)) {
                        outputSerial.writeByte(width);
                        outputSerial.writeVarInt(paletteLen);
                        for (int p = 0; p < paletteLen; p++) {
                            outputSerial.writeVarInt(palette[p]);
                        }
                        outputSerial.writeLongArray(blockListHelper);
                        continue;
                    }
                    char dataBitsF = (char)(64 / width);
                    int expectedLength = (4096 + dataBitsF - 1) / dataBitsF;
                    if (blockListHelper.length != expectedLength) {
                        return originalPacket; // This chunk is too-complex and is using non-standard chunk format. For now, just ignore it.
                        // TODO: Add support for processing very-complex chunks (DataPaletteHash might be responsible for the unique format?)
                    }
                    BitStorage bits = new BitStorage(width, 4096, blockListHelper);
                    int minY = y << 4;
                    int maxY = (y << 4) + 16;
                    for (FakeBlock block : blocks) {
                        int blockY = block.location.getBlockY();
                        if (blockY >= minY && blockY < maxY) {
                            int blockX = block.location.getBlockX();
                            int blockZ = block.location.getBlockZ();
                            blockX -= (blockX >> 4) * 16;
                            blockY -= (blockY >> 4) * 16;
                            blockZ -= (blockZ >> 4) * 16;
                            int blockIndex = blockArrayIndex(blockX, blockY, blockZ);
                            BlockState replacementData = getNMSState(block);
                            int globalPaletteIndex = indexInPalette(replacementData);
                            int subPaletteId = getPaletteSubId(palette, globalPaletteIndex);
                            if (subPaletteId == -1) {
                                int[] newPalette = new int[paletteLen + 1];
                                if (paletteLen >= 0) System.arraycopy(palette, 0, newPalette, 0, paletteLen);
                                newPalette[paletteLen] = globalPaletteIndex;
                                subPaletteId = paletteLen;
                                paletteLen++;
                                palette = newPalette;
                                int newWidth = Mth.ceillog2(paletteLen);
                                if (newWidth > width) {
                                    BitStorage newBits = new BitStorage(newWidth, 4096);
                                    for (int i = 0; i < bits.getSize(); i++) {
                                        newBits.getAndSet(i, bits.get(i));
                                    }
                                    bits = newBits;
                                    width = newWidth;
                                }
                            }
                            bits.getAndSet(blockIndex, subPaletteId);
                        }
                    }
                    outputSerial.writeByte(width);
                    outputSerial.writeVarInt(paletteLen);
                    for (int p = 0; p < palette.length; p++) {
                        outputSerial.writeVarInt(palette[p]);
                    }
                    outputSerial.writeLongArray(bits.getRaw());
                }
            }
            if (isFull) {
                int[] biomes = (int[]) BIOMESTORAGE_MAPCHUNK.get(packet);
                if (biomes != null) {
                    outputSerial.writeVarIntArray(biomes);
                }
            }
            byte[] outputBytes = outputSerial.array();
            DATA_MAPCHUNK.set(packet, outputBytes);
            return packet;
        }
        catch (Exception ex) {
            Debug.echoError(ex);
        }
        return null;
    }
}

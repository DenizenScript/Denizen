package net.aufdemrand.denizen.nms;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.aufdemrand.denizen.nms.abstracts.*;
import net.aufdemrand.denizen.nms.helpers.*;
import net.aufdemrand.denizen.nms.impl.BiomeNMS_v1_14_R1;
import net.aufdemrand.denizen.nms.impl.ProfileEditor_v1_14_R1;
import net.aufdemrand.denizen.nms.impl.Sidebar_v1_14_R1;
import net.aufdemrand.denizen.nms.impl.blocks.BlockLight_v1_14_R1;
import net.aufdemrand.denizen.nms.impl.jnbt.CompoundTag_v1_14_R1;
import net.aufdemrand.denizen.nms.impl.packets.handlers.DenizenPacketListener_v1_14_R1;
import net.aufdemrand.denizen.nms.interfaces.*;
import net.aufdemrand.denizen.nms.interfaces.packets.PacketHandler;
import net.aufdemrand.denizen.nms.util.PlayerProfile;
import net.aufdemrand.denizen.nms.util.jnbt.CompoundTag;
import net.aufdemrand.denizen.nms.util.jnbt.Tag;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import net.aufdemrand.denizencore.utilities.debugging.dB;
import net.minecraft.server.v1_14_R1.IInventory;
import net.minecraft.server.v1_14_R1.INamableTileEntity;
import net.minecraft.server.v1_14_R1.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.craftbukkit.v1_14_R1.CraftServer;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftInventoryCustom;
import org.bukkit.craftbukkit.v1_14_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_14_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.lang.reflect.Field;
import java.util.Map;

public class Handler_v1_14_R1 extends NMSHandler {

    private final AdvancementHelper advancementHelper = new AdvancementHelper_v1_14_R1();
    private final AnimationHelper animationHelper = new AnimationHelper_v1_14_R1();
    private final BlockHelper blockHelper = new BlockHelper_v1_14_R1();
    private final ChunkHelper chunkHelper = new ChunkHelper_v1_14_R1();
    private final CustomEntityHelper customEntityHelper = new CustomEntityHelper_v1_14_R1();
    private final EntityHelper entityHelper = new EntityHelper_v1_14_R1();
    private final FishingHelper fishingHelper = new FishingHelper_v1_14_R1();
    private final ItemHelper itemHelper = new ItemHelper_v1_14_R1();
    private final SoundHelper soundHelper = new SoundHelper_v1_14_R1();
    private final PacketHelper packetHelper = new PacketHelper_v1_14_R1();
    private final ParticleHelper particleHelper = new ParticleHelper_v1_14_R1();
    private final PlayerHelper playerHelper = new PlayerHelper_v1_14_R1();
    private final WorldHelper worldHelper = new WorldHelper_v1_14_R1();

    private final ProfileEditor profileEditor = new ProfileEditor_v1_14_R1();

    @Override
    public void disableAsyncCatcher() {
        org.spigotmc.AsyncCatcher.enabled = false;
    }

    @Override
    public boolean isCorrectMappingsCode() {
        return ((CraftMagicNumbers) CraftMagicNumbers.INSTANCE).getMappingsVersion().equals("8b7fe9012a93b36df04844a6c990de27");
    }

    @Override
    public Thread getMainThread() {
        return ((CraftServer) Bukkit.getServer()).getServer().serverThread;
    }

    @Override
    public double[] getRecentTps() {
        return ((CraftServer) Bukkit.getServer()).getServer().recentTps;
    }

    @Override
    public AdvancementHelper getAdvancementHelper() {
        return advancementHelper;
    }

    @Override
    public AnimationHelper getAnimationHelper() {
        return animationHelper;
    }

    @Override
    public BlockHelper getBlockHelper() {
        return blockHelper;
    }

    @Override
    public ChunkHelper getChunkHelper() {
        return chunkHelper;
    }

    @Override
    public CustomEntityHelper getCustomEntityHelper() {
        return customEntityHelper;
    }

    @Override
    public EntityHelper getEntityHelper() {
        return entityHelper;
    }

    @Override
    public FishingHelper getFishingHelper() {
        return fishingHelper;
    }

    @Override
    public ItemHelper getItemHelper() {
        return itemHelper;
    }

    @Override
    public SoundHelper getSoundHelper() {
        return soundHelper;
    }

    @Override
    public PacketHelper getPacketHelper() {
        return packetHelper;
    }

    @Override
    public ParticleHelper getParticleHelper() {
        return particleHelper;
    }

    @Override
    public PlayerHelper getPlayerHelper() {
        return playerHelper;
    }

    @Override
    public WorldHelper getWorldHelper() {
        return worldHelper;
    }

    @Override
    public void enablePacketInterception(PacketHandler packetHandler) {
        DenizenPacketListener_v1_14_R1.enable(packetHandler);
    }

    @Override
    public CompoundTag createCompoundTag(Map<String, Tag> value) {
        return new CompoundTag_v1_14_R1(value);
    }

    @Override
    public Sidebar createSidebar(Player player) {
        return new Sidebar_v1_14_R1(player);
    }

    @Override
    public BlockLight createBlockLight(Location location, int lightLevel, long ticks) {
        return BlockLight_v1_14_R1.createLight(location, lightLevel, ticks);
    }

    @Override
    public PlayerProfile fillPlayerProfile(PlayerProfile playerProfile) {
        try {
            if (playerProfile != null) {
                GameProfile gameProfile = new GameProfile(playerProfile.getUniqueId(), playerProfile.getName());
                gameProfile.getProperties().get("textures").clear();
                if (playerProfile.getTextureSignature() != null) {
                    gameProfile.getProperties().put("textures", new Property("textures", playerProfile.getTexture(), playerProfile.getTextureSignature()));
                }
                else {
                    gameProfile.getProperties().put("textures", new Property("textures", playerProfile.getTexture()));
                }
                MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();
                GameProfile gameProfile1 = null;
                if (gameProfile.getId() != null) {
                    gameProfile1 = minecraftServer.getUserCache().a(gameProfile.getId());
                }
                if (gameProfile1 == null && gameProfile.getName() != null) {
                    gameProfile1 = minecraftServer.getUserCache().getProfile(gameProfile.getName());
                }
                if (gameProfile1 == null) {
                    gameProfile1 = gameProfile;
                }
                if (playerProfile.hasTexture()) {
                    gameProfile1.getProperties().get("textures").clear();
                    if (playerProfile.getTextureSignature() != null) {
                        gameProfile1.getProperties().put("textures", new Property("textures", playerProfile.getTexture(), playerProfile.getTextureSignature()));
                    }
                    else {
                        gameProfile1.getProperties().put("textures", new Property("textures", playerProfile.getTexture()));
                    }
                }
                if (Iterables.getFirst(gameProfile1.getProperties().get("textures"), null) == null) {
                    gameProfile1 = minecraftServer.getMinecraftSessionService().fillProfileProperties(gameProfile1, true);
                }
                Property property = Iterables.getFirst(gameProfile1.getProperties().get("textures"), null);
                return new PlayerProfile(gameProfile1.getName(), gameProfile1.getId(),
                        property != null ? property.getValue() : null,
                        property != null ? property.getSignature() : null);
            }
        }
        catch (Exception e) {
            if (dB.verbose) {
                dB.echoError(e);
            }
        }
        return null;
    }

    @Override
    public int getPort() {
        return ((CraftServer) Bukkit.getServer()).getServer().getPort();
    }

    @Override
    public String getTitle(Inventory inventory) {
        IInventory nms = ((CraftInventory) inventory).getInventory();
        if (nms instanceof INamableTileEntity) {
            return CraftChatMessage.fromComponent(((INamableTileEntity) nms).getDisplayName());
        }
        else if (MINECRAFT_INVENTORY.isInstance(nms)) {
            try {
                return (String) INVENTORY_TITLE.get(nms);
            }
            catch (IllegalAccessException e) {
                dB.echoError(e);
            }
        }
        return null;
    }

    private static final Class MINECRAFT_INVENTORY;
    private static final Field INVENTORY_TITLE;

    static {
        Class minecraftInv = null;
        Field title = null;
        try {
            for (Class clzz : CraftInventoryCustom.class.getDeclaredClasses()) {
                if (CoreUtilities.toLowerCase(clzz.getName()).contains("minecraftinventory")) { // MinecraftInventory.
                    minecraftInv = clzz;
                    title = clzz.getDeclaredField("title");
                    title.setAccessible(true);
                    break;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        MINECRAFT_INVENTORY = minecraftInv;
        INVENTORY_TITLE = title;
    }

    @Override
    public PlayerProfile getPlayerProfile(Player player) {
        GameProfile gameProfile = ((CraftPlayer) player).getProfile();
        Property property = Iterables.getFirst(gameProfile.getProperties().get("textures"), null);
        return new PlayerProfile(gameProfile.getName(), gameProfile.getId(),
                property != null ? property.getValue() : null,
                property != null ? property.getSignature() : null);
    }

    @Override
    public ProfileEditor getProfileEditor() {
        return profileEditor;
    }

    @Override
    public BiomeNMS getBiomeNMS(Biome biome) {
        return new BiomeNMS_v1_14_R1(biome);
    }

    @Override
    public Boolean getSwitchState(Block b) {
        ModernBlockData mbd = new ModernBlockData(b);
        if (mbd.data instanceof Openable) {
            return ((Openable) mbd.data).isOpen();
        }
        else if (mbd.data instanceof Powerable) {
            return ((Powerable) mbd.data).isPowered();
        }
        return null;
    }

    @Override
    public boolean setSwitchState(Location interactLocation, boolean state) {
        Block block = interactLocation.getBlock();
        ModernBlockData mbd = new ModernBlockData(block);
        if (mbd.data instanceof Openable) {
            Openable newState = ((Openable) mbd.data);
            newState.setOpen(state);
            NMSHandler.getInstance().getChunkHelper().changeChunkServerThread(block.getWorld());
            interactLocation.getBlock().setBlockData(newState, true);
            interactLocation.getBlock().getState().update(true, true);
            NMSHandler.getInstance().getChunkHelper().restoreServerThread(block.getWorld());
            return true;
        }
        else if (mbd.data instanceof Powerable) {
            Powerable newState = ((Powerable) mbd.data);
            newState.setPowered(state);
            NMSHandler.getInstance().getChunkHelper().changeChunkServerThread(block.getWorld());
            interactLocation.getBlock().setBlockData(newState, true);
            interactLocation.getBlock().getState().update(true, true);
            NMSHandler.getInstance().getChunkHelper().restoreServerThread(block.getWorld());
            return true;
        }
        return false;
    }
}

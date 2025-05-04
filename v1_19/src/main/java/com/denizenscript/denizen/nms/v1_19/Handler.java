package com.denizenscript.denizen.nms.v1_19;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.BiomeNMS;
import com.denizenscript.denizen.nms.abstracts.BlockLight;
import com.denizenscript.denizen.nms.abstracts.ProfileEditor;
import com.denizenscript.denizen.nms.abstracts.Sidebar;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.Tag;
import com.denizenscript.denizen.nms.v1_19.helpers.*;
import com.denizenscript.denizen.nms.v1_19.impl.BiomeNMSImpl;
import com.denizenscript.denizen.nms.v1_19.impl.ProfileEditorImpl;
import com.denizenscript.denizen.nms.v1_19.impl.SidebarImpl;
import com.denizenscript.denizen.nms.v1_19.impl.blocks.BlockLightImpl;
import com.denizenscript.denizen.nms.v1_19.impl.jnbt.CompoundTagImpl;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.biome.Biome;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.boss.CraftBossBar;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftInventoryCustom;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_19_R3.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftNamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.persistence.PersistentDataContainer;
import org.spigotmc.AsyncCatcher;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Handler extends NMSHandler {

    public Handler() {
        advancementHelper = new AdvancementHelperImpl();
        animationHelper = new AnimationHelperImpl();
        blockHelper = new BlockHelperImpl();
        chunkHelper = new ChunkHelperImpl();
        customEntityHelper = new CustomEntityHelperImpl();
        entityHelper = new EntityHelperImpl();
        fishingHelper = new FishingHelperImpl();
        itemHelper = new ItemHelperImpl();
        packetHelper = new PacketHelperImpl();
        playerHelper = new PlayerHelperImpl();
        worldHelper = new WorldHelperImpl();
        enchantmentHelper = new EnchantmentHelperImpl();
    }

    private final ProfileEditor profileEditor = new ProfileEditorImpl();

    private boolean wasAsyncCatcherEnabled;

    @Override
    public void disableAsyncCatcher() {
        wasAsyncCatcherEnabled = AsyncCatcher.enabled;
        AsyncCatcher.enabled = false;
    }

    @Override
    public void undisableAsyncCatcher() {
        AsyncCatcher.enabled = wasAsyncCatcherEnabled;
    }

    @Override
    public boolean isCorrectMappingsCode() {
        return ((CraftMagicNumbers) CraftMagicNumbers.INSTANCE).getMappingsVersion().equals("3009edc0fff87fa34680686663bd59df");
    }

    @Override
    public double[] getRecentTps() {
        return ((CraftServer) Bukkit.getServer()).getServer().recentTps;
    }

    @Override
    public CompoundTag createCompoundTag(Map<String, Tag> value) {
        return new CompoundTagImpl(value);
    }

    @Override
    public Sidebar createSidebar(Player player) {
        return new SidebarImpl(player);
    }

    @Override
    public BlockLight createBlockLight(Location location, int lightLevel, long ticks) {
        return BlockLightImpl.createLight(location, lightLevel, ticks);
    }

    @Override
    public PlayerProfile fillPlayerProfile(PlayerProfile playerProfile) {
        if (playerProfile == null) {
            return null;
        }
        if (playerProfile.getName() == null && playerProfile.getUniqueId() == null) {
            return playerProfile; // Cannot fill without lookup data
        }
        if (playerProfile.hasTexture() && playerProfile.hasTextureSignature() && playerProfile.getName() != null && playerProfile.getUniqueId() != null) {
            return playerProfile; // Already filled
        }
        try {
            GameProfile profile = null;
            MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();
            if (playerProfile.getUniqueId() != null) {
                profile = minecraftServer.getProfileCache().get(playerProfile.getUniqueId()).orElse(null);
            }
            if (profile == null && playerProfile.getName() != null) {
                profile = minecraftServer.getProfileCache().get(playerProfile.getName()).orElse(null);
            }
            if (profile == null) {
                profile = new GameProfile(playerProfile.getUniqueId(), playerProfile.getName());
            }
            Property textures = profile.getProperties().containsKey("textures") ? Iterables.getFirst(profile.getProperties().get("textures"), null) : null;
            if (textures == null || !textures.hasSignature() || profile.getName() == null || profile.getId() == null) {
                profile = minecraftServer.getSessionService().fillProfileProperties(profile, true);
                textures = profile.getProperties().containsKey("textures") ? Iterables.getFirst(profile.getProperties().get("textures"), null) : null;
            }
            return new PlayerProfile(profile.getName(), profile.getId(), textures == null ? null : textures.getValue(), textures == null ? null : textures.getSignature());
        }
        catch (Exception e) {
            if (CoreConfiguration.debugVerbose) {
                Debug.echoError(e);
            }
        }
        return null;
    }

    public static MethodHandle PAPER_INVENTORY_TITLE_GETTER;

    @Override
    public String getTitle(Inventory inventory) {
        Container nms = ((CraftInventory) inventory).getInventory();
        if (inventory instanceof CraftInventoryCustom && Denizen.supportsPaper) {
            try {
                if (PAPER_INVENTORY_TITLE_GETTER == null) {
                    PAPER_INVENTORY_TITLE_GETTER = ReflectionHelper.getMethodHandle(nms.getClass(), "title");
                }
                return PaperAPITools.instance.parseComponent(PAPER_INVENTORY_TITLE_GETTER.invoke(nms));
            }
            catch (Throwable ex) {
                Debug.echoError(ex);
            }
        }
        if (nms instanceof Nameable) {
            return CraftChatMessage.fromComponent(((Nameable) nms).getDisplayName());
        }
        else if (MINECRAFT_INVENTORY.isInstance(nms)) {
            try {
                return (String) INVENTORY_TITLE.get(nms);
            }
            catch (IllegalAccessException e) {
                Debug.echoError(e);
            }
        }
        return "Chest";
    }

    public static MethodHandle AbstractContainerMenu_title_SETTER = ReflectionHelper.getFinalSetter(AbstractContainerMenu.class, "title");

    @Override
    public void setInventoryTitle(InventoryView view, String title) {
        AbstractContainerMenu menu = ((CraftInventoryView) view).getHandle();
        try {
            AbstractContainerMenu_title_SETTER.invoke(menu, componentToNMS(FormattedTextHelper.parse(title, ChatColor.DARK_GRAY)));
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    public static final Class MINECRAFT_INVENTORY;
    public static final Field INVENTORY_TITLE;
    public static final Field ENTITY_BUKKITYENTITY = ReflectionHelper.getFields(Entity.class).get("bukkitEntity");

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
    public List<BiomeNMS> getBiomes(World world) {
        ServerLevel level = ((CraftWorld) world).getHandle();
        ArrayList<BiomeNMS> output = new ArrayList<>();
        for (Map.Entry<ResourceKey<Biome>, Biome> pair : level.registryAccess().registryOrThrow(Registries.BIOME).entrySet()) {
            output.add(new BiomeNMSImpl(level, CraftNamespacedKey.fromMinecraft(pair.getKey().location())));
        }
        return output;
    }

    @Override
    public BiomeNMS getBiomeNMS(World world, NamespacedKey key) {
        BiomeNMSImpl impl = new BiomeNMSImpl(((CraftWorld) world).getHandle(), key);
        if (impl.biomeHolder == null) {
            return null;
        }
        return impl;
    }

    @Override
    public BiomeNMS getBiomeAt(Block block) {
        // Based on CraftWorld source
        ServerLevel level = ((CraftWorld) block.getWorld()).getHandle();
        Holder<Biome> biome = level.getNoiseBiome(block.getX() >> 2, block.getY() >> 2, block.getZ() >> 2);
        ResourceLocation key = level.registryAccess().registryOrThrow(Registries.BIOME).getKey(biome.value());
        return new BiomeNMSImpl(level, CraftNamespacedKey.fromMinecraft(key));
    }

    @Override
    public ArrayList<String> containerListFlags(PersistentDataContainer container, String prefix) {
        prefix = "denizen:" + prefix;
        ArrayList<String> output = new ArrayList<>();
        for (String key : ((CraftPersistentDataContainer) container).getRaw().keySet()) {
            if (key.startsWith(prefix)) {
                output.add(key.substring(prefix.length()));
            }
        }
        return output;
    }

    @Override
    public boolean containerHas(PersistentDataContainer container, String key) {
        return ((CraftPersistentDataContainer) container).getRaw().containsKey(key);
    }

    @Override
    public String containerGetString(PersistentDataContainer container, String key) {
        net.minecraft.nbt.Tag base = ((CraftPersistentDataContainer) container).getRaw().get(key);
        if (base instanceof StringTag) {
            return base.getAsString();
        }
        else if (base instanceof ByteArrayTag) {
            return new String(((ByteArrayTag) base).getAsByteArray(), StandardCharsets.UTF_8);
        }
        return null;
    }

    @Override
    public UUID getBossbarUUID(BossBar bar) {
        return ((CraftBossBar) bar).getHandle().getId();
    }

    public static MethodHandle BOSSBAR_ID_SETTER = ReflectionHelper.getFinalSetterForFirstOfType(BossEvent.class, UUID.class);

    @Override
    public void setBossbarUUID(BossBar bar, UUID id) {
        try {
            BOSSBAR_ID_SETTER.invoke(((CraftBossBar) bar).getHandle(), id);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    public static BaseComponent[] componentToSpigot(Component nms) {
        if (nms == null) {
            return null;
        }
        String json = Component.Serializer.toJson(nms);
        return ComponentSerializer.parse(json);
    }

    public static MutableComponent componentToNMS(BaseComponent[] spigot) {
        if (spigot == null) {
            return null;
        }
        String json = FormattedTextHelper.componentToJson(spigot);
        return Component.Serializer.fromJson(json);
    }
}

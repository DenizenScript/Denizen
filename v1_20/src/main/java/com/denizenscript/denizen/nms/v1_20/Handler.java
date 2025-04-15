package com.denizenscript.denizen.nms.v1_20;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.BiomeNMS;
import com.denizenscript.denizen.nms.abstracts.BlockLight;
import com.denizenscript.denizen.nms.abstracts.ProfileEditor;
import com.denizenscript.denizen.nms.abstracts.Sidebar;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.Tag;
import com.denizenscript.denizen.nms.v1_20.helpers.*;
import com.denizenscript.denizen.nms.v1_20.impl.BiomeNMSImpl;
import com.denizenscript.denizen.nms.v1_20.impl.ProfileEditorImpl;
import com.denizenscript.denizen.nms.v1_20.impl.SidebarImpl;
import com.denizenscript.denizen.nms.v1_20.impl.blocks.BlockLightImpl;
import com.denizenscript.denizen.nms.v1_20.impl.jnbt.CompoundTagImpl;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.objects.properties.item.ItemRawNBT;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.core.QuaternionTag;
import com.denizenscript.denizencore.scripts.commands.core.ReflectionSetCommand;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Rotations;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_20_R4.CraftServer;
import org.bukkit.craftbukkit.v1_20_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R4.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R4.boss.CraftBossBar;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftInventoryCustom;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R4.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v1_20_R4.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_20_R4.util.CraftLocation;
import org.bukkit.craftbukkit.v1_20_R4.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_20_R4.util.CraftNamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.persistence.PersistentDataContainer;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spigotmc.AsyncCatcher;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

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

        registerConversion(ItemTag.class, ItemStack.class, item -> CraftItemStack.asNMSCopy(item.getItemStack()));
        registerConversion(ElementTag.class, Component.class, element -> componentToNMS(FormattedTextHelper.parse(element.asString(), ChatColor.WHITE)));
        registerConversion(MaterialTag.class, BlockState.class, material -> ((CraftBlockData) material.getModernData()).getState());
        registerConversion(LocationTag.class, Rotations.class, location -> new Rotations((float) location.getX(), (float) location.getY(), (float) location.getZ()));
        registerConversion(LocationTag.class, BlockPos.class, CraftLocation::toBlockPosition);
        registerConversion(MapTag.class, net.minecraft.nbt.CompoundTag.class, map ->
                ItemRawNBT.convertObjectToNbt(map.identify(), CoreUtilities.noDebugContext, "(item).") instanceof CompoundTagImpl compoundTag ? compoundTag.toNMSTag() : null);
        registerConversion(LocationTag.class, Vector3f.class, location -> new Vector3f((float) location.getX(), (float) location.getY(), (float) location.getZ()));
        registerConversion(QuaternionTag.class, Quaternionf.class, quaternion -> new Quaternionf(quaternion.x, quaternion.y, quaternion.z, quaternion.w));
    }

    public static <DT extends ObjectTag, JT> void registerConversion(Class<DT> denizenType, Class<JT> javaType, Function<DT, JT> convertor) {
        ReflectionSetCommand.typeConverters.put(javaType, objectTag -> {
            DT denizenObject = objectTag.asType(denizenType, CoreUtilities.noDebugContext);
            return denizenObject != null ? convertor.apply(denizenObject) : null;
        });
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
        return ((CraftMagicNumbers) CraftMagicNumbers.INSTANCE).getMappingsVersion().equals("ee13f98a43b9c5abffdcc0bb24154460");
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
    public CompoundTag parseSNBT(String snbt) {
        try {
            return CompoundTagImpl.fromNMSTag(TagParser.parseTag(snbt));
        }
        catch (CommandSyntaxException e) {
            return null;
        }
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
                profile = ProfileEditorImpl.getGameProfileNoProperties(playerProfile);
            }
            Property textures = profile.getProperties().containsKey("textures") ? Iterables.getFirst(profile.getProperties().get("textures"), null) : null;
            if (textures == null || !textures.hasSignature() || profile.getName() == null || profile.getId() == null) {
                ProfileResult actualProfile = minecraftServer.getSessionService().fetchProfile(profile.getId(), true);
                if (actualProfile == null) {
                    return null;
                }
                profile = actualProfile.profile();
                textures = profile.getProperties().containsKey("textures") ? Iterables.getFirst(profile.getProperties().get("textures"), null) : null;
            }
            return new PlayerProfile(profile.getName(), profile.getId(), textures == null ? null : textures.value(), textures == null ? null : textures.signature());
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
                property != null ? property.value() : null,
                property != null ? property.signature() : null);
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
        return ComponentSerializer.parse(CraftChatMessage.toJSON(nms));
    }

    public static Component componentToNMS(BaseComponent[] spigot) {
        if (spigot == null) {
            return null;
        }
        return CraftChatMessage.fromJSONOrNull(FormattedTextHelper.componentToJson(spigot));
    }
}

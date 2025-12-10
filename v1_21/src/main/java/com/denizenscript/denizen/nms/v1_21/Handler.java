package com.denizenscript.denizen.nms.v1_21;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.BiomeNMS;
import com.denizenscript.denizen.nms.abstracts.BlockLight;
import com.denizenscript.denizen.nms.abstracts.ProfileEditor;
import com.denizenscript.denizen.nms.abstracts.Sidebar;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.nms.v1_21.helpers.*;
import com.denizenscript.denizen.nms.v1_21.impl.BiomeNMSImpl;
import com.denizenscript.denizen.nms.v1_21.impl.ProfileEditorImpl;
import com.denizenscript.denizen.nms.v1_21.impl.SidebarImpl;
import com.denizenscript.denizen.nms.v1_21.impl.blocks.BlockLightImpl;
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
import com.denizenscript.denizencore.utilities.debugging.DebugInternals;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.serialization.DynamicOps;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Rotations;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_21_R7.CraftRegistry;
import org.bukkit.craftbukkit.v1_21_R7.CraftServer;
import org.bukkit.craftbukkit.v1_21_R7.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R7.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_21_R7.boss.CraftBossBar;
import org.bukkit.craftbukkit.v1_21_R7.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R7.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_21_R7.inventory.CraftInventoryCustom;
import org.bukkit.craftbukkit.v1_21_R7.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_21_R7.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_21_R7.legacy.FieldRename;
import org.bukkit.craftbukkit.v1_21_R7.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v1_21_R7.util.*;
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
import java.util.UUID;
import java.util.function.Consumer;
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
        registerConversion(MapTag.class, CompoundTag.class, map -> {
            CompoundBinaryTag compoundTag = (CompoundBinaryTag) ItemRawNBT.convertObjectToNbt(map, CoreUtilities.noDebugContext, "(item).");
            return compoundTag != null ? NBTAdapter.toNMS(compoundTag) : null;
        });
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
    public boolean isExactServerVersionMatch() {
        return Denizen.supportsPaper ? SharedConstants.getCurrentVersion().id().equals("1.21.11") : CraftMagicNumbers.INSTANCE.getMappingsVersion().equals("e3cd927e07e6ff434793a0474c51b2b9");
    }

    @Override
    public double[] getRecentTps() {
        return ((CraftServer) Bukkit.getServer()).getServer().recentTps;
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
                profile = minecraftServer.services().nameToIdCache().get(playerProfile.getUniqueId()).map(result -> new GameProfile(result.id(), result.name())).orElse(null);
            }
            if (profile == null && playerProfile.getName() != null) {
                profile = minecraftServer.services().nameToIdCache().get(playerProfile.getName()).map(result -> new GameProfile(result.id(), result.name())).orElse(null);
            }
            if (profile == null) {
                profile = ProfileEditorImpl.getGameProfileNoProperties(playerProfile);
            }
            Property textures = profile.properties().containsKey("textures") ? Iterables.getFirst(profile.properties().get("textures"), null) : null;
            if (textures == null || !textures.hasSignature() || profile.name() == null || profile.id() == null) {
                profile = minecraftServer.services().profileResolver().fetchById(profile.id()).orElse(null);
                if (profile == null) {
                    return null;
                }
                textures = profile.properties().containsKey("textures") ? Iterables.getFirst(profile.properties().get("textures"), null) : null;
            }
            return new PlayerProfile(profile.name(), profile.id(), textures == null ? null : textures.value(), textures == null ? null : textures.signature());
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
        Property property = Iterables.getFirst(gameProfile.properties().get("textures"), null);
        return new PlayerProfile(gameProfile.name(), gameProfile.id(),
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
        for (Identifier key : level.registryAccess().lookupOrThrow(Registries.BIOME).keySet()) {
            output.add(new BiomeNMSImpl(level, CraftNamespacedKey.fromMinecraft(key)));
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
        Identifier key = level.registryAccess().lookupOrThrow(Registries.BIOME).getKey(biome.value());
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
            return base.asString().get();
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
        return FormattedTextHelper.parseJson(CraftChatMessage.toJSON(nms));
    }

    public static Component componentToNMS(BaseComponent[] spigot) {
        if (spigot == null) {
            return null;
        }
        return CraftChatMessage.fromJSONOrNull(FormattedTextHelper.componentToJson(spigot));
    }

    public static final MethodHandle TAG_VALUE_OUTPUT_CONSTRUCTOR = ReflectionHelper.getConstructor(TagValueOutput.class, ProblemReporter.class, DynamicOps.class, CompoundTag.class);

    public static CompoundTag useValueOutput(Consumer<ValueOutput> handler) {
        ProblemReporter.Collector nmsProblemReporter = new ProblemReporter.Collector();
        TagValueOutput nmsValueOutput = TagValueOutput.createWithContext(nmsProblemReporter, CraftRegistry.getMinecraftRegistry());
        handler.accept(nmsValueOutput);
        handleProblems(nmsProblemReporter);
        return nmsValueOutput.buildResult();
    }

    public static CompoundTag useValueOutput(CompoundTag nmsExistingValue, Consumer<ValueOutput> handler) {
        ProblemReporter.Collector nmsProblemReporter = new ProblemReporter.Collector();
        TagValueOutput nmsValueOutput;
        try {
            nmsValueOutput = (TagValueOutput) TAG_VALUE_OUTPUT_CONSTRUCTOR.invoke(nmsProblemReporter, CraftRegistry.getMinecraftRegistry().createSerializationContext(NbtOps.INSTANCE), nmsExistingValue);
        }
        catch (Throwable e) {
            Debug.echoError(e);
            return nmsExistingValue;
        }
        handler.accept(nmsValueOutput);
        handleProblems(nmsProblemReporter);
        return nmsValueOutput.buildResult();
    }

    public static void useValueInput(CompoundTag nmsTag, Consumer<ValueInput> handler) {
        ProblemReporter.Collector nmsProblemReporter = new ProblemReporter.Collector();
        ValueInput nmsValueInput = TagValueInput.create(nmsProblemReporter, CraftRegistry.getMinecraftRegistry(), nmsTag);
        handler.accept(nmsValueInput);
        handleProblems(nmsProblemReporter);
    }

    private static void handleProblems(ProblemReporter.Collector nmsProblemReporter) {
        if (!nmsProblemReporter.isEmpty()) {
            Debug.echoError(nmsProblemReporter.getTreeReport());
        }
    }

    @Override
    public String updateLegacyName(Class<?> type, String legacyName) {
        return FieldRename.rename(ApiVersion.FIELD_NAME_PARITY, DebugInternals.getFullClassNameOpti(type).replace('.', '/'), legacyName);
    }
}

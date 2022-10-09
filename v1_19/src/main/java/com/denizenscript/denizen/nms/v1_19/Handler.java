package com.denizenscript.denizen.nms.v1_19;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.*;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.Tag;
import com.denizenscript.denizen.nms.v1_19.helpers.*;
import com.denizenscript.denizen.nms.v1_19.impl.BiomeNMSImpl;
import com.denizenscript.denizen.nms.v1_19.impl.ProfileEditorImpl;
import com.denizenscript.denizen.nms.v1_19.impl.SidebarImpl;
import com.denizenscript.denizen.nms.v1_19.impl.blocks.BlockLightImpl;
import com.denizenscript.denizen.nms.v1_19.impl.jnbt.CompoundTagImpl;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftInventoryCustom;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R1.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftMagicNumbers;
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
        particleHelper = new ParticleHelper();
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
        return ((CraftMagicNumbers) CraftMagicNumbers.INSTANCE).getMappingsVersion().equals("69c84c88aeb92ce9fa9525438b93f4fe");
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

    @Override
    public int getPort() {
        return ((CraftServer) Bukkit.getServer()).getServer().getPort();
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
        for (Map.Entry<ResourceKey<Biome>, Biome> pair : level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).entrySet()) {
            output.add(new BiomeNMSImpl(level, pair.getKey().location().toString()));
        }
        return output;
    }

    @Override
    public BiomeNMS getBiomeNMS(World world, String name) {
        BiomeNMSImpl impl = new BiomeNMSImpl(((CraftWorld) world).getHandle(), name);
        if (impl.biomeBase == null) {
            return null;
        }
        return impl;
    }

    @Override
    public BiomeNMS getBiomeAt(Block block) {
        // Based on CraftWorld source
        ServerLevel level = ((CraftWorld) block.getWorld()).getHandle();
        Holder<Biome> biome = level.getNoiseBiome(block.getX() >> 2, block.getY() >> 2, block.getZ() >> 2);
        ResourceLocation key = level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getKey(biome.value());
        String keyText = key.getNamespace().equals("minecraft") ? key.getPath() : key.toString();
        return new BiomeNMSImpl(level, keyText);
    }

    @Override
    public String stringForHover(HoverEvent hover) {
        if (hover.getContents().isEmpty()) {
            return "";
        }
        Content contentObject = hover.getContents().get(0);
        if (contentObject instanceof Text) {
            Object value = ((Text) contentObject).getValue();
            if (value instanceof BaseComponent[]) {
                return FormattedTextHelper.stringify((BaseComponent[]) value);
            }
            else {
                return value.toString();
            }
        }
        else if (contentObject instanceof Item) {
            Item item = (Item) contentObject;
            try {
                net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
                tag.putString("id", item.getId());
                tag.putByte("Count", item.getCount() == -1 ? 1 : (byte) item.getCount());
                if (item.getTag() != null && item.getTag().getNbt() != null) {
                    tag.put("tag", TagParser.parseTag(item.getTag().getNbt()));
                }
                ItemStack itemStack = ItemStack.of(tag);
                return new ItemTag(CraftItemStack.asBukkitCopy(itemStack)).identify();
            }
            catch (Throwable ex) {
                Debug.echoError(ex);
                return null;
            }
        }
        else if (contentObject instanceof net.md_5.bungee.api.chat.hover.content.Entity) {
            net.md_5.bungee.api.chat.hover.content.Entity entity = (net.md_5.bungee.api.chat.hover.content.Entity) contentObject;
            // TODO: Maybe a stabler way of doing this?
            return "e@" + entity.getId();
        }
        else {
            throw new UnsupportedOperationException();
        }
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
        String json = ComponentSerializer.toString(spigot);
        return Component.Serializer.fromJson(json);
    }
}

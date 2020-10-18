package com.denizenscript.denizen.nms.v1_16;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.*;
import com.denizenscript.denizen.nms.v1_16.helpers.*;
import com.denizenscript.denizen.nms.v1_16.impl.BiomeNMSImpl;
import com.denizenscript.denizen.nms.v1_16.impl.ProfileEditorImpl;
import com.denizenscript.denizen.nms.v1_16.impl.SidebarImpl;
import com.denizenscript.denizen.nms.v1_16.impl.blocks.BlockLightImpl;
import com.denizenscript.denizen.nms.v1_16.impl.jnbt.CompoundTagImpl;
import com.denizenscript.denizen.nms.v1_16.impl.network.handlers.DenizenPacketListenerImpl;
import com.denizenscript.denizen.nms.util.jnbt.Tag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.packets.DenizenPacketHandler;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_16_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_16_R2.CraftServer;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftInventoryCustom;
import org.bukkit.craftbukkit.v1_16_R2.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_16_R2.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.spigotmc.AsyncCatcher;

import java.lang.reflect.Field;
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
        soundHelper = new SoundHelperImpl();
        packetHelper = new PacketHelperImpl();
        particleHelper = new ParticleHelperImpl();
        playerHelper = new PlayerHelperImpl();
        worldHelper = new WorldHelperImpl();
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
        return ((CraftMagicNumbers) CraftMagicNumbers.INSTANCE).getMappingsVersion().equals("09f04031f41cb54f1077c6ac348cc220");
    }

    @Override
    public double[] getRecentTps() {
        return ((CraftServer) Bukkit.getServer()).getServer().recentTps;
    }

    @Override
    public void enablePacketInterception(DenizenPacketHandler packetHandler) {
        DenizenPacketListenerImpl.enable(packetHandler);
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
                    gameProfile1 = minecraftServer.getUserCache().getProfile(gameProfile.getId());
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
            if (Debug.verbose) {
                Debug.echoError(e);
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
                Debug.echoError(e);
            }
        }
        return "Chest";
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
    public BiomeNMS getBiomeNMS(Biome biome) {
        return new BiomeNMSImpl(biome);
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
            ItemStack itemStack = new ItemStack(org.bukkit.Material.getMaterial(item.getId()), item.getCount());
            // TODO: Apply tag somehow
            return new ItemTag(itemStack).identify();
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

    public static BaseComponent[] componentToSpigot(IChatMutableComponent nms) {
        String json = IChatBaseComponent.ChatSerializer.a(nms);
        return ComponentSerializer.parse(json);
    }

    public static IChatMutableComponent componentToNMS(BaseComponent[] spigot) {
        String json = ComponentSerializer.toString(spigot);
        return IChatBaseComponent.ChatSerializer.b(json);
    }
}

package com.denizenscript.denizen.nms.v1_16;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.BiomeNMS;
import com.denizenscript.denizen.nms.abstracts.BlockLight;
import com.denizenscript.denizen.nms.abstracts.ProfileEditor;
import com.denizenscript.denizen.nms.abstracts.Sidebar;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.Tag;
import com.denizenscript.denizen.nms.v1_16.helpers.*;
import com.denizenscript.denizen.nms.v1_16.impl.BiomeNMSImpl;
import com.denizenscript.denizen.nms.v1_16.impl.ProfileEditorImpl;
import com.denizenscript.denizen.nms.v1_16.impl.SidebarImpl;
import com.denizenscript.denizen.nms.v1_16.impl.blocks.BlockLightImpl;
import com.denizenscript.denizen.nms.v1_16.impl.jnbt.CompoundTagImpl;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventoryCustom;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R3.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.spigotmc.AsyncCatcher;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
        return ((CraftMagicNumbers) CraftMagicNumbers.INSTANCE).getMappingsVersion().equals("d4b392244df170796f8779ef0fc1f2e9");
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
    public BiomeNMS getBiomeNMS(World world, String name) {
        for (Biome biome : Biome.values()) {
            if (biome.name().equalsIgnoreCase(name)) {
                return new BiomeNMSImpl(world, biome);
            }
        }
        return null;
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
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString("id", item.getId());
                tag.setByte("Count", (byte) item.getCount());
                if (item.getTag() != null && item.getTag().getNbt() != null) {
                    tag.set("tag", MojangsonParser.parse(item.getTag().getNbt()));
                }
                net.minecraft.server.v1_16_R3.ItemStack itemStack = net.minecraft.server.v1_16_R3.ItemStack.a(tag);
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
        NBTBase base = ((CraftPersistentDataContainer) container).getRaw().get(key);
        if (base instanceof NBTTagString) {
            return base.asString();
        }
        else if (base instanceof NBTTagByteArray) {
            return new String(((NBTTagByteArray) base).getBytes(), StandardCharsets.UTF_8);
        }
        return null;
    }

    public static BaseComponent[] componentToSpigot(IChatBaseComponent nms) {
        String json = IChatBaseComponent.ChatSerializer.a(nms);
        return ComponentSerializer.parse(json);
    }

    public static IChatMutableComponent componentToNMS(BaseComponent[] spigot) {
        String json = ComponentSerializer.toString(spigot);
        return IChatBaseComponent.ChatSerializer.b(json);
    }
}

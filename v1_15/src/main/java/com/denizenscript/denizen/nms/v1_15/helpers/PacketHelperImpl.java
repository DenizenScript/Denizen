package com.denizenscript.denizen.nms.v1_15.helpers;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.util.ReflectionHelper;
import com.denizenscript.denizen.nms.v1_15.impl.jnbt.CompoundTagImpl;
import com.denizenscript.denizen.nms.interfaces.PacketHelper;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.JNBTListTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.banner.Pattern;
import org.bukkit.craftbukkit.v1_15_R1.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftChatMessage;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class PacketHelperImpl implements PacketHelper {

    public static final DataWatcherObject<Float> ENTITY_HUMAN_DATA_WATCHER_ABSORPTION = ReflectionHelper.getFieldValue(EntityHuman.class, "c", null);

    @Override
    public void setFakeAbsorption(Player player, float value) {
        DataWatcher dw = new DataWatcher(null);
        dw.register(ENTITY_HUMAN_DATA_WATCHER_ABSORPTION, value);
        sendPacket(player, new PacketPlayOutEntityMetadata(player.getEntityId(), dw, true));
    }

    @Override
    public void resetWorldBorder(Player player) {
        WorldBorder wb = ((CraftWorld) player.getWorld()).getHandle().getWorldBorder();
        sendPacket(player, new PacketPlayOutWorldBorder(wb, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE));
    }

    @Override
    public void setWorldBorder(Player player, Location center, double size, double currSize, long time, int warningDistance, int warningTime) {
        WorldBorder wb = new WorldBorder();

        wb.world = ((CraftWorld) player.getWorld()).getHandle();
        wb.setCenter(center.getX(), center.getZ());
        wb.setWarningDistance(warningDistance);
        wb.setWarningTime(warningTime);

        if (time > 0) {
            wb.transitionSizeBetween(currSize, size, time);
        }
        else {
            wb.setSize(size);
        }

        sendPacket(player, new PacketPlayOutWorldBorder(wb, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE));
    }

    @Override
    public void setSlot(Player player, int slot, ItemStack itemStack, boolean playerOnly) {
        int windowId = playerOnly ? 0 : ((CraftPlayer) player).getHandle().activeContainer.windowId;
        sendPacket(player, new PacketPlayOutSetSlot(windowId, slot, CraftItemStack.asNMSCopy(itemStack)));
    }

    @Override
    public void setFieldOfView(Player player, float fov) {
        PacketPlayOutAbilities packet = new PacketPlayOutAbilities(((CraftPlayer) player).getHandle().abilities);
        if (!Float.isNaN(fov)) {
            packet.b(fov);
        }
        sendPacket(player, packet);
    }

    @Override
    public void respawn(Player player) {
        ((CraftPlayer) player).getHandle().playerConnection.a(
                new PacketPlayInClientCommand(PacketPlayInClientCommand.EnumClientCommand.PERFORM_RESPAWN));
    }

    @Override
    public void setVision(Player player, EntityType entityType) {
        final EntityLiving entity;
        if (entityType == EntityType.CREEPER) {
            entity = new EntityCreeper(EntityTypes.CREEPER, ((CraftWorld) player.getWorld()).getHandle());
        }
        else if (entityType == EntityType.SPIDER) {
            entity = new EntitySpider(EntityTypes.SPIDER, ((CraftWorld) player.getWorld()).getHandle());
        }
        else if (entityType == EntityType.CAVE_SPIDER) {
            entity = new EntityCaveSpider(EntityTypes.CAVE_SPIDER, ((CraftWorld) player.getWorld()).getHandle());
        }
        else if (entityType == EntityType.ENDERMAN) {
            entity = new EntityEnderman(EntityTypes.ENDERMAN, ((CraftWorld) player.getWorld()).getHandle());
        }
        else {
            return;
        }

        // Spectating an entity then immediately respawning the player prevents a client shader update,
        // allowing the player to retain whatever vision the mob they spectated had.
        sendPacket(player, new PacketPlayOutSpawnEntityLiving(entity));
        sendPacket(player, new PacketPlayOutCamera(entity));
        ((CraftServer) Bukkit.getServer()).getHandle().moveToWorld(((CraftPlayer) player).getHandle(),
                ((CraftWorld) player.getWorld()).getHandle().worldProvider.getDimensionManager(), true, player.getLocation(), false);
    }

    @Override
    public void showDemoScreen(Player player) {
        sendPacket(player, new PacketPlayOutGameStateChange(5, 0.0F));
    }

    @Override
    public void showBlockAction(Player player, Location location, int action, int state) {
        BlockPosition position = new BlockPosition(location.getX(), location.getY(), location.getZ());
        Block block = ((CraftWorld) location.getWorld()).getHandle().getType(position).getBlock();
        sendPacket(player, new PacketPlayOutBlockAction(position, block, action, state));
    }

    @Override
    public void showBlockCrack(Player player, int id, Location location, int progress) {
        BlockPosition position = new BlockPosition(location.getX(), location.getY(), location.getZ());
        sendPacket(player, new PacketPlayOutBlockBreakAnimation(id, position, progress));
    }

    @Override
    public void showTileEntityData(Player player, Location location, int action, CompoundTag compoundTag) {
        BlockPosition position = new BlockPosition(location.getX(), location.getY(), location.getZ());
        sendPacket(player, new PacketPlayOutTileEntityData(position, action, ((CompoundTagImpl) compoundTag).toNMSTag()));
    }

    @Override
    public void showBannerUpdate(Player player, Location location, DyeColor base, List<Pattern> patterns) {
        List<CompoundTag> nbtPatterns = new ArrayList<>();
        for (Pattern pattern : patterns) {
            nbtPatterns.add(NMSHandler.getInstance()
                    .createCompoundTag(new HashMap<>())
                    .createBuilder()
                    .putInt("Color", pattern.getColor().getDyeData())
                    .putString("Pattern", pattern.getPattern().getIdentifier())
                    .build());
        }
        CompoundTag compoundTag = NMSHandler.getBlockHelper().getNbtData(location.getBlock())
                .createBuilder()
                .put("Patterns", new JNBTListTag(CompoundTag.class, nbtPatterns))
                .build();
        showTileEntityData(player, location, 3, compoundTag);
    }

    @Override
    public void showTabListHeaderFooter(Player player, String header, String footer) {
        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
        packet.header = new ChatComponentText(header);
        packet.footer = new ChatComponentText(footer);
        sendPacket(player, packet);
    }

    @Override
    public void resetTabListHeaderFooter(Player player) {
        showTabListHeaderFooter(player, "", "");
    }

    @Override
    public void showTitle(Player player, String title, String subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        sendPacket(player, new PacketPlayOutTitle(fadeInTicks, stayTicks, fadeOutTicks));
        if (title != null) {
            sendPacket(player, new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, new ChatComponentText(title)));
        }
        if (subtitle != null) {
            sendPacket(player, new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, new ChatComponentText(subtitle)));
        }
    }

    @Override
    public void showEquipment(Player player, LivingEntity entity, EquipmentSlot equipmentSlot, ItemStack itemStack) {
        sendPacket(player, new PacketPlayOutEntityEquipment(entity.getEntityId(), CraftEquipmentSlot.getNMS(equipmentSlot), CraftItemStack.asNMSCopy(itemStack)));
    }

    @Override
    public void resetEquipment(Player player, LivingEntity entity) {
        EntityEquipment equipment = entity.getEquipment();
        showEquipment(player, entity, EquipmentSlot.HAND, equipment.getItemInMainHand());
        showEquipment(player, entity, EquipmentSlot.OFF_HAND, equipment.getItemInOffHand());
        showEquipment(player, entity, EquipmentSlot.HEAD, equipment.getHelmet());
        showEquipment(player, entity, EquipmentSlot.CHEST, equipment.getChestplate());
        showEquipment(player, entity, EquipmentSlot.LEGS, equipment.getLeggings());
        showEquipment(player, entity, EquipmentSlot.FEET, equipment.getBoots());
    }

    @Override
    public void openBook(Player player, EquipmentSlot hand) {
        sendPacket(player, new PacketPlayOutOpenBook(hand == EquipmentSlot.OFF_HAND ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND));
    }

    @Override
    public void showHealth(Player player, float health, int food, float saturation) {
        sendPacket(player, new PacketPlayOutUpdateHealth(health, food, saturation));
    }

    @Override
    public void resetHealth(Player player) {
        showHealth(player, (float) player.getHealth(), player.getFoodLevel(), player.getSaturation());
    }

    @Override
    public void showExperience(Player player, float experience, int level) {
        sendPacket(player, new PacketPlayOutExperience(experience, 0, level));
    }

    @Override
    public void resetExperience(Player player) {
        showExperience(player, player.getExp(), player.getLevel());
    }

    @Override
    public boolean showSignEditor(Player player, Location location) {
        TileEntity tileEntity = ((CraftWorld) location.getWorld()).getHandle().getTileEntity(new BlockPosition(location.getBlockX(),
                location.getBlockY(), location.getBlockZ()));
        if (tileEntity instanceof TileEntitySign) {
            TileEntitySign sign = (TileEntitySign) tileEntity;
            // Prevent client crashing by sending current state of the sign
            sendPacket(player, sign.getUpdatePacket());
            sign.isEditable = true;
            sign.a((EntityHuman) ((CraftPlayer) player).getHandle());
            sendPacket(player, new PacketPlayOutOpenSignEditor(sign.getPosition()));
            return true;
        }
        else {
            return false;
        }
    }

    public static MethodHandle ENTITY_METADATA_EID_SETTER = ReflectionHelper.getFinalSetter(PacketPlayOutEntityMetadata.class, "a");
    public static MethodHandle ENTITY_METADATA_LIST_SETTER = ReflectionHelper.getFinalSetter(PacketPlayOutEntityMetadata.class, "b");

    public static DataWatcherObject<Optional<IChatBaseComponent>> ENTITY_CUSTOM_NAME_METADATA;
    public static DataWatcherObject<Boolean> ENTITY_CUSTOM_NAME_VISIBLE_METADATA;

    static {
        try {
            ENTITY_CUSTOM_NAME_METADATA = ReflectionHelper.getFieldValue(net.minecraft.server.v1_15_R1.Entity.class, "az", null);
            ENTITY_CUSTOM_NAME_VISIBLE_METADATA = ReflectionHelper.getFieldValue(net.minecraft.server.v1_15_R1.Entity.class, "aA", null);
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void sendRename(Player player, Entity entity, String name) {
        try {
            PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata();
            ENTITY_METADATA_EID_SETTER.invoke(packet, entity.getEntityId());
            List<DataWatcher.Item<?>> list = new ArrayList<>();
            ChatComponentText text = new ChatComponentText("");
            for (IChatBaseComponent component : CraftChatMessage.fromString(name)) {
                text.addSibling(component);
            }
            list.add(new DataWatcher.Item<>(ENTITY_CUSTOM_NAME_METADATA, Optional.of(text)));
            list.add(new DataWatcher.Item<>(ENTITY_CUSTOM_NAME_VISIBLE_METADATA, true));
            ENTITY_METADATA_LIST_SETTER.invoke(packet, list);
            sendPacket(player, packet);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public void forceSpectate(Player player, Entity entity) {
        sendPacket(player, new PacketPlayOutCamera(((CraftEntity) entity).getHandle()));
    }

    public static void sendPacket(Player player, Packet packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}

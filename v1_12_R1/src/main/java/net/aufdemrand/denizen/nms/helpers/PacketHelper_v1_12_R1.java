package net.aufdemrand.denizen.nms.helpers;

import io.netty.buffer.Unpooled;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.impl.jnbt.CompoundTag_v1_12_R1;
import net.aufdemrand.denizen.nms.interfaces.PacketHelper;
import net.aufdemrand.denizen.nms.util.ReflectionHelper;
import net.aufdemrand.denizen.nms.util.jnbt.CompoundTag;
import net.aufdemrand.denizen.nms.util.jnbt.ListTag;
import net.aufdemrand.denizen.nms.util.jnbt.Tag;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.banner.Pattern;
import org.bukkit.craftbukkit.v1_12_R1.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PacketHelper_v1_12_R1 implements PacketHelper {

    @Override
    public void setFakeAbsorption(Player player, float value) {
        DataWatcher dw = new DataWatcher(null);
        dw.register(DataWatcherRegistry.c.a(11), value);
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
            entity = new EntityCreeper(((CraftWorld) player.getWorld()).getHandle());
        }
        else if (entityType == EntityType.SPIDER || entityType == EntityType.CAVE_SPIDER) {
            entity = new EntitySpider(((CraftWorld) player.getWorld()).getHandle());
        }
        else if (entityType == EntityType.ENDERMAN) {
            entity = new EntityEnderman(((CraftWorld) player.getWorld()).getHandle());
        }
        else {
            return;
        }

        // Spectating an entity then immediately respawning the player prevents a client shader update,
        // allowing the player to retain whatever vision the mob they spectated had.
        sendPacket(player, new PacketPlayOutSpawnEntityLiving(entity));
        sendPacket(player, new PacketPlayOutCamera(entity));
        ((CraftServer) Bukkit.getServer()).getHandle().moveToWorld(((CraftPlayer) player).getHandle(),
                ((CraftWorld) player.getWorld()).getHandle().dimension, true, player.getLocation(), false);
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
        sendPacket(player, new PacketPlayOutTileEntityData(position, action, ((CompoundTag_v1_12_R1) compoundTag).toNMSTag()));
    }

    @Override
    public void showBannerUpdate(Player player, Location location, DyeColor base, List<Pattern> patterns) {
        List<CompoundTag> nbtPatterns = new ArrayList<CompoundTag>();
        for (Pattern pattern : patterns) {
            nbtPatterns.add(NMSHandler.getInstance()
                    .createCompoundTag(new HashMap<String, Tag>())
                    .createBuilder()
                    .putInt("Color", pattern.getColor().getDyeData())
                    .putString("Pattern", pattern.getPattern().getIdentifier())
                    .build());
        }
        CompoundTag compoundTag = NMSHandler.getInstance().getBlockHelper().getNbtData(location.getBlock())
                .createBuilder()
                .putInt("Base", base.getDyeData())
                .put("Patterns", new ListTag(CompoundTag.class, nbtPatterns))
                .build();
        showTileEntityData(player, location, 3, compoundTag);
    }

    @Override
    public void showTabListHeaderFooter(Player player, String header, String footer) {
        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
        ReflectionHelper.setFieldValue(packet.getClass(), "a", packet, new ChatComponentText(header));
        ReflectionHelper.setFieldValue(packet.getClass(), "b", packet, new ChatComponentText(footer));
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
    public void sendActionBarMessage(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
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
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());
        serializer.a(hand == EquipmentSlot.OFF_HAND ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
        sendPacket(player, new PacketPlayOutCustomPayload("MC|BOpen", serializer));
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
        TileEntity tileEntity = ((CraftWorld) location.getWorld()).getTileEntityAt(location.getBlockX(),
                location.getBlockY(), location.getBlockZ());
        if (tileEntity instanceof TileEntitySign) {
            TileEntitySign sign = (TileEntitySign) tileEntity;
            // Prevent client crashing by sending current state of the sign
            sendPacket(player, sign.getUpdatePacket());
            sign.isEditable = true;
            sign.a(((CraftPlayer) player).getHandle());
            sendPacket(player, new PacketPlayOutOpenSignEditor(sign.getPosition()));
            return true;
        }
        else {
            return false;
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

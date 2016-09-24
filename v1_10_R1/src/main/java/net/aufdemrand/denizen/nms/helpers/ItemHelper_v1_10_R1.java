package net.aufdemrand.denizen.nms.helpers;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.aufdemrand.denizen.nms.impl.jnbt.CompoundTag_v1_10_R1;
import net.aufdemrand.denizen.nms.interfaces.ItemHelper;
import net.aufdemrand.denizen.nms.util.PlayerProfile;
import net.aufdemrand.denizen.nms.util.jnbt.CompoundTag;
import net.aufdemrand.denizen.nms.util.jnbt.Tag;
import net.minecraft.server.v1_10_R1.GameProfileSerializer;
import net.minecraft.server.v1_10_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class ItemHelper_v1_10_R1 implements ItemHelper {

    @Override
    public String getJsonString(ItemStack itemStack) {
        String json = CraftItemStack.asNMSCopy(itemStack).B().getChatModifier().toString().replace("\"", "\\\"");
        return json.substring(176, json.length() - 185);
    }

    @Override
    public PlayerProfile getSkullSkin(ItemStack is) {
        net.minecraft.server.v1_10_R1.ItemStack itemStack = CraftItemStack.asNMSCopy(is);
        if (itemStack.hasTag()) {
            NBTTagCompound tag = itemStack.getTag();
            if (tag.hasKeyOfType("SkullOwner", 10)) {
                GameProfile profile = GameProfileSerializer.deserialize(tag.getCompound("SkullOwner"));
                if (profile != null) {
                    Property property = Iterables.getFirst(profile.getProperties().get("textures"), null);
                    return new PlayerProfile(profile.getName(), profile.getId(),
                            property != null ? property.getValue() : null,
                            property != null ? property.getSignature() : null);
                }
            }
        }
        return null;
    }

    @Override
    public ItemStack setSkullSkin(ItemStack itemStack, PlayerProfile playerProfile) {
        GameProfile gameProfile = new GameProfile(playerProfile.getUniqueId(), playerProfile.getName());
        if (playerProfile.hasTexture()) {
            gameProfile.getProperties().put("textures",
                    new Property("value", playerProfile.getTexture(), playerProfile.getTextureSignature()));
        }
        net.minecraft.server.v1_10_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItemStack.hasTag() ? nmsItemStack.getTag() : new NBTTagCompound();
        tag.set("SkullOwner", GameProfileSerializer.serialize(new NBTTagCompound(), gameProfile));
        nmsItemStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    @Override
    public ItemStack addNbtData(ItemStack itemStack, String key, Tag value) {
        net.minecraft.server.v1_10_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItemStack.hasTag() ? nmsItemStack.getTag() : new NBTTagCompound();
        CompoundTag compound = CompoundTag_v1_10_R1.fromNMSTag(tag).createBuilder().put(key, value).build();
        nmsItemStack.setTag(((CompoundTag_v1_10_R1) compound).toNMSTag());
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    @Override
    public CompoundTag getNbtData(ItemStack itemStack) {
        net.minecraft.server.v1_10_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        if (nmsItemStack != null && nmsItemStack.hasTag()) {
            return CompoundTag_v1_10_R1.fromNMSTag(nmsItemStack.getTag());
        }
        return new CompoundTag_v1_10_R1(new HashMap<String, Tag>());
    }

    @Override
    public ItemStack setNbtData(ItemStack itemStack, CompoundTag compoundTag) {
        net.minecraft.server.v1_10_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        nmsItemStack.setTag(((CompoundTag_v1_10_R1) compoundTag).toNMSTag());
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }
}

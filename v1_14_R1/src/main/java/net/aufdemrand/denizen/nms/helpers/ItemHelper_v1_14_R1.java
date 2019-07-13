package net.aufdemrand.denizen.nms.helpers;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.aufdemrand.denizen.nms.enums.EntityAttribute;
import net.aufdemrand.denizen.nms.impl.jnbt.CompoundTag_v1_14_R1;
import net.aufdemrand.denizen.nms.interfaces.ItemHelper;
import net.aufdemrand.denizen.nms.util.EntityAttributeModifier;
import net.aufdemrand.denizen.nms.util.PlayerProfile;
import net.aufdemrand.denizen.nms.util.jnbt.*;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.minecraft.server.v1_14_R1.GameProfileSerializer;
import net.minecraft.server.v1_14_R1.NBTTagCompound;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class ItemHelper_v1_14_R1 implements ItemHelper {

    @Override
    public String getInternalNameFromMaterial(Material material) {
        // In 1.13+ Material names match their internal name
        return "minecraft:" + CoreUtilities.toLowerCase(material.name());
    }

    @Override
    public Material getMaterialFromInternalName(String internalName) {
        return Material.matchMaterial(internalName);
    }

    @Override
    public String getJsonString(ItemStack itemStack) {
        String json = CraftItemStack.asNMSCopy(itemStack).B().getChatModifier().toString().replace("\\", "\\\\").replace("\"", "\\\"");
        return json.substring(176, json.length() - 185);
    }

    @Override
    public PlayerProfile getSkullSkin(ItemStack is) {
        net.minecraft.server.v1_14_R1.ItemStack itemStack = CraftItemStack.asNMSCopy(is);
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
            gameProfile.getProperties().get("textures").clear();
            if (playerProfile.getTextureSignature() != null) {
                gameProfile.getProperties().put("textures", new Property("textures", playerProfile.getTexture(), playerProfile.getTextureSignature()));
            }
            else {
                gameProfile.getProperties().put("textures", new Property("textures", playerProfile.getTexture()));
            }
        }
        net.minecraft.server.v1_14_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItemStack.hasTag() ? nmsItemStack.getTag() : new NBTTagCompound();
        tag.set("SkullOwner", GameProfileSerializer.serialize(new NBTTagCompound(), gameProfile));
        nmsItemStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    @Override
    public ItemStack addNbtData(ItemStack itemStack, String key, Tag value) {
        net.minecraft.server.v1_14_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItemStack.hasTag() ? nmsItemStack.getTag() : new NBTTagCompound();
        CompoundTag compound = CompoundTag_v1_14_R1.fromNMSTag(tag).createBuilder().put(key, value).build();
        nmsItemStack.setTag(((CompoundTag_v1_14_R1) compound).toNMSTag());
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    @Override
    public CompoundTag getNbtData(ItemStack itemStack) {
        net.minecraft.server.v1_14_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        if (nmsItemStack != null && nmsItemStack.hasTag()) {
            return CompoundTag_v1_14_R1.fromNMSTag(nmsItemStack.getTag());
        }
        return new CompoundTag_v1_14_R1(new HashMap<>());
    }

    @Override
    public ItemStack setNbtData(ItemStack itemStack, CompoundTag compoundTag) {
        net.minecraft.server.v1_14_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        nmsItemStack.setTag(((CompoundTag_v1_14_R1) compoundTag).toNMSTag());
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    @Override
    public ItemStack setAttributeModifiers(ItemStack itemStack, Map<EntityAttribute, List<EntityAttributeModifier>> modifiers) {
        List<Tag> modifierList = new ArrayList<>(getNbtData(itemStack).getList("AttributeModifiers"));
        for (Map.Entry<EntityAttribute, List<EntityAttributeModifier>> entry : modifiers.entrySet()) {
            EntityAttribute attribute = entry.getKey();
            for (EntityAttributeModifier modifier : entry.getValue()) {
                Map<String, Tag> compound = new HashMap<>();
                compound.put("AttributeName", new StringTag(attribute.getName()));
                UUID uuid = modifier.getUniqueId();
                compound.put("UUIDMost", new LongTag(uuid.getMostSignificantBits()));
                compound.put("UUIDLeast", new LongTag(uuid.getLeastSignificantBits()));
                compound.put("Name", new StringTag(modifier.getName()));
                compound.put("Operation", new IntTag(modifier.getOperation().ordinal()));
                compound.put("Amount", new DoubleTag(modifier.getAmount()));
                modifierList.add(new CompoundTag_v1_14_R1(compound));
            }
        }
        return addNbtData(itemStack, "AttributeModifiers", new ListTag(CompoundTag.class, modifierList));
    }

    @Override
    public PotionEffect getPotionEffect(PotionEffectType type, int duration, int amplifier, boolean ambient, boolean particles, Color color, boolean icon) {
        return new PotionEffect(type, duration, amplifier, ambient, particles, icon);
    }
}

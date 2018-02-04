package net.aufdemrand.denizen.nms.helpers;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.aufdemrand.denizen.nms.enums.EntityAttribute;
import net.aufdemrand.denizen.nms.impl.jnbt.CompoundTag_v1_12_R1;
import net.aufdemrand.denizen.nms.interfaces.ItemHelper;
import net.aufdemrand.denizen.nms.util.EntityAttributeModifier;
import net.aufdemrand.denizen.nms.util.PlayerProfile;
import net.aufdemrand.denizen.nms.util.jnbt.*;
import net.minecraft.server.v1_12_R1.GameProfileSerializer;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ItemHelper_v1_12_R1 implements ItemHelper {

    @Override
    public String getVanillaName(ItemStack itemStack) {
        return CraftItemStack.asNMSCopy(itemStack).save(new NBTTagCompound()).getString("id");
    }

    @Override
    public String getJsonString(ItemStack itemStack) {
        String json = CraftItemStack.asNMSCopy(itemStack).C().getChatModifier().toString().replace("\"", "\\\"");
        return json.substring(176, json.length() - 185);
    }

    @Override
    public PlayerProfile getSkullSkin(ItemStack is) {
        net.minecraft.server.v1_12_R1.ItemStack itemStack = CraftItemStack.asNMSCopy(is);
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
        net.minecraft.server.v1_12_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItemStack.hasTag() ? nmsItemStack.getTag() : new NBTTagCompound();
        tag.set("SkullOwner", GameProfileSerializer.serialize(new NBTTagCompound(), gameProfile));
        nmsItemStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    @Override
    public ItemStack addNbtData(ItemStack itemStack, String key, Tag value) {
        net.minecraft.server.v1_12_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItemStack.hasTag() ? nmsItemStack.getTag() : new NBTTagCompound();
        CompoundTag compound = CompoundTag_v1_12_R1.fromNMSTag(tag).createBuilder().put(key, value).build();
        nmsItemStack.setTag(((CompoundTag_v1_12_R1) compound).toNMSTag());
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    @Override
    public CompoundTag getNbtData(ItemStack itemStack) {
        net.minecraft.server.v1_12_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        if (nmsItemStack != null && nmsItemStack.hasTag()) {
            return CompoundTag_v1_12_R1.fromNMSTag(nmsItemStack.getTag());
        }
        return new CompoundTag_v1_12_R1(new HashMap<String, Tag>());
    }

    @Override
    public ItemStack setNbtData(ItemStack itemStack, CompoundTag compoundTag) {
        net.minecraft.server.v1_12_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        nmsItemStack.setTag(((CompoundTag_v1_12_R1) compoundTag).toNMSTag());
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    @Override
    public Map<EntityAttribute, List<EntityAttributeModifier>> getAttributeModifiers(ItemStack itemStack) {
        Map<EntityAttribute, List<EntityAttributeModifier>> modifiers = new HashMap<EntityAttribute, List<EntityAttributeModifier>>();
        List<Tag> modifierList = getNbtData(itemStack).getList("AttributeModifiers");
        for (Tag tag : modifierList) {
            if (!(tag instanceof CompoundTag)) {
                continue;
            }
            CompoundTag modifier = (CompoundTag) tag;
            EntityAttribute attribute = EntityAttribute.getByName(modifier.getString("AttributeName"));
            if (attribute == null) {
                continue;
            }
            if (!modifiers.containsKey(attribute)) {
                modifiers.put(attribute, new ArrayList<EntityAttributeModifier>());
            }
            UUID uuid = new UUID(modifier.getLong("UUIDMost"), modifier.getLong("UUIDLeast"));
            String name = modifier.getString("Name");
            EntityAttributeModifier.Operation operation = EntityAttributeModifier.Operation.values()[modifier.getInt("Operation")];
            if (operation == null) {
                continue;
            }
            double amount = modifier.getDouble("Amount");
            modifiers.get(attribute).add(new EntityAttributeModifier(uuid, name, operation, amount));
        }
        return modifiers;
    }

    @Override
    public ItemStack setAttributeModifiers(ItemStack itemStack, Map<EntityAttribute, List<EntityAttributeModifier>> modifiers) {
        List<Tag> modifierList = new ArrayList<Tag>(getNbtData(itemStack).getList("AttributeModifiers"));
        for (Map.Entry<EntityAttribute, List<EntityAttributeModifier>> entry : modifiers.entrySet()) {
            EntityAttribute attribute = entry.getKey();
            for (EntityAttributeModifier modifier : entry.getValue()) {
                Map<String, Tag> compound = new HashMap<String, Tag>();
                compound.put("AttributeName", new StringTag(attribute.getName()));
                UUID uuid = modifier.getUniqueId();
                compound.put("UUIDMost", new LongTag(uuid.getMostSignificantBits()));
                compound.put("UUIDLeast", new LongTag(uuid.getLeastSignificantBits()));
                compound.put("Name", new StringTag(modifier.getName()));
                compound.put("Operation", new IntTag(modifier.getOperation().ordinal()));
                compound.put("Amount", new DoubleTag(modifier.getAmount()));
                modifierList.add(new CompoundTag_v1_12_R1(compound));
            }
        }
        return addNbtData(itemStack, "AttributeModifiers", new ListTag(CompoundTag.class, modifierList));
    }
}

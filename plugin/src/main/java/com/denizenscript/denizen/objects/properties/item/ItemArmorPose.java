package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.FloatBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemArmorPose extends ItemProperty<MapTag> {

    // <--[property]
    // @object ItemTag
    // @name armor_pose
    // @input MapTag
    // @description
    // Controls the pose of this armor stand item.
    // Allowed keys: head, body, left_arm, right_arm, left_leg, right_leg
    // -->

    public static boolean describes(ItemTag item) {
        return item.getBukkitMaterial() == Material.ARMOR_STAND;
    }

    @Override
    public MapTag getPropertyValue() {
        CompoundBinaryTag entityNbt = NMSHandler.itemHelper.getEntityData(getItemStack());
        if (entityNbt == null) {
            return null;
        }
        CompoundBinaryTag pose = entityNbt.getCompound("Pose", null);
        if (pose == null) {
            return null;
        }
        MapTag result = new MapTag();
        procPart(pose, "Head", "head", result);
        procPart(pose, "Body", "body", result);
        procPart(pose, "LeftArm", "left_arm", result);
        procPart(pose, "RightArm", "right_arm", result);
        procPart(pose, "LeftLeg", "left_leg", result);
        procPart(pose, "RightLeg", "right_leg", result);
        return result;
    }

    @Override
    public void setPropertyValue(MapTag param, Mechanism mechanism) {
        CompoundBinaryTag entityNbt = NMSHandler.itemHelper.getEntityData(getItemStack());
        if (mechanism.hasValue()) {
            if (entityNbt == null) {
                entityNbt = CompoundBinaryTag.empty();
            }
            CompoundBinaryTag.Builder poseBuilder = CompoundBinaryTag.builder();
            procMechKey(mechanism, poseBuilder, "Head", "head", param);
            procMechKey(mechanism, poseBuilder, "Body", "body", param);
            procMechKey(mechanism, poseBuilder, "LeftArm", "left_arm", param);
            procMechKey(mechanism, poseBuilder, "RightArm", "right_arm", param);
            procMechKey(mechanism, poseBuilder, "LeftLeg", "left_leg", param);
            procMechKey(mechanism, poseBuilder, "RightLeg", "right_leg", param);
            CompoundBinaryTag pose = poseBuilder.build();
            if (pose.isEmpty()) {
                entityNbt = entityNbt.remove("Pose");
            }
            else {
                entityNbt = entityNbt.put("Pose", pose);
            }
        }
        else {
            if (entityNbt == null) {
                return;
            }
            // TODO: adventure-nbt: contains
            if (!(entityNbt.get("Pose") instanceof CompoundBinaryTag)) {
                return;
            }
            entityNbt = entityNbt.remove("Pose");
        }
        ItemStack result = NMSHandler.itemHelper.setEntityData(getItemStack(), entityNbt, EntityType.ARMOR_STAND);
        setItemStack(result);
    }

    @Override
    public String getPropertyId() {
        return "armor_pose";
    }

    public static void procPart(CompoundBinaryTag pose, String nmsName, String denizenName, MapTag result) {
        ListBinaryTag list = pose.getList(nmsName, BinaryTagTypes.FLOAT, null);
        if (list == null || list.size() != 3) {
            return;
        }
        String combined = list.getFloat(0) + "," + list.getFloat(1) + "," + list.getFloat(2);
        result.putObject(denizenName, new ElementTag(combined));
    }

    public static void register() {
        autoRegister("armor_pose", ItemArmorPose.class, MapTag.class, false);
    }

    public static void procMechKey(Mechanism mech, CompoundBinaryTag.Builder poseBuilder, String nmsName, String denizenName, MapTag input) {
        ObjectTag value = input.getObject(denizenName);
        if (value == null) {
            return;
        }
        List<String> raw = CoreUtilities.split(value.toString(), ',');
        if (raw.size() != 3) {
            mech.echoError("Invalid pose piece '" + value + "'");
            return;
        }
        ListBinaryTag.Builder<FloatBinaryTag> listBuilder = ListBinaryTag.builder(BinaryTagTypes.FLOAT);
        for (int i = 0; i < 3; i++) {
            listBuilder.add(FloatBinaryTag.floatBinaryTag(Float.parseFloat(raw.get(i))));
        }
        poseBuilder.put(nmsName, listBuilder.build());
    }
}

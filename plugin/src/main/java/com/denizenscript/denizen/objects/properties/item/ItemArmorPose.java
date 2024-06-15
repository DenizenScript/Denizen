package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.util.jnbt.*;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
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
        CompoundTag entityNbt = NMSHandler.itemHelper.getEntityData(getItemStack());
        if (entityNbt == null) {
            return null;
        }
        Tag posePart = entityNbt.getValue().get("Pose");
        if (!(posePart instanceof CompoundTag)) {
            return null;
        }
        CompoundTag pose = (CompoundTag) posePart;
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
        CompoundTag entityNbt = NMSHandler.itemHelper.getEntityData(getItemStack());
        if (mechanism.hasValue()) {
            if (entityNbt == null) {
                entityNbt = new CompoundTagBuilder().build();
            }
            CompoundTagBuilder poseBuilder = new CompoundTagBuilder();
            procMechKey(mechanism, poseBuilder, "Head", "head", param);
            procMechKey(mechanism, poseBuilder, "Body", "body", param);
            procMechKey(mechanism, poseBuilder, "LeftArm", "left_arm", param);
            procMechKey(mechanism, poseBuilder, "RightArm", "right_arm", param);
            procMechKey(mechanism, poseBuilder, "LeftLeg", "left_leg", param);
            procMechKey(mechanism, poseBuilder, "RightLeg", "right_leg", param);
            CompoundTag pose = poseBuilder.build();
            if (pose.getValue().isEmpty()) {
                entityNbt = entityNbt.createBuilder().remove("Pose").build();
            }
            else {
                entityNbt = entityNbt.createBuilder().put("Pose", pose).build();
            }
        }
        else {
            if (entityNbt == null) {
                return;
            }
            if (!(entityNbt.getValue().get("Pose") instanceof CompoundTag)) {
                return;
            }
            entityNbt = entityNbt.createBuilder().remove("Pose").build();
        }
        ItemStack result = NMSHandler.itemHelper.setEntityData(getItemStack(), entityNbt, EntityType.ARMOR_STAND);
        setItemStack(result);
    }

    @Override
    public String getPropertyId() {
        return "armor_pose";
    }

    public static void procPart(CompoundTag pose, String nmsName, String denizenName, MapTag result) {
        List<Tag> list = pose.getList(nmsName);
        if (list == null || list.size() != 3) {
            return;
        }
        Tag x = list.get(0), y = list.get(1), z = list.get(2);
        if (!(x instanceof FloatTag) || !(y instanceof FloatTag) || !(z instanceof FloatTag)) {
            return;
        }
        String combined = x.getValue() + "," + y.getValue() + "," + z.getValue();
        result.putObject(denizenName, new ElementTag(combined));
    }

    public static void register() {
        autoRegister("armor_pose", ItemArmorPose.class, MapTag.class, false);
    }

    public static void procMechKey(Mechanism mech, CompoundTagBuilder pose, String nmsName, String denizenName, MapTag input) {
        ObjectTag value = input.getObject(denizenName);
        if (value == null) {
            return;
        }
        List<String> raw = CoreUtilities.split(value.toString(), ',');
        if (raw.size() != 3) {
            mech.echoError("Invalid pose piece '" + value + "'");
            return;
        }
        List<FloatTag> rawList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            rawList.add(new FloatTag(Float.parseFloat(raw.get(i))));
        }
        JNBTListTag list = new JNBTListTag(FloatTag.class, rawList);
        pose.put(nmsName, list);
    }
}

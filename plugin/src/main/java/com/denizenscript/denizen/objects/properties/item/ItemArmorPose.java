package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.util.jnbt.*;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemArmorPose extends ItemProperty {

    public static boolean describes(ItemTag item) {
        return item.getBukkitMaterial() == Material.ARMOR_STAND;
    }

    @Override
    public String getPropertyString() {
        MapTag result = getPoseMap();
        if (result == null) {
            return null;
        }
        return  result.toString();
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

    public MapTag getPoseMap() {
        CompoundTag compoundTag = NMSHandler.itemHelper.getNbtData(getItemStack());
        if (compoundTag == null) {
            return null;
        }
        Tag entPart = compoundTag.getValue().get("EntityTag");
        if (!(entPart instanceof CompoundTag)) {
            return null;
        }
        Tag posePart = ((CompoundTag) entPart).getValue().get("Pose");
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

    public static void register() {

        // <--[tag]
        // @attribute <ItemTag.armor_pose>
        // @returns MapTag
        // @group properties
        // @mechanism ItemTag.armor_pose
        // @description
        // Returns the pose of this armor stand item, if any.
        // Map has keys: head, body, left_arm, right_arm, left_leg, right_leg
        // -->
        PropertyParser.registerTag(ItemArmorPose.class, MapTag.class, "armor_pose", (attribute, prop) -> {
            return prop.getPoseMap();
        });

        // <--[mechanism]
        // @object ItemTag
        // @name armor_pose
        // @input MapTag
        // @description
        // Sets the pose of this armor stand item.
        // Allowed keys: head, body, left_arm, right_arm, left_leg, right_leg
        // @tags
        // <ItemTag.armor_pose>
        // -->
        PropertyParser.registerMechanism(ItemArmorPose.class, MapTag.class, "armor_pose", (prop, mechanism, param) -> {
            CompoundTag compoundTag = NMSHandler.itemHelper.getNbtData(prop.getItemStack());
            Tag entPart, posePart;
            if (mechanism.hasValue()) {
                if (compoundTag == null) {
                    compoundTag = new CompoundTagBuilder().build();
                }
                entPart = compoundTag.getValue().get("EntityTag");
                if (!(entPart instanceof CompoundTag)) {
                    entPart = new CompoundTagBuilder().build();
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
                    entPart = ((CompoundTag) entPart).createBuilder().remove("Pose").build();
                }
                else {
                    entPart = ((CompoundTag) entPart).createBuilder().put("Pose", pose).build();
                }
            }
            else {
                if (compoundTag == null) {
                    return;
                }
                entPart = compoundTag.getValue().get("EntityTag");
                if (!(entPart instanceof CompoundTag)) {
                    return;
                }
                posePart = ((CompoundTag) entPart).getValue().get("Pose");
                if (!(posePart instanceof CompoundTag)) {
                    return;
                }
                entPart = ((CompoundTag) entPart).createBuilder().remove("Pose").build();
            }
            if (((CompoundTag) entPart).getValue().isEmpty()) {
                compoundTag = compoundTag.createBuilder().remove("EntityTag").build();
            }
            else {
                compoundTag = compoundTag.createBuilder().put("EntityTag", entPart).build();
            }
            ItemStack result = NMSHandler.itemHelper.setNbtData(prop.getItemStack(), compoundTag);
            prop.setItemStack(result);
        });
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

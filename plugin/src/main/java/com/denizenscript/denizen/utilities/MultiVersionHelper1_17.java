package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.properties.entity.EntityColor;
import com.denizenscript.denizen.objects.properties.material.MaterialBlockType;
import com.denizenscript.denizen.objects.properties.material.MaterialMode;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Vibration;
import org.bukkit.block.data.type.BigDripleaf;
import org.bukkit.block.data.type.PointedDripstone;
import org.bukkit.block.data.type.SculkSensor;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Goat;

public class MultiVersionHelper1_17 { // TODO: 1.17

    public static boolean colorIsApplicable(EntityType type) {
        return type == EntityType.GOAT || type == EntityType.AXOLOTL;
    }

    public static String getColor(Entity entity) {
        if (entity instanceof Goat) {
            return ((Goat) entity).isScreaming() ? "screaming" : "normal";
        }
        else if (entity instanceof Axolotl) {
            return ((Axolotl) entity).getVariant().name();
        }
        return null;
    }

    public static ListTag getAllowedColors(EntityType type) {
        if (type == EntityType.GOAT) {
            ListTag result = new ListTag();
            result.add("screaming");
            result.add("normal");
            return result;
        }
        else if (type == EntityType.AXOLOTL) {
            return EntityColor.listForEnum(Axolotl.Variant.values());
        }
        return null;
    }

    public static void setColor(Entity entity, Mechanism mech) {
        if (entity instanceof Goat) {
            ((Goat) entity).setScreaming(CoreUtilities.toLowerCase(mech.getValue().asString()).equals("screaming"));
        }
        else if (entity instanceof Axolotl && mech.requireEnum(Axolotl.Variant.class)) {
            ((Axolotl) entity).setVariant(Axolotl.Variant.valueOf(mech.getValue().asString().toUpperCase()));
        }
    }

    public static void materialBlockTypeRunMech(Mechanism mechanism, MaterialBlockType object) {
        if (object.isDripstone() && mechanism.requireEnum(PointedDripstone.Thickness.class)) {
            ((PointedDripstone) object.material.getModernData()).setThickness(PointedDripstone.Thickness.valueOf(mechanism.getValue().asString().toUpperCase()));
        }
    }

    public static void materialModeRunMech(Mechanism mechanism, MaterialMode object) {
        if (object.isSculkSensor() && mechanism.requireEnum(SculkSensor.Phase.class)) {
            ((SculkSensor) object.material.getModernData()).setPhase(SculkSensor.Phase.valueOf(mechanism.getValue().asString().toUpperCase()));
        }
        else if (object.isBigDripleaf() && mechanism.requireEnum(BigDripleaf.Tilt.class)) {
            ((BigDripleaf) object.material.getModernData()).setTilt(BigDripleaf.Tilt.valueOf(mechanism.getValue().asString().toUpperCase()));
        }
    }

    public static Object getPlayEffectVibrationObject(ListTag dataList, ScriptEntry scriptEntry) {
        DurationTag duration = dataList.getObject(0).asType(DurationTag.class, scriptEntry.context);
        LocationTag origin = dataList.getObject(1).asType(LocationTag.class, scriptEntry.context);
        ObjectTag destination = dataList.getObject(2);
        Vibration.Destination destObj;
        if (destination.shouldBeType(EntityTag.class)) {
            destObj = new Vibration.Destination.EntityDestination(destination.asType(EntityTag.class, scriptEntry.context).getBukkitEntity());
        }
        else {
            destObj = new Vibration.Destination.BlockDestination(destination.asType(LocationTag.class, scriptEntry.context));
        }
        return new Vibration(origin, destObj, duration.getTicksAsInt());
    }
}

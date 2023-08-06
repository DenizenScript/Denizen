package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.EulerAngle;

import java.util.Iterator;
import java.util.Map;

public class EntityArmorPose implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof ArmorStand;
    }

    public static EntityArmorPose getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityArmorPose((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "armor_pose"
    };

    public EntityArmorPose(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return getPoseMap().identify();
    }

    @Override
    public String getPropertyId() {
        return "armor_pose";
    }

    public ListTag getPoseList() {
        ArmorStand armorStand = (ArmorStand) entity.getBukkitEntity();
        ListTag list = new ListTag();
        for (PosePart posePart : PosePart.values()) {
            list.add(CoreUtilities.toLowerCase(posePart.name()));
            list.addObject(fromEulerAngle(posePart.getAngle(armorStand)));
        }
        return list;
    }

    public MapTag getPoseMap() {
        ArmorStand armorStand = (ArmorStand) entity.getBukkitEntity();
        MapTag map = new MapTag();
        for (PosePart posePart : PosePart.values()) {
            map.putObject(CoreUtilities.toLowerCase(posePart.name()), fromEulerAngle(posePart.getAngle(armorStand)));
        }
        return map;
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.armor_pose_map>
        // @returns MapTag
        // @mechanism EntityTag.armor_pose
        // @group attributes
        // @description
        // Returns a map of all poses and angles for the armor stand.
        // For example, head=4.5,3,4.5;body=5.4,3.2,1
        // Angles are in radians!
        // -->
        PropertyParser.registerTag(EntityArmorPose.class, MapTag.class, "armor_pose_map", (attribute, entity) -> {
            return entity.getPoseMap();
        });

        // <--[tag]
        // @attribute <EntityTag.armor_pose_list>
        // @returns ListTag
        // @mechanism EntityTag.armor_pose
        // @deprecated Use 'armor_pose_map'
        // @group attributes
        // @description
        // Deprecated in favor of <@link tag EntityTag.armor_pose_map>
        // -->
        PropertyParser.registerTag(EntityArmorPose.class, ListTag.class, "armor_pose_list", (attribute, entity) -> {
            BukkitImplDeprecations.entityArmorPose.warn(attribute.context);
            return entity.getPoseList();
        });

        // <--[tag]
        // @attribute <EntityTag.armor_pose>
        // @returns LocationTag
        // @mechanism EntityTag.armor_pose
        // @deprecated Use 'armor_pose_map'
        // @group attributes
        // @description
        // Deprecated in favor of <@link tag EntityTag.armor_pose_map>
        // -->
        PropertyParser.registerTag(EntityArmorPose.class, LocationTag.class, "armor_pose", (attribute, entity) -> {
            BukkitImplDeprecations.entityArmorPose.warn(attribute.context);
            if (!attribute.hasParam()) {
                return null;
            }
            String name = attribute.getParam();
            PosePart posePart = PosePart.fromName(name);
            if (posePart == null) {
                attribute.echoError("Invalid pose part specified: " + name);
                return null;
            }
            else {
                return fromEulerAngle(posePart.getAngle((ArmorStand) entity.entity.getBukkitEntity()));
            }
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name armor_pose
        // @input MapTag
        // @description
        // Sets the angle for various parts of the armor stand.
        // For example, [head=4.5,3,4.5;body=5.4,3.2,1]
        // Valid parts: HEAD, BODY, LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG
        // Angles are in radians!
        // Here's a website to help you figure out the correct values: <@link url https://b-universe.github.io/Minecraft-ArmorStand/>.
        // @tags
        // <EntityTag.armor_pose_map>
        // -->
        if (mechanism.matches("armor_pose")) {
            ArmorStand armorStand = (ArmorStand) entity.getBukkitEntity();
            if (mechanism.getValue().asString().contains("|")) { // legacy format
                ListTag list = mechanism.valueAsType(ListTag.class);
                Iterator<String> iterator = list.iterator();
                while (iterator.hasNext()) {
                    String name = iterator.next();
                    String angle = iterator.next();
                    PosePart posePart = PosePart.fromName(name);
                    if (posePart == null) {
                        mechanism.echoError("Invalid pose part specified: " + name + "; ignoring next: " + angle);
                    }
                    else {
                        posePart.setAngle(armorStand, toEulerAngle(LocationTag.valueOf(angle, mechanism.context)));
                    }
                }
            }
            else {
                MapTag map = mechanism.valueAsType(MapTag.class);
                for (Map.Entry<StringHolder, ObjectTag> entry : map.entrySet()) {
                    PosePart posePart = PosePart.fromName(entry.getKey().str);
                    if (posePart == null) {
                        mechanism.echoError("Invalid pose part specified: " + entry.getKey().str);
                    }
                    else {
                        posePart.setAngle(armorStand, toEulerAngle(entry.getValue().asType(LocationTag.class, mechanism.context)));
                    }
                }
            }
        }
    }

    public static LocationTag fromEulerAngle(EulerAngle eulerAngle) {
        return new LocationTag(null, eulerAngle.getX(), eulerAngle.getY(), eulerAngle.getZ());
    }

    public static EulerAngle toEulerAngle(LocationTag location) {
        return new EulerAngle(location.getX(), location.getY(), location.getZ());
    }

    public enum PosePart {
        HEAD {
            @Override
            EulerAngle getAngle(ArmorStand armorStand) {
                return armorStand.getHeadPose();
            }

            @Override
            void setAngle(ArmorStand armorStand, EulerAngle eulerAngle) {
                armorStand.setHeadPose(eulerAngle);
            }
        },
        BODY {
            @Override
            EulerAngle getAngle(ArmorStand armorStand) {
                return armorStand.getBodyPose();
            }

            @Override
            void setAngle(ArmorStand armorStand, EulerAngle eulerAngle) {
                armorStand.setBodyPose(eulerAngle);
            }
        },
        LEFT_ARM {
            @Override
            EulerAngle getAngle(ArmorStand armorStand) {
                return armorStand.getLeftArmPose();
            }

            @Override
            void setAngle(ArmorStand armorStand, EulerAngle eulerAngle) {
                armorStand.setLeftArmPose(eulerAngle);
            }
        },
        RIGHT_ARM {
            @Override
            EulerAngle getAngle(ArmorStand armorStand) {
                return armorStand.getRightArmPose();
            }

            @Override
            void setAngle(ArmorStand armorStand, EulerAngle eulerAngle) {
                armorStand.setRightArmPose(eulerAngle);
            }
        },
        LEFT_LEG {
            @Override
            EulerAngle getAngle(ArmorStand armorStand) {
                return armorStand.getLeftLegPose();
            }

            @Override
            void setAngle(ArmorStand armorStand, EulerAngle eulerAngle) {
                armorStand.setLeftLegPose(eulerAngle);
            }
        },
        RIGHT_LEG {
            @Override
            EulerAngle getAngle(ArmorStand armorStand) {
                return armorStand.getRightLegPose();
            }

            @Override
            void setAngle(ArmorStand armorStand, EulerAngle eulerAngle) {
                armorStand.setRightLegPose(eulerAngle);
            }
        };

        abstract EulerAngle getAngle(ArmorStand armorStand);

        abstract void setAngle(ArmorStand armorStand, EulerAngle eulerAngle);

        static PosePart fromName(String name) {
            for (PosePart posePart : PosePart.values()) {
                if (posePart.name().equalsIgnoreCase(name)) {
                    return posePart;
                }
            }
            return null;
        }
    }
}

package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.util.EulerAngle;

import java.util.Iterator;

public class EntityArmorPose implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntityType() == EntityType.ARMOR_STAND;
    }

    public static EntityArmorPose getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityArmorPose((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "armor_pose_list", "armor_pose"
    };

    public static final String[] handledMechs = new String[] {
            "armor_pose"
    };

    private EntityArmorPose(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return getPoseList().identify();
    }

    @Override
    public String getPropertyId() {
        return "armor_pose";
    }

    private ListTag getPoseList() {
        ArmorStand armorStand = (ArmorStand) entity.getBukkitEntity();
        ListTag list = new ListTag();
        for (PosePart posePart : PosePart.values()) {
            list.add(CoreUtilities.toLowerCase(posePart.name()));
            list.addObject(fromEulerAngle(posePart.getAngle(armorStand)));
        }
        return list;
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.armor_pose_list>
        // @returns ListTag
        // @mechanism EntityTag.armor_pose
        // @group attributes
        // @description
        // Returns a list of all poses and angles for the armor stand in the
        // format: PART|ANGLE|...
        // For example, head|4.5,3,4.5|body|5.4,3.2,1
        // Angles are in radians!
        // -->
        if (attribute.startsWith("armor_pose_list")) {
            return getPoseList().getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.armor_pose[<part>]>
        // @returns LocationTag
        // @mechanism EntityTag.armor_pose
        // @group attributes
        // @description
        // Returns the current angle pose for the specified part.
        // Valid parts: HEAD, BODY, LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG
        // Angles are in radians!
        // -->
        else if (attribute.startsWith("armor_pose") && attribute.hasContext(1)) {
            String name = attribute.getContext(1);
            PosePart posePart = PosePart.fromName(name);
            if (posePart == null) {
                Debug.echoError("Invalid pose part specified: " + name);
                return null;
            }
            else {
                return fromEulerAngle(posePart.getAngle((ArmorStand) entity.getBukkitEntity()))
                        .getObjectAttribute(attribute.fulfill(1));
            }
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name armor_pose
        // @input ListTag
        // @description
        // Sets the angle for various parts of the armor stand in the
        // format: PART|ANGLE|...
        // For example, head|4.5,3,4.5|body|5.4,3.2,1
        // Valid parts: HEAD, BODY, LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG
        // Angles are in radians!
        // @tags
        // <EntityTag.armor_pose_list>
        // <EntityTag.armor_pose[<part>]>
        // -->
        if (mechanism.matches("armor_pose")) {
            ArmorStand armorStand = (ArmorStand) entity.getBukkitEntity();
            ListTag list = mechanism.valueAsType(ListTag.class);
            Iterator<String> iterator = list.iterator();
            while (iterator.hasNext()) {
                String name = iterator.next();
                String angle = iterator.next();
                PosePart posePart = PosePart.fromName(name);
                if (posePart == null) {
                    Debug.echoError("Invalid pose part specified: " + name + "; ignoring next: " + angle);
                }
                else {
                    posePart.setAngle(armorStand, toEulerAngle(LocationTag.valueOf(angle, mechanism.context)));
                }
            }
        }
    }

    private static LocationTag fromEulerAngle(EulerAngle eulerAngle) {
        return new LocationTag(null, eulerAngle.getX(), eulerAngle.getY(), eulerAngle.getZ());
    }

    private static EulerAngle toEulerAngle(LocationTag location) {
        return new EulerAngle(location.getX(), location.getY(), location.getZ());
    }

    private enum PosePart {
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

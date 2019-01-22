package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.util.EulerAngle;

import java.util.Iterator;

public class EntityArmorPose implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity
                && ((dEntity) entity).getBukkitEntityType() == EntityType.ARMOR_STAND;
    }

    public static EntityArmorPose getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityArmorPose((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[]{
            "armor_pose_list", "armor_pose"
    };

    public static final String[] handledMechs = new String[] {
            "armor_pose"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityArmorPose(dEntity ent) {
        entity = ent;
    }

    dEntity entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return getPoseList().identify();
    }

    @Override
    public String getPropertyId() {
        return "armor_pose";
    }

    private dList getPoseList() {
        ArmorStand armorStand = (ArmorStand) entity.getBukkitEntity();
        dList list = new dList();
        for (PosePart posePart : PosePart.values()) {
            list.add(CoreUtilities.toLowerCase(posePart.name()));
            list.add(fromEulerAngle(posePart.getAngle(armorStand)).identify());
        }
        return list;
    }

    ///////////
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <e@entity.armor_pose_list>
        // @returns dList
        // @group attributes
        // @description
        // Returns a list of all poses and angles for the armor stand in the
        // format: PART|ANGLE|...
        // For example, head|4.5,3,4.5|body|5.4,3.2,1
        // Angles are in radians!
        // -->
        if (attribute.startsWith("armor_pose_list")) {
            return getPoseList().getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.armor_pose[<part>]>
        // @returns dLocation
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
                dB.echoError("Invalid pose part specified: " + name);
                return null;
            }
            else {
                return fromEulerAngle(posePart.getAngle((ArmorStand) entity.getBukkitEntity()))
                        .getAttribute(attribute.fulfill(1));
            }
        }


        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name armor_pose
        // @input dList
        // @description
        // Sets the angle for various parts of the armor stand in the
        // format: PART|ANGLE|...
        // For example, head|4.5,3,4.5|body|5.4,3.2,1
        // Valid parts: HEAD, BODY, LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG
        // Angles are in radians!
        // @tags
        // <e@entity.armor_pose_list>
        // <e@entity.armor_pose[<part>]>
        // -->
        if (mechanism.matches("armor_pose")) {
            ArmorStand armorStand = (ArmorStand) entity.getBukkitEntity();
            dList list = mechanism.getValue().asType(dList.class);
            Iterator<String> iterator = list.iterator();
            while (iterator.hasNext()) {
                String name = iterator.next();
                String angle = iterator.next();
                PosePart posePart = PosePart.fromName(name);
                if (posePart == null) {
                    dB.echoError("Invalid pose part specified: " + name + "; ignoring next: " + angle);
                }
                else {
                    posePart.setAngle(armorStand, toEulerAngle(dLocation.valueOf(angle)));
                }
            }
        }
    }

    private static dLocation fromEulerAngle(EulerAngle eulerAngle) {
        return new dLocation(null, eulerAngle.getX(), eulerAngle.getY(), eulerAngle.getZ());
    }

    private static EulerAngle toEulerAngle(dLocation location) {
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

package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TippedArrow;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntityPotionEffects implements Property {

    public static boolean describes(dObject object) {
        return object instanceof dEntity &&
                (((dEntity) object).isLivingEntity()
                        || isTippedArrow(((dEntity) object).getBukkitEntityType()));
    }

    public static boolean isTippedArrow(EntityType entityType) {
        return entityType.getEntityClass() == TippedArrow.class;
    }

    public static EntityPotionEffects getFrom(dObject object) {
        if (!describes(object)) {
            return null;
        }
        else {
            return new EntityPotionEffects((dEntity) object);
        }
    }

    public static final String[] handledTags = new String[] {
            "list_effects", "has_effect"
    };

    public static final String[] handledMechs = new String[] {
            "potion_effects"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityPotionEffects(dEntity entity) {
        this.entity = entity;
    }

    dEntity entity;

    public Collection<PotionEffect> getEffectsList() {
        if (entity.isLivingEntity()) {
            return entity.getLivingEntity().getActivePotionEffects();
        }
        else if (isTippedArrow(entity.getBukkitEntityType())) {
            return ((TippedArrow) entity.getBukkitEntity()).getCustomEffects();
        }
        return new ArrayList<PotionEffect>();
    }

    /////////
    // Property Methods
    ///////

    public String getPropertyString() {
        Collection<PotionEffect> effects = getEffectsList();
        if (effects.isEmpty()) {
            return null;
        }
        dList returnable = new dList();
        for (PotionEffect effect : effects) {
            returnable.add(effect.getType().getName() + "," + effect.getAmplifier() + "," + effect.getDuration());
        }
        return returnable.identify().substring(3);
    }

    public String getPropertyId() {
        return "potion_effects";
    }

    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return "null";
        }

        // <--[tag]
        // @attribute <e@entity.list_effects>
        // @returns dList
        // @group attribute
        // @mechanism dEntity.potion_effects
        // @description
        // Returns the list of active potion effects on the entity, in the format: li@TYPE,AMPLIFIER,DURATION|...
        // Note that AMPLIFIER is a number representing the level, and DURATION is a number representing the time, in ticks, it will last for.
        // -->
        if (attribute.startsWith("list_effects")) {
            dList effects = new dList();
            for (PotionEffect effect : getEffectsList()) {
                effects.add(effect.getType().getName() + "," + effect.getAmplifier() + "," + effect.getDuration());
            }
            return effects.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.has_effect[<effect>]>
        // @returns Element(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity has a specified effect.
        // If no effect is specified, returns whether the entity has any effect.
        // -->
        if (attribute.startsWith("has_effect")) {
            boolean returnElement = false;
            if (attribute.hasContext(1)) {
                PotionEffectType effectType = PotionEffectType.getByName(attribute.getContext(1));
                for (org.bukkit.potion.PotionEffect effect : getEffectsList()) {
                    if (effect.getType().equals(effectType)) {
                        returnElement = true;
                    }
                }
            }
            else if (!getEffectsList().isEmpty()) {
                returnElement = true;
            }
            return new Element(returnElement).getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name potion_effects
        // @input dList
        // @description
        // Set the entity's active potion effects.
        // Each item in the list is formatted as: TYPE,AMPLIFIER,DURATION
        // Note that AMPLIFIER is a number representing the level, and DURATION is a number representing the time, in ticks, it will last for.
        // For example: SPEED,0,120 would give the entity a swiftness potion for 120 ticks.
        // @tags
        // <e@entity.list_effects>
        // -->
        if (mechanism.matches("potion_effects")) {
            dList effects = dList.valueOf(mechanism.getValue().asString());
            for (String effect : effects) {
                List<String> split = CoreUtilities.split(effect, ',');
                if (split.size() != 3) {
                    // Maybe error message?
                    continue;
                }
                PotionEffectType effectType = PotionEffectType.getByName(split.get(0));
                if (effectType == null) {
                    dB.echoError("Cannot apply potion effect '" + split.get(0) + "': unknown effect type.");
                    continue;
                }
                try {
                    if (entity.isLivingEntity()) {
                        entity.getLivingEntity().addPotionEffect(new PotionEffect(effectType, Integer.valueOf(split.get(2)),
                                Integer.valueOf(split.get(1))));
                    }
                    else if (isTippedArrow(entity.getBukkitEntityType())) {
                        ((TippedArrow) entity.getBukkitEntity()).addCustomEffect(new PotionEffect(effectType, Integer.valueOf(split.get(2)),
                                Integer.valueOf(split.get(1))), true);
                    }
                }
                catch (NumberFormatException ex) {
                    dB.echoError("Cannot apply potion effect '" + effect + "': invalid amplifier or duration number.");
                    continue;
                }
            }
        }
    }
}

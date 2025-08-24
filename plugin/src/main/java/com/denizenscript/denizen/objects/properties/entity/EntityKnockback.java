package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.inventory.ItemStack;

@Deprecated
public class EntityKnockback implements Property {

    public static boolean describes(ObjectTag object) {
        return object instanceof EntityTag entity && entity.getBukkitEntity() instanceof AbstractArrow;
    }

    public static EntityKnockback getFrom(ObjectTag object) {
        if (!describes(object)) {
            return null;
        }
        else {
            return new EntityKnockback((EntityTag) object);
        }
    }

    public EntityKnockback(EntityTag entity) {
        arrow = entity;
    }

    EntityTag arrow;

    @Override
    public String getPropertyString() {
        return NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) ? null : String.valueOf(getAbstractArrow().getKnockbackStrength());
    }

    @Override
    public String getPropertyId() {
        return "knockback";
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.knockback>
        // @returns ElementTag(Number)
        // @mechanism EntityTag.knockback
        // @group properties
        // @description
        // Deprecated in favor of setting the knockback enchantment on the weapon itself on MC 1.21+.
        // @deprecated use the knockback enchantment on the weapon itself on MC 1.21+.
        // -->
        PropertyParser.registerTag(EntityKnockback.class, ElementTag.class, "knockback", (attribute, object) -> {
            BukkitImplDeprecations.entityKnockback.warn(attribute.context);
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21)) {
                ItemStack is = object.getAbstractArrow().getWeapon();
                return new ElementTag(is == null ? 0 : is.getEnchantmentLevel(Enchantment.KNOCKBACK)); // it is nullable even when it is marked as @NotNull, for example when you spawn an arrow
            }
            else {
                return new ElementTag(object.getAbstractArrow().getKnockbackStrength());
            }
        });

        // <--[mechanism]
        // @object EntityTag
        // @name knockback
        // @input ElementTag(Number)
        // @description
        // Deprecated in favor of setting the knockback enchantment on the weapon itself on MC 1.21+.
        // @deprecated use the knockback enchantment on the weapon itself on MC 1.21+.
        // @tags
        // <EntityTag.knockback>
        // -->
        PropertyParser.registerMechanism(EntityKnockback.class, ElementTag.class, "knockback", (object, mechanism, input) -> {
            BukkitImplDeprecations.entityKnockback.warn(mechanism.context);
            if (mechanism.requireInteger()) {
                if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21)) {
                    ItemStack is = object.getAbstractArrow().getWeapon();
                    if (is == null) { // it is nullable even when it is marked as @NotNull, for example when you spawn an arrow
                        is = new ItemStack(Material.BOW);
                    }
                    is.addUnsafeEnchantment(Enchantment.KNOCKBACK, input.asInt());
                    object.getAbstractArrow().setWeapon(is);
                }
                else {
                    object.getAbstractArrow().setKnockbackStrength(input.asInt());
                }
            }
        });
    }

    public AbstractArrow getAbstractArrow() {
        return (AbstractArrow) arrow.getBukkitEntity();
    }
}

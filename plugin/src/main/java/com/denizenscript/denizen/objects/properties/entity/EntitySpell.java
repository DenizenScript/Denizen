package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import net.citizensnpcs.trait.versioned.SpellcasterTrait;
import org.bukkit.entity.Spellcaster;

public class EntitySpell extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name spell
    // @input ElementTag
    // @description
    // Controls the spell that an Illager entity (such as an Evoker or Illusioner) should cast.
    // Valid spells are: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Spellcaster.Spell.html>
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Spellcaster;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(Spellcaster.class).getSpell());
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (!mechanism.requireEnum(Spellcaster.Spell.class)) {
            return;
        }
        Spellcaster.Spell spell = param.asEnum(Spellcaster.Spell.class);
        if (object.isCitizensNPC()) {
            object.getDenizenNPC().getCitizen().getOrAddTrait(SpellcasterTrait.class).setSpell(spell);
        }
        as(Spellcaster.class).setSpell(spell);
    }

    @Override
    public String getPropertyId() {
        return "spell";
    }

    public static void register() {
        autoRegister("spell", EntitySpell.class, ElementTag.class, false);
    }
}

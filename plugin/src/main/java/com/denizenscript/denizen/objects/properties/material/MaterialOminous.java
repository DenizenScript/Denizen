package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.TrialSpawner;
import org.bukkit.block.data.type.Vault;

public class MaterialOminous implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && (((MaterialTag) material).getModernData() instanceof Vault
                || ((MaterialTag) material).getModernData() instanceof TrialSpawner);
    }

    public static MaterialOminous getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        } else {
            return new MaterialOminous((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[]{
            "ominous"
    };

    public MaterialOminous(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void register() {

        // <--[tag]
        // @attribute <MaterialTag.is_ominous>
        // @returns ElementTag(Boolean)
        // @mechanism MaterialTag.ominous
        // @group properties
        // @description
        // Returns whether the block is ominous or not.
        // Currently supports Vault and Trial Spawner blocks.
        // -->
        PropertyParser.registerStaticTag(MaterialOminous.class, ElementTag.class, "is_ominous", (attribute, material) -> {
            return new ElementTag(material.getOminous());
        });
    }

    public boolean getOminous() {
        if (material.getModernData() instanceof Vault) {
            return ((Vault) material.getModernData()).isOminous();
        } else if (material.getModernData() instanceof TrialSpawner) {
            return ((TrialSpawner) material.getModernData()).isOminous();
        }
        return false;
    }

    public void setOminous(boolean ominous) {
        if (material.getModernData() instanceof Vault vault) {
            vault.setOminous(ominous);
        } else if (material.getModernData() instanceof TrialSpawner spawner) {
            spawner.setOminous(ominous);
        }
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getOminous());
    }

    @Override
    public String getPropertyId() {
        return "ominous";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name ominous
        // @input ElementTag(Boolean)
        // @description
        // Sets the ominousness of the block (trial spawner or vault).
        // @tags
        // <MaterialTag.is_ominous>
        // -->
        if (mechanism.matches("ominous") && mechanism.requireBoolean()) {
            setOminous(mechanism.getValue().asBoolean());
        }
    }
}

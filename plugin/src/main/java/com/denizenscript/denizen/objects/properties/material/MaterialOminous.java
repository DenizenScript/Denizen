package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.block.data.type.TrialSpawner;
import org.bukkit.block.data.type.Vault;

public class MaterialOminous extends MaterialProperty<ElementTag> {

    // <--[property]
    // @object MaterialTag
    // @name ominous
    // @input ElementTag(Boolean)
    // @description
    // Controls whether a trial spawner or vault is in ominous mode.
    // -->

    public static boolean describes(MaterialTag material) {
        return material.getModernData() instanceof TrialSpawner
                || material.getModernData() instanceof Vault;
    }

    @Override
    public ElementTag getPropertyValue() {
        if (getBlockData() instanceof TrialSpawner trialSpawner) {
            return new ElementTag(trialSpawner.isOminous());
        }
        return new ElementTag(as(Vault.class).isOminous());
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (!mechanism.requireBoolean()) {
            return;
        }
        if (getBlockData() instanceof TrialSpawner trialSpawner) {
            trialSpawner.setOminous(value.asBoolean());
        }
        else {
            as(Vault.class).setOminous(value.asBoolean());
        }
    }

    @Override
    public String getPropertyId() {
        return "ominous";
    }

    public static void register() {
        autoRegister("ominous", MaterialOminous.class, ElementTag.class, false);
    }
}

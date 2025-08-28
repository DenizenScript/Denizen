package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.Vault;

public class MaterialVaultState implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData() instanceof Vault;
    }

    public static MaterialVaultState getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        } else {
            return new MaterialVaultState((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[]{
            "state"
    };

    public MaterialVaultState(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void register() {

        // <--[tag]
        // @attribute <MaterialTag.state>
        // @returns ElementTag
        // @mechanism MaterialTag.state
        // @group properties
        // @description
        // Returns the current state of this vault block.
        // -->
        PropertyParser.registerStaticTag(MaterialVaultState.class, ElementTag.class, "state", (attribute, material) -> {
            return material.getState();
        });
    }

    public Vault getVault() {
        return (Vault) material.getModernData();
    }

    public ElementTag getState() {
        return new ElementTag(getVault().getVaultState());
    }

    @Override
    public String getPropertyString() {
        return getState().identify();
    }

    @Override
    public String getPropertyId() {
        return "state";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name state
        // @input ElementTag
        // @description
        // Sets the vault block state.
        // @tags
        // <MaterialTag.state>
        // -->
        if (mechanism.matches("state")) {
            if (mechanism.requireEnum(Vault.State.class)) {
                getVault().setVaultState(Vault.State.valueOf(mechanism.getValue().asString().toUpperCase()));
            } else {
                mechanism.echoError("MaterialTag.State mechanism has bad input: vault state value '" + mechanism.getValue().asString() + "' is invalid.");
            }
        }
    }
}

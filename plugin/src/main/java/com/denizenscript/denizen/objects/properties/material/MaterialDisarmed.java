package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Tripwire;

public class MaterialDisarmed implements Property {

    public static boolean describes(ObjectTag material) {
        if (!(material instanceof MaterialTag)) {
            return false;
        }
        MaterialTag mat = (MaterialTag) material;
        if (!mat.hasModernData()) {
            return false;
        }
        BlockData data = mat.getModernData();
        return data instanceof MaterialTag
                || ((MaterialTag) material).getModernData() instanceof Tripwire;
    }

    public static MaterialDisarmed getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialDisarmed((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "disarmed"
    };

    private MaterialDisarmed(MaterialTag _material) {
        material = _material;
    }

    public MaterialTag material;

    public static void register() {

        // <--[tag]
        // @attribute <MaterialTag.disarmed>
        // @returns ElementTag
        // @mechanism MaterialTag.disarmed
        // @group properties
        // @description
        // Returns the current disarmed state of the tripwire.
        // For tripwires, is TRUE (corresponding to "disarmed") or FALSE (corresponding to "armed").
        // -->
        PropertyParser.registerStaticTag(MaterialDisarmed.class, ElementTag.class, "disarmed",
                (attribute, material) -> new ElementTag(material.getDisarmable().isDisarmed()),"is_disarmed");
    }

    public boolean isDisarmed() {
        return material.getModernData() instanceof Tripwire;
    }

    public Tripwire getDisarmable() {
        return (Tripwire) material.getModernData();
    }

    @Override
    public String getPropertyString() {
        if (isDisarmed()) {
            Bukkit.getLogger().info(getDisarmable().getAsString());
            return getDisarmable().getAsString();
        }
        return null; // Unreachable.
    }

    @Override
    public String getPropertyId() {
        return "disarmed";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name disarmed
        // @input ElementTag
        // @description
        // Sets the current disarmed state of the tripwire.
        // For tripwires, input is TRUE (corresponding to "disarmed") or FALSE (corresponding to "armed").
        // @tags
        // <MaterialTag.disarmed>
        // -->
        if (mechanism.matches("disarmed")) {
            if (isDisarmed()) {
                getDisarmable().setDisarmed(mechanism.getValue().asBoolean());
            }
        }
    }
}

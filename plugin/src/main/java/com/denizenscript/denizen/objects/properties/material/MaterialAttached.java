package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.exceptions.Unreachable;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.Attachable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Hangable;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.Lantern;

public class MaterialAttached extends MaterialProperty {

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof Gate || data instanceof Lantern || data instanceof Attachable
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && data instanceof Hangable);
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(isAttached());
    }

    @Override
    public String getPropertyId() {
        return "attached";
    }

    public static void register() {

        // <--[tag]
        // @attribute <MaterialTag.attached>
        // @returns ElementTag(Boolean)
        // @mechanism MaterialTag.attached
        // @group properties
        // @description
        // Returns whether a material is attached.
        // For a lantern, this returns whether it is hanging from the ceiling.
        // For a gate, this returns whether it is lowered to attach to a wall block.
        // For a mangrove_propagule, this returns whether it is hanging from the block above it.
        // For a tripwire, this returns whether a tripwire hook or string forms a complete tripwire circuit and is ready to trigger.
        // For a hanging sign, this returns whether it is hanging from the block above it.
        // -->
        PropertyParser.registerStaticTag(MaterialAttached.class, ElementTag.class, "attached", (attribute, prop) -> {
            return new ElementTag(prop.isAttached());
        }, "attached_to_wall");

        // <--[mechanism]
        // @object MaterialTag
        // @name attached
        // @input ElementTag(Boolean)
        // @description
        // Sets whether a material is attached.
        // For a lantern, this sets whether it is hanging from the ceiling.
        // For a gate, this sets whether it is lowered to attach to a wall block.
        // For a mangrove_propagule, this sets whether it is hanging from the block above it.
        // For a tripwire, this sets whether a tripwire hook or string forms a complete tripwire circuit and is ready to trigger.
        // Updating the property on a tripwire hook will change the texture to indicate a connected string, but will not have any effect when used on the tripwire string itself.
        // It may however still be used to check whether the string forms a circuit.
        // For hanging signs, this affects signs hanging below a block and changes whether the chains are vertical (false) or diagonal (true).
        // @tags
        // <MaterialTag.attached>
        // -->
        PropertyParser.registerMechanism(MaterialAttached.class, ElementTag.class, "attached", (prop, mechanism, param) -> {
            if (!mechanism.requireBoolean()) {
                return;
            }
            boolean attach = param.asBoolean();
            BlockData data = prop.getBlockData();
            if (data instanceof Gate gate) {
                gate.setInWall(attach);
            }
            else if (data instanceof Lantern lantern) { // TODO: remove once 1.19 is the minimum - Lantern extends Hangable
                lantern.setHanging(attach);
            }
            else if (data instanceof Attachable attachable) {
                attachable.setAttached(attach);
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && data instanceof Hangable hangable) {
                hangable.setHanging(attach);
            }
        }, "attached_to_wall");
    }

    public boolean isAttached() {
        BlockData data = getBlockData();
        if (data instanceof Gate gate) {
            return gate.isInWall();
        }
        else if (data instanceof Lantern lantern) { // TODO: remove once 1.19 is the minimum - Lantern extends Hangable
            return lantern.isHanging(); // This is explicitly Hangable.isHanging, yet somehow it still works pre-1.19, rare moment of Java being nice about that stuff
        }
        else if (data instanceof Attachable attachable) {
            return attachable.isAttached();
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && data instanceof Hangable hangable) {
            return hangable.isHanging();
        }
        throw new Unreachable();
    }
}

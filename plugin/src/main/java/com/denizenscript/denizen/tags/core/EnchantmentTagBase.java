package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.EnchantmentTag;
import com.denizenscript.denizencore.tags.TagManager;

public class EnchantmentTagBase {

    public EnchantmentTagBase() {

        // <--[tag]
        // @attribute <enchantment[<enchantment>]>
        // @returns EnchantmentTag
        // @description
        // Returns an enchantment object constructed from the input value.
        // Refer to <@link objecttype EnchantmentTag>.
        // -->
        TagManager.registerTagHandler(EnchantmentTag.class, "enchantment", (attribute) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("Enchantment tag base must have input.");
                return null;
            }
            return EnchantmentTag.valueOf(attribute.getContext(1), attribute.context);
        });
    }
}

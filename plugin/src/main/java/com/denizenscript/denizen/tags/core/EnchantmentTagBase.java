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
        TagManager.registerStaticTagBaseHandler(EnchantmentTag.class, "enchantment", (attribute) -> {
            if (!attribute.hasParam()) {
                attribute.echoError("Enchantment tag base must have input.");
                return null;
            }
            return EnchantmentTag.valueOf(attribute.getParam(), attribute.context);
        });
    }
}

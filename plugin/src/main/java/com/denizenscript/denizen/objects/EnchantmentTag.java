package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.scripts.containers.core.EnchantmentScriptContainer;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.flags.RedirectionFlagTracker;
import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityDamageEvent;

public class EnchantmentTag implements ObjectTag, FlaggableObject {

    // <--[ObjectType]
    // @name EnchantmentTag
    // @prefix enchantment
    // @base ElementTag
    // @implements FlaggableObject
    // @ExampleTagBase enchantment[sharpness]
    // @ExampleValues sharpness
    // @ExampleForReturns
    // - inventory adjust slot:hand enchantments:[%VALUE%=5]
    // @format
    // The identity format for enchantments is the vanilla ID, Denizen ID, or full key. Can also be constructed by Denizen script name.
    // For example, 'enchantment@sharpness', 'enchantment@my_custom_ench', or 'enchantment@otherplugin:customench'.
    //
    // @description
    // An EnchantmentTag represents an item enchantment abstractly (the enchantment itself, like 'sharpness', which *can be* applied to an item, as opposed to the specific reference to an enchantment on a specific item).
    // For enchantment names, check <@link url https://minecraft.wiki/w/Enchanting#Summary_of_enchantments>. Note spaces should swapped to underscores, so for example "Aqua Affinity" becomes "aqua_affinity".
    //
    // This object type is flaggable.
    // Flags on this object type will be stored in the server saves file, under special sub-key "__enchantments"
    //
    // -->

    //////////////////
    //    Object Fetcher
    ////////////////

    @Fetchable("enchantment")
    public static EnchantmentTag valueOf(String string, TagContext context) {
        if (string == null) {
            return null;
        }
        string = CoreUtilities.toLowerCase(string);
        if (string.startsWith("enchantment@")) {
            string = string.substring("enchantment@".length());
        }
        Enchantment ench;
        NamespacedKey key = Utilities.parseNamespacedKey(string);
        ench = Enchantment.getByKey(key);
        if (ench == null) {
            ench = Enchantment.getByName(string.toUpperCase());
        }
        if (ench == null) {
            ench = Enchantment.getByKey(new NamespacedKey("denizen", Utilities.cleanseNamespaceID(string)));
        }
        if (ench == null && ScriptRegistry.containsScript(string, EnchantmentScriptContainer.class)) {
            ench = ScriptRegistry.getScriptContainerAs(string, EnchantmentScriptContainer.class).enchantment;
        }
        if (ench == null) {
            if (context == null || context.debug || CoreConfiguration.debugOverride) {
                Debug.echoError("Unknown enchantment '" + string + "'");
            }
            return null;
        }
        return new EnchantmentTag(ench);

    }

    public static boolean matches(String arg) {
        if (CoreUtilities.toLowerCase(arg).startsWith("enchantment@")) {
            return true;
        }
        return valueOf(arg, CoreUtilities.noDebugContext) != null;
    }

    public EnchantmentTag(Enchantment enchantment) {
        this.enchantment = enchantment;
    }

    public Enchantment enchantment;

    private String prefix = "Enchantment";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public String identify() {
        return "enchantment@" + getCleanName();
    }

    @Override
    public String identifySimple() {
        return identify();
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public Object getJavaObject() {
        return enchantment;
    }

    @Override
    public EnchantmentTag setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public String getCleanName() {
        NamespacedKey key = enchantment.getKey();
        if (key.getNamespace().equals("minecraft") || key.getNamespace().equals("denizen")) {
            return key.getKey();
        }
        return key.toString();
    }

    @Override
    public AbstractFlagTracker getFlagTracker() {
        return new RedirectionFlagTracker(DenizenCore.serverFlagMap, "__enchantments." + getCleanName().replace(".", "&dot"));
    }

    @Override
    public void reapplyTracker(AbstractFlagTracker tracker) {
        // Nothing to do.
    }

    public static void register() {

        AbstractFlagTracker.registerFlagHandlers(tagProcessor);

        // <--[tag]
        // @attribute <EnchantmentTag.name>
        // @returns ElementTag
        // @description
        // Gets the name of this enchantment. For vanilla enchantments, uses the vanilla name like 'sharpness'.
        // For Denizen custom enchantments, returns the 'id' specified in the script.
        // For any other enchantments, returns the full key.
        // @example
        // # This can narrate something like this:
        // # "The item in your hand's first enchantment is sharpness!"
        // - narrate "The item in your hand's first enchantment is <player.item_in_hand.enchantment_types.get[1].name||nothing>!"
        // -->
        tagProcessor.registerTag(ElementTag.class, "name", (attribute, object) -> {
            return new ElementTag(object.getCleanName());
        });

        // <--[tag]
        // @attribute <EnchantmentTag.key>
        // @returns ElementTag
        // @description
        // Returns the full key for this enchantment, like "minecraft:sharpness".
        // @example
        // # Narrates "The key for the flame enchantment is: minecraft:flame!"
        // - narrate "The key for the flame enchantment is: <enchantment[flame].key>!"
        // @example
        // # This example uses a custom Denizen enchantment.
        // # Narrates "The key for the my_enchantment enchantment is: denizen:my_enchantment!"
        // - narrate "The key for the my_enchantment enchantment is: <enchantment[my_enchantment].key>!"
        // -->
        tagProcessor.registerTag(ElementTag.class, "key", (attribute, object) -> {
            return new ElementTag(object.enchantment.getKey().toString());
        });

        // <--[tag]
        // @attribute <EnchantmentTag.full_name[<level>]>
        // @returns ElementTag
        // @description
        // Returns the full name for this enchantment for a given level, like "Sharpness V".
        // For vanilla enchantments, uses language translation keys.
        // @example
        // # Narrates "You don your Thorns III armor."
        // # Note: vanilla enchantments have a color applied to them, such as grey or red.
        // - narrate "You don your <enchantment[thorns].full_name[3]><reset> armor."
        // -->
        tagProcessor.registerTag(ElementTag.class, "full_name", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            return new ElementTag(NMSHandler.enchantmentHelper.getFullName(object.enchantment, attribute.getIntParam()));
        });

        // <--[tag]
        // @attribute <EnchantmentTag.script>
        // @returns ScriptTag
        // @description
        // Returns the script that created this enchantment type, if any.
        // @example
        // # This can narrate something like this:
        // # "The script used to create the my_enchantment enchantment is: my_enchantment_script"
        // - narrate "The script used to create the my_enchantment enchantment is: <enchantment[my_enchantment].script.name>"
        // -->
        tagProcessor.registerTag(ScriptTag.class, "script", (attribute, object) -> {
            if (!object.enchantment.getKey().getNamespace().equals("denizen")) {
                return null;
            }
            EnchantmentScriptContainer.EnchantmentReference ref = EnchantmentScriptContainer.registeredEnchantmentContainers.get(object.enchantment.getKey().getKey());
            if (ref == null) {
                return null;
            }
            return new ScriptTag(ref.script);
        });

        // <--[tag]
        // @attribute <EnchantmentTag.min_level>
        // @returns ElementTag(Number)
        // @description
        // Returns the minimum level of this enchantment. Usually '1'.
        // @example
        // # Narrates "The minimum enchantment level that you can get for Feather Falling is: 1!"
        // - narrate "The minimum enchantment level that you can get for Feather Falling is: <enchantment[feather_falling].min_level>!"
        // -->
        tagProcessor.registerTag(ElementTag.class, "min_level", (attribute, object) -> {
            return new ElementTag(object.enchantment.getStartLevel());
        });

        // <--[tag]
        // @attribute <EnchantmentTag.max_level>
        // @returns ElementTag(Number)
        // @description
        // Returns the maximum level of this enchantment. Usually between 1 and 5.
        // @example
        // # Narrates "The maximum enchantment level that you can get for Feather Falling is: 4!"
        // - narrate "The maximum enchantment level that you can get for Feather Falling is: <enchantment[feather_falling].max_level>!"
        // -->
        tagProcessor.registerTag(ElementTag.class, "max_level", (attribute, object) -> {
            return new ElementTag(object.enchantment.getMaxLevel());
        });

        // <--[tag]
        // @attribute <EnchantmentTag.treasure_only>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether this enchantment is only for spawning as treasure.
        // @example
        // # In the case of the "loyalty" enchantment, this will narrate:
        // # "You cannot find this enchantment as treasure!"
        // - if <enchantment[loyalty].treasure_only>:
        //     - narrate "You can only find this enchantment as treasure!"
        // - else:
        //     - narrate "You cannot find this enchantment as treasure!"
        // -->
        tagProcessor.registerTag(ElementTag.class, "treasure_only", (attribute, object) -> {
            return new ElementTag(object.enchantment.isTreasure());
        });

        // <--[tag]
        // @attribute <EnchantmentTag.is_tradable>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether this enchantment is only considered to be tradable. Villagers won't trade this enchantment if set to false.
        // @example
        // # In the case of the "loyalty" enchantment, this will narrate:
        // # "Let's do some haggling to get this enchantment!"
        // - if <enchantment[loyalty].is_tradable>:
        //     - narrate "Let's do some haggling to get this enchantment!"
        // - else:
        //     - narrate "Villagers don't seem to want to trade this enchantment!"
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_tradable", (attribute, object) -> {
            return new ElementTag(NMSHandler.enchantmentHelper.isTradable(object.enchantment));
        });

        // <--[tag]
        // @attribute <EnchantmentTag.is_discoverable>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether this enchantment is only considered to be discoverable.
        // If true, this will spawn from vanilla sources like the enchanting table. If false, it can only be given directly by script.
        // @example
        // # In the case of the "loyalty" enchantment, this will narrate:
        // # "Time to do some discovering!"
        // - if <enchantment[loyalty].is_discoverable>:
        //     - narrate "Time to do some discovering!"
        // - else:
        //     - narrate "You'll have to be given this enchantment through a script!"
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_discoverable", (attribute, object) -> {
            return new ElementTag(NMSHandler.enchantmentHelper.isDiscoverable(object.enchantment));
        });

        // <--[tag]
        // @attribute <EnchantmentTag.is_curse>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether this enchantment is only considered to be a curse. Curses are removed at grindstones, and spread from crafting table repairs.
        // @example
        // # In the case of the "vanishing_curse" enchantment, this will narrate:
        // # "Watch out! This enchantment is a curse!"
        // - if <enchantment[vanishing_curse].is_curse>:
        //     - narrate "Watch out! This enchantment is a curse!"
        // - else:
        //     - narrate "Phew, this enchantment is not a curse!"
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_curse", (attribute, object) -> {
            return new ElementTag(NMSHandler.enchantmentHelper.isCurse(object.enchantment));
        });

        // <--[tag]
        // @attribute <EnchantmentTag.category>
        // @returns ElementTag
        // @description
        // Returns the category of this enchantment. Can be any of: ARMOR, ARMOR_FEET, ARMOR_LEGS, ARMOR_CHEST, ARMOR_HEAD,
        // WEAPON, DIGGER, FISHING_ROD, TRIDENT, BREAKABLE, BOW, WEARABLE, CROSSBOW, VANISHABLE
        // @example
        // # This narrates "The bane_of_arthropods enchantment is in the WEAPON category!"
        // - narrate "The bane_of_arthropods enchantment is in the <enchantment[bane_of_arthropods].category> category!"
        // -->
        tagProcessor.registerTag(ElementTag.class, "category", (attribute, object) -> {
            return new ElementTag(object.enchantment.getItemTarget());
        });

        // <--[tag]
        // @attribute <EnchantmentTag.rarity>
        // @returns ElementTag
        // @description
        // Returns the rarity of this enchantment. Can be any of: COMMON, UNCOMMON, RARE, VERY_RARE
        // @example
        // # This narrates "The infinity enchantment is VERY_RARE!"
        // - narrate "The infinity enchantment is <enchantment[infinity].rarity>!"
        // -->
        tagProcessor.registerTag(ElementTag.class, "rarity", (attribute, object) -> {
            return new ElementTag(NMSHandler.enchantmentHelper.getRarity(object.enchantment));
        });

        // <--[tag]
        // @attribute <EnchantmentTag.can_enchant[<item>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether this enchantment can enchant the given ItemTag (based on material mainly).
        // This is internally based on multiple factors, such as the enchantment's category and its own specific compatibility checks.
        // @example
        // # In the case of the "knockback" enchantment, this narrates
        // # "You can apply the knockback enchantment in a diamond sword!"
        // - if <enchantment[knockback].can_enchant[diamond_sword]>:
        //     - narrate "You can apply the knockback enchantment in a diamond sword!"
        // - else:
        //     - narrate "You cannot apply the knockback enchantment in a diamond sword!"
        // -->
        tagProcessor.registerTag(ElementTag.class, "can_enchant", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            return new ElementTag(object.enchantment.canEnchantItem(attribute.paramAsType(ItemTag.class).getItemStack()));
        });

        // <--[tag]
        // @attribute <EnchantmentTag.is_compatible[<enchantment>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether this enchantment is compatible with another given enchantment.
        // @example
        // # In the case of the "silk_touch" and "mending" enchantments, this narrates
        // # "These enchantments are compatible together!"
        // - if <enchantment[silk_touch].is_compatible[mending]>:
        //     - narrate "These enchantments are compatible together!"
        // - else:
        //     - narrate "These enchantments are not compatible with each other!"
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_compatible", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            return new ElementTag(!object.enchantment.conflictsWith(attribute.paramAsType(EnchantmentTag.class).enchantment));
        });

        // <--[tag]
        // @attribute <EnchantmentTag.min_cost[<level>]>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the minimum cost for this enchantment for the given level.
        // @example
        // # This narrates "The minimum cost for the efficiency 3 enchantment is: 21!"
        // - narrate "The minimum cost for the efficiency 3 enchantment is: <enchantment[efficiency].min_cost[3]>!"
        // -->
        tagProcessor.registerTag(ElementTag.class, "min_cost", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            return new ElementTag(NMSHandler.enchantmentHelper.getMinCost(object.enchantment, attribute.getIntParam()));
        });

        // <--[tag]
        // @attribute <EnchantmentTag.max_cost[<level>]>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the maximum cost for this enchantment for the given level.
        // @example
        // # This narrates "The maximum cost for the efficiency 3 enchantment is: 81!"
        // - narrate "The maximum cost for the efficiency 3 enchantment is: <enchantment[efficiency].max_cost[3]>!"
        // -->
        tagProcessor.registerTag(ElementTag.class, "max_cost", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            return new ElementTag(NMSHandler.enchantmentHelper.getMaxCost(object.enchantment, attribute.getIntParam()));
        });

        // <--[tag]
        // @attribute <EnchantmentTag.damage_bonus[level=<level>;type=<type>]>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the damage bonus this enchantment applies against the given monster type.
        // The input is a MapTag with a level value and a monster type specified, where the type can be any of: ARTHROPOD, ILLAGER, WATER, UNDEAD, or UNDEFINED
        // See also <@link tag EntityTag.monster_type> for getting the category of another mob.
        // @example
        // # Narrates "With Bane of Arthropods 2, you get a damage bonus of 5 on arthropods!"
        // - narrate "With Bane of Arthropods 2, you get a damage bonus of <enchantment[bane_of_arthropods].damage_bonus[level=2;type=arthropod]> on arthropods!"
        // -->
        tagProcessor.registerTag(ElementTag.class, MapTag.class, "damage_bonus", (attribute, object, map) -> {
            ElementTag level = map.getElement("level");
            ElementTag type = map.getElement("type");
            if (level == null || type == null) {
                attribute.echoError("Invalid MapTag input to damage_bonus - missing 'level' or 'type'");
                return null;
            }
            return new ElementTag(NMSHandler.enchantmentHelper.getDamageBonus(object.enchantment, level.asInt(), CoreUtilities.toLowerCase(type.toString())));
        });

        // <--[tag]
        // @attribute <EnchantmentTag.damage_protection[level=<level>;type=<cause>;(attacker=<entity>)]>
        // @returns ElementTag(Number)
        // @description
        // Returns the damage protection this enchantment applies against the given damage cause and optional attacker.
        // The input is a MapTag with a level value and a damage type specified, where the damage type must be from <@link language Damage Cause>.
        // For entity damage causes, optionally specify the entity attacker.
        // @example
        // # Narrates "With the Protection 3 enchantment, there is fall damage protection of 3!"
        // - narrate "With the Protection 3 enchantment, there is fall damage protection of <enchantment[protection].damage_protection[level=3;type=fall]>!"
        // -->
        tagProcessor.registerTag(ElementTag.class, MapTag.class, "damage_protection", (attribute, object, map) -> {
            ElementTag level = map.getRequiredObjectAs("level", ElementTag.class, attribute);
            ElementTag type = map.getRequiredObjectAs("type", ElementTag.class, attribute);
            if (level == null || type == null) {
                return null;
            }
            EntityDamageEvent.DamageCause cause;
            try {
                cause = EntityDamageEvent.DamageCause.valueOf(type.toString().toUpperCase());
            }
            catch (IllegalArgumentException ex) {
                attribute.echoError("Invalid MapTag input to damage_protection - cause '" + type + "' is not a valid DamageCause.");
                return null;
            }
            EntityTag attacker = map.getObjectAs("attacker", EntityTag.class, attribute.context);
            return new ElementTag(NMSHandler.enchantmentHelper.getDamageProtection(object.enchantment, level.asInt(), cause, attacker == null ? null : attacker.getBukkitEntity()));
        });
    }

    public static ObjectTagProcessor<EnchantmentTag> tagProcessor = new ObjectTagProcessor<>();

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }
}

package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemPatterns implements Property {

    public static boolean isBannerOrShield(Material material) {
        return material == Material.SHIELD || material.name().endsWith("_BANNER");
    }

    public static boolean describes(ObjectTag item) {
        if (item instanceof ItemTag) {
            Material material = ((ItemTag) item).getBukkitMaterial();
            return isBannerOrShield(material);
        }
        return false;
    }

    public static ItemPatterns getFrom(ObjectTag item) {
        if (!describes(item)) {
            return null;
        }
        else {
            return new ItemPatterns((ItemTag) item);
        }
    }

    public static final String[] handledTags = new String[] {
            "patterns"
    };

    public static final String[] handledMechs = new String[] {
            "patterns"
    };

    public ItemPatterns(ItemTag item) {
        this.item = item;
    }

    ItemTag item;

    public ListTag listPatterns() {
        ListTag list = new ListTag();
        for (Pattern pattern : getPatterns()) {
            list.add(pattern.getColor().name() + "/" + pattern.getPattern().name());
        }
        return list;
    }

    public List<Pattern> getPatterns() {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta instanceof BannerMeta) {
            return ((BannerMeta) itemMeta).getPatterns();
        }
        else if (itemMeta instanceof BlockStateMeta) {
            return ((Banner) ((BlockStateMeta) itemMeta).getBlockState()).getPatterns();
        }
        else {
            // ...???
            return new ArrayList<>();
        }
    }

    public void setPatterns(List<Pattern> patterns) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta instanceof BannerMeta) {
            ((BannerMeta) itemMeta).setPatterns(patterns);
        }
        else if (itemMeta instanceof BlockStateMeta) {
            try {
                Banner banner = (Banner) ((BlockStateMeta) itemMeta).getBlockState();
                banner.setPatterns(patterns);
                banner.update();
                ((BlockStateMeta) itemMeta).setBlockState(banner);
            }
            catch (Exception ex) {
                Debug.echoError("Banner setPatterns failed!");
                Debug.echoError(ex);
            }
        }
        else {
            // ...???
        }
        item.setItemMeta(itemMeta);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.patterns>
        // @returns ListTag
        // @group properties
        // @mechanism ItemTag.patterns
        // @description
        // Lists a banner's patterns in the form "COLOR/PATTERN|COLOR/PATTERN" etc.
        // For the list of possible colors, see <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/DyeColor.html>.
        // For the list of possible patterns, see <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/banner/PatternType.html>.
        // -->
        if (attribute.startsWith("patterns")) {
            return listPatterns().getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public String getPropertyString() {
        ListTag list = listPatterns();
        if (list.isEmpty()) {
            return null;
        }
        return list.identify();
    }

    @Override
    public String getPropertyId() {
        return "patterns";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name patterns
        // @input ListTag
        // @description
        // Changes the patterns of a banner. Input must be in the form
        // "COLOR/PATTERN|COLOR/PATTERN" etc.
        // For the list of possible colors, see <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/DyeColor.html>.
        // For the list of possible patterns, see <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/banner/PatternType.html>.
        // @tags
        // <ItemTag.patterns>
        // <server.pattern_types>
        // -->
        if (mechanism.matches("patterns")) {
            List<Pattern> patterns = new ArrayList<>();
            ListTag list = mechanism.valueAsType(ListTag.class);
            List<String> split;
            for (String string : list) {
                try {
                    split = CoreUtilities.split(string, '/', 2);
                    patterns.add(new Pattern(DyeColor.valueOf(split.get(0).toUpperCase()),
                            PatternType.valueOf(split.get(1).toUpperCase())));
                }
                catch (Exception e) {
                    Debug.echoError("Could not apply pattern to banner: " + string);
                }
            }
            setPatterns(patterns);
        }
    }
}

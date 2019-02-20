package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.utilities.MaterialCompat;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemPatterns implements Property {

    public static boolean describes(dObject item) {
        if (item instanceof dItem) {
            Material material = ((dItem) item).getItemStack().getType();
            return MaterialCompat.isBannerOrShield(material);
        }
        return false;
    }

    public static ItemPatterns getFrom(dObject item) {
        if (!describes(item)) {
            return null;
        }
        else {
            return new ItemPatterns((dItem) item);
        }
    }

    public static final String[] handledTags = new String[] {
            "patterns"
    };

    public static final String[] handledMechs = new String[] {
            "patterns"
    };


    private ItemPatterns(dItem item) {
        this.item = item;
    }

    dItem item;

    private dList listPatterns() {
        dList list = new dList();
        for (Pattern pattern : getPatterns()) {
            list.add(pattern.getColor().name() + "/" + pattern.getPattern().name());
        }
        return list;
    }

    private List<Pattern> getPatterns() {
        ItemMeta itemMeta = item.getItemStack().getItemMeta();
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

    private void setPatterns(List<Pattern> patterns) {
        ItemStack itemStack = item.getItemStack();
        ItemMeta itemMeta = itemStack.getItemMeta();
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
                dB.echoError("Banner setPatterns failed!");
                dB.echoError(ex);
            }
        }
        else {
            // ...???
        }
        itemStack.setItemMeta(itemMeta);
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <i@item.patterns>
        // @returns dList
        // @group properties
        // @mechanism dItem.patterns
        // @description
        // Lists a banner's patterns in the form "li@COLOR/PATTERN|COLOR/PATTERN" etc.
        // TODO: Local meta for these links
        // For the list of possible colors, see <@link url http://bit.ly/1dydq12>.
        // For the list of possible patterns, see <@link url http://bit.ly/1MqRn7T>.
        // -->
        if (attribute.startsWith("patterns")) {
            return listPatterns().getAttribute(attribute.fulfill(1));
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        dList list = listPatterns();
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
        // @object dItem
        // @name patterns
        // @input dList
        // @description
        // Changes the patterns of a banner. Input must be in the form
        // "li@COLOR/PATTERN|COLOR/PATTERN" etc.
        // For the list of possible colors, see <@link url http://bit.ly/1dydq12>.
        // For the list of possible patterns, see <@link url http://bit.ly/1MqRn7T>.
        // @tags
        // <i@item.patterns>
        // <server.list_patterns>
        // -->

        if (mechanism.matches("patterns")) {
            List<Pattern> patterns = new ArrayList<>();
            dList list = mechanism.valueAsType(dList.class);
            List<String> split;
            for (String string : list) {
                try {
                    split = CoreUtilities.split(string, '/', 2);
                    patterns.add(new Pattern(DyeColor.valueOf(split.get(0).toUpperCase()),
                            PatternType.valueOf(split.get(1).toUpperCase())));
                }
                catch (Exception e) {
                    dB.echoError("Could not apply pattern to banner: " + string);
                }
            }
            setPatterns(patterns);
        }
    }
}

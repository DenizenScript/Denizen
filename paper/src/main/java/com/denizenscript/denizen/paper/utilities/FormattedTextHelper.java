package com.denizenscript.denizen.paper.utilities;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.paper.properties.PaperElementExtensions;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.core.ColorTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.utilities.AsciiMatcher;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.*;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class FormattedTextHelper {

    // <--[language]
    // @name Denizen Text Formatting
    // @group Denizen Magic
    // @description
    // Denizen provides a variety of special chat format options like "on_hover" and "on_click".
    // These options exist within Denizen and do not appear in the historical Minecraft legacy chat format that most plugins and systems read.
    // That legacy system has 16 colors (0-9, A-F) and a few toggleable formats (bold, italic, etc). It does not contain anything that needs more than just an on/off.
    //
    // Modern Minecraft, however, supports a JSON based "raw" message format that can do click events, hover events, full RGB colors, etc.
    //
    // Denizen therefore has its own internal system that works like the legacy format system, but also supports the new options normally only available as 'raw JSON'.
    //
    // Because it is entirely processed within Denizen, these options only work within Denizen, when performing actions that support raw JSON input.
    // This magic tool exists to let you write messages without having to write the messy JSON.
    //
    // Be aware that many inputs do not support raw JSON, and as such are limited only the historical Minecraft legacy format.
    // Also be aware that click events, hover events, etc. are exclusively limited to the chat bar and the pages of books, as you cannot mouse over anything else.
    //
    // Also note that RGB colors use a format that Spigot invented, meaning they will work in places that use Spigot's parser OR Denizen's version, but nowhere that uses the vanilla format still.
    //
    // Thanks to Paper's implementation of component APIs where Spigot was too lazy to, Paper servers have advanced text formatting available in more areas.
    // -->

    public static AsciiMatcher needsEscapeMatcher = new AsciiMatcher("&;[]");
    public static final char LEGACY_SECTION = LegacyComponentSerializer.SECTION_CHAR;

    public enum LegacyColor {
        BLACK('0', NamedTextColor.BLACK),
        DARK_BLUE('1', NamedTextColor.DARK_BLUE),
        DARK_GREEN('2', NamedTextColor.DARK_GREEN),
        DARK_AQUA('3', NamedTextColor.DARK_AQUA),
        DARK_RED('4', NamedTextColor.DARK_RED),
        DARK_PURPLE('5', NamedTextColor.DARK_PURPLE),
        GOLD('6', NamedTextColor.GOLD),
        GRAY('7', NamedTextColor.GRAY),
        DARK_GRAY('8', NamedTextColor.DARK_GRAY),
        BLUE('9', NamedTextColor.BLUE),
        GREEN('a', NamedTextColor.GREEN),
        AQUA('b', NamedTextColor.AQUA),
        RED('c', NamedTextColor.RED),
        LIGHT_PURPLE('d', NamedTextColor.LIGHT_PURPLE),
        YELLOW('e', NamedTextColor.YELLOW),
        WHITE('f', NamedTextColor.WHITE);

        public final char colorChar;
        public final String colorString;
        public final NamedTextColor color;

        LegacyColor(char colorChar, NamedTextColor color) {
            this.colorChar = colorChar;
            this.colorString = new String(new char[]{LEGACY_SECTION, colorChar});
            this.color = color;
        }

        @Override
        public String toString() {
            return colorString;
        }

        private static int calculateIndex(char colorChar) {
            if (colorChar >= '0' && colorChar <= '9') {
                return colorChar - '0';
            }
            else if (colorChar >= 'a' && colorChar <= 'f') {
                return colorChar - 'a' + 10;
            }
            else {
                return -1;
            }
        }

        public static NamedTextColor fromChar(char colorChar) {
            int index = calculateIndex(colorChar);
            return index != -1 ? FROM_LEGACY[index].color : null;
        }

        public static LegacyColor legacyFromChar(char colorChar) {
            int index = calculateIndex(colorChar);
            return index != -1 ? FROM_LEGACY[index] : null;
        }

        public static LegacyColor fromModern(NamedTextColor textColor) {
            return TO_LEGACY.get(textColor);
        }

        private static final Map<NamedTextColor, LegacyColor> TO_LEGACY = new IdentityHashMap<>(16);
        private static final LegacyColor[] FROM_LEGACY = new LegacyColor[16];

        static {
            for (LegacyColor legacyColor : values()) {
                TO_LEGACY.put(legacyColor.color, legacyColor);
                FROM_LEGACY[calculateIndex(legacyColor.colorChar)] = legacyColor;
            }
        }
    }

    public enum LegacyFormatting {
        BOLD('l'),
        ITALIC('o'),
        STRIKETHROUGH('m'),
        UNDERLINE('n'),
        OBFUSCATED('k'),
        RESET('r');

        public final String formatString;

        LegacyFormatting(char formatChar) {
            this.formatString = new String(new char[]{LEGACY_SECTION, formatChar});
        }

        @Override
        public String toString() {
            return formatString;
        }
    }

    public static String escape(String input) {
        if (needsEscapeMatcher.containsAnyMatch(input)) {
            input = input.replace("&", "&amp").replace(";", "&sc").replace("[", "&lb").replace("]", "&rb").replace("\n", "&nl");
        }
        return input.replace(String.valueOf(LEGACY_SECTION), "&ss");
    }

    public static String unescape(String input) {
        if (input.indexOf('&') != -1) {
            return input.replace("&sc", ";").replace("&lb", "[").replace("&rb", "]").replace("&nl", "\n").replace("&ss", String.valueOf(LEGACY_SECTION)).replace("&amp", "&");
        }
        return input;
    }

    public static boolean hasRootFormat(Component component) {
        if (component == null) {
            return false;
        }
        if (component.hasStyling()) {
            return true;
        }
        if (!(component instanceof TextComponent textComponent)) {
            return false;
        }
        if (!textComponent.content().isEmpty()) {
            return false;
        }
        List<Component> children = component.children();
        if (children.isEmpty()) {
            return false;
        }
        return hasRootFormat(children.get(0));
    }

    public static String stringify(Component component) {
        if (component == null) {
            return null;
        }
        String output = stringifySub(component);
        if (hasRootFormat(component)) {
            output = RESET + output;
        }
        while (output.endsWith(RESET)) {
            output = output.substring(0, output.length() - RESET.length());
        }
        while (output.startsWith(POSSIBLE_RESET_PREFIX) && output.length() > 4 && colorCodeInvalidator.isMatch(output.charAt(3))) {
            output = output.substring(2);
        }
        return cleanRedundantCodes(output);
    }

    public static String stringifyRGBSpigot(String hex) {
        StringBuilder hexBuilder = new StringBuilder(7);
        hexBuilder.append('x');
        for (int i = hex.length(); i < 6; i++) {
            hexBuilder.append('0');
        }
        hexBuilder.append(hex);
        hex = hexBuilder.toString();
        StringBuilder outColor = new StringBuilder();
        for (char c : hex.toCharArray()) {
            outColor.append(LEGACY_SECTION).append(c);
        }
        return outColor.toString();
    }

    public static String stringifySub(Component component) {
        return stringifySub(component, null);
    }

    public static String stringifySub(Component component, TextColor parentColor) {
        if (component == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder(128);
        TextColor color = component.color();
        if (color == null) {
            color = parentColor;
        }
        if (color != null) {
            if (color instanceof NamedTextColor namedTextColor) {
                builder.append(LegacyColor.fromModern(namedTextColor));
            }
            else {
                builder.append(stringifyRGBSpigot(color.asHexString().substring(1)));
            }
        }
        if (component.hasDecoration(TextDecoration.BOLD)) {
            builder.append(LegacyFormatting.BOLD);
        }
        if (component.hasDecoration(TextDecoration.ITALIC)) {
            builder.append(LegacyFormatting.ITALIC);
        }
        if (component.hasDecoration(TextDecoration.STRIKETHROUGH)) {
            builder.append(LegacyFormatting.STRIKETHROUGH);
        }
        if (component.hasDecoration(TextDecoration.UNDERLINED)) {
            builder.append(LegacyFormatting.UNDERLINE);
        }
        if (component.hasDecoration(TextDecoration.OBFUSCATED)) {
            builder.append(LegacyFormatting.OBFUSCATED);
        }
        boolean hasFont = component.font() != null;
        if (hasFont) {
            builder.append(LEGACY_SECTION).append("[font=").append(component.font()).append("]");
        }
        boolean hasInsertion = component.insertion() != null;
        if (hasInsertion) {
            builder.append(LEGACY_SECTION).append("[insertion=").append(escape(component.insertion())).append("]");
        }
        boolean hasHover = component.hoverEvent() != null;
        if (hasHover) {
            HoverEvent<?> hover = component.hoverEvent();
            builder.append(LEGACY_SECTION).append("[hover=").append(hover.action()).append(";").append(escape(HoverFormatHelper.stringForHover(hover))).append("]");
        }
        boolean hasClick = component.clickEvent() != null;
        if (hasClick) {
            ClickEvent click = component.clickEvent();
            // TODO modern click events
            builder.append(LEGACY_SECTION).append("[click=").append(click.action().name()).append(";").append(escape(click.value())).append("]");
        }
        if (component instanceof TextComponent textComponent) {
            builder.append(textComponent.content());
        }
        else if (component instanceof TranslatableComponent translatableComponent) {
            MapTag map = new MapTag();
            map.putObject("key", new ElementTag(translatableComponent.key(), true));
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20) && translatableComponent.fallback() != null) {
                map.putObject("fallback", new ElementTag(translatableComponent.fallback(), true));
            }
            if (!translatableComponent.arguments().isEmpty()) {
                map.putObject("with", new ListTag(translatableComponent.arguments(), argument -> new ElementTag(stringifySub(argument.asComponent()), true)));
            }
            builder.append(LEGACY_SECTION).append("[translate=").append(escape(map.savable())).append(']');
        }
        else if (component instanceof SelectorComponent) {
            // TODO separator
            builder.append(LEGACY_SECTION).append("[selector=").append(escape(((SelectorComponent) component).pattern())).append("]");
        }
        else if (component instanceof KeybindComponent) {
            builder.append(LEGACY_SECTION).append("[keybind=").append(escape(((KeybindComponent) component).keybind())).append("]");
        }
        else if (component instanceof ScoreComponent) {
            // TODO value is deprecated
            builder.append(LEGACY_SECTION).append("[score=").append(escape(((ScoreComponent) component).name()))
                    .append(";").append(escape(((ScoreComponent) component).objective()))
                    .append(";").append(escape(((ScoreComponent) component).value())).append("]");
        }
        for (Component afterComponent : component.children()) {
            builder.append(stringifySub(afterComponent, color));
        }
        if (hasClick) {
            builder.append(LEGACY_SECTION + "[/click]");
        }
        if (hasHover) {
            builder.append(LEGACY_SECTION + "[/hover]");
        }
        if (hasInsertion) {
            builder.append(LEGACY_SECTION + "[/insertion]");
        }
        if (hasFont) {
            builder.append(LEGACY_SECTION + "[reset=font]");
        }
        builder.append(RESET);
        String output = builder.toString();
        return cleanRedundantCodes(output);
    }

    public static final String RESET = LegacyFormatting.RESET.toString(), POSSIBLE_RESET_PREFIX = RESET + LEGACY_SECTION;

    private static void copyDecoration(TextDecoration decoration, StyleGetter origin, StyleSetter<?> destination, boolean optimize) {
        TextDecoration.State state = origin.decoration(decoration);
        if (state == TextDecoration.State.NOT_SET) {
            return;
        }
        if (optimize && state == TextDecoration.State.FALSE) {
            return;
        }
        destination.decoration(decoration, state);
    }

    public static TextComponent.Builder copyFormatToNewText(TextComponent.Builder last, boolean minimize) {
        TextComponent.Builder toRet = Component.text();
        Component lastBuilt = last.build();
        copyDecoration(TextDecoration.OBFUSCATED, lastBuilt, toRet, minimize);
        copyDecoration(TextDecoration.BOLD, lastBuilt, toRet, minimize);
        copyDecoration(TextDecoration.STRIKETHROUGH, lastBuilt, toRet, minimize);
        copyDecoration(TextDecoration.UNDERLINED, lastBuilt, toRet, minimize);
        copyDecoration(TextDecoration.ITALIC, lastBuilt, toRet, minimize);
        toRet.color(lastBuilt.color());
        return toRet;
    }

    public static Component parse(String str, TextColor baseColor) {
        if (str == null) {
            return null;
        }
        return parse(str, baseColor, true);
    }

    public static int findNextNormalColorSymbol(String base, int startAt) {
        while (true) {
            int next = base.indexOf(LEGACY_SECTION, startAt);
            if (next == -1 || next + 1 >= base.length()) {
                return -1;
            }
            char after = base.charAt(next + 1);
            if (colorCodeInvalidator.isMatch(after)) {
                return next;
            }
            startAt = next + 1;
        }
    }

    public static int findEndIndexFor(String base, String startSymbol, String endSymbol, int startAt) {
        int layers = 1;
        while (true) {
            int next = base.indexOf(LEGACY_SECTION, startAt);
            if (next == -1) {
                return -1;
            }
            if (next + endSymbol.length() >= base.length()) {
                return -1;
            }
            if (base.startsWith(startSymbol, next + 1)) {
                layers++;
            }
            else if (base.startsWith(endSymbol, next + 1)) {
                layers--;
                if (layers == 0) {
                    return next;
                }
            }
            startAt = next + 1;
        }
    }

    public static int findEndIndexFor(String base, String type, int startAt) {
        return findEndIndexFor(base, "[" + type + "=", "[/" + type + "]", startAt);
    }

    public static String HEX = "0123456789abcdefABCDEF";

    public static AsciiMatcher allowedCharCodes = new AsciiMatcher(HEX + "klmnorxKLMNORX[");

    public static AsciiMatcher hexMatcher = new AsciiMatcher(HEX);

    public static AsciiMatcher colorCodesOrReset = new AsciiMatcher(HEX + "rR"); // Any color code that can be invalidated

    public static AsciiMatcher colorCodeInvalidator = new AsciiMatcher(HEX + "rRxX"); // Any code that can invalidate the colors above

    public static String cleanRedundantCodes(String str) {
        int index = str.indexOf(LEGACY_SECTION);
        if (index == -1) {
            return str;
        }
        int start = 0;
        StringBuilder output = new StringBuilder(str.length());
        while (index != -1) {
            output.append(str, start, index);
            start = index;
            if (index + 1 >= str.length()) {
                break;
            }
            char symbol = str.charAt(index + 1);
            if (allowedCharCodes.isMatch(symbol)) {
                if (symbol == 'x' || symbol == 'X') { // Skip entire hex block
                    index = str.indexOf(LEGACY_SECTION, index + 14);
                    continue;
                }
                int nextIndex = str.indexOf(LEGACY_SECTION, index + 1);
                if (colorCodesOrReset.isMatch(symbol) && nextIndex == index + 2 && nextIndex + 1 < str.length()) {
                    char nextSymbol = str.charAt(nextIndex + 1);
                    if (colorCodeInvalidator.isMatch(nextSymbol)) {
                        start = index + 2; // Exclude from output the initial (redundant) color code
                        index = nextIndex;
                        continue;
                    }
                }
            }
            index = str.indexOf(LEGACY_SECTION, index + 1);
        }
        output.append(str, start, str.length());
        return output.toString();
    }

    public static final Style CLEAN_BASE_STYLE = Style.style()
            .decoration(TextDecoration.BOLD, false)
            .decoration(TextDecoration.ITALIC, false)
            .decoration(TextDecoration.STRIKETHROUGH, false)
            .decoration(TextDecoration.UNDERLINED, false)
            .decoration(TextDecoration.OBFUSCATED, false)
            .build();

    public static TextComponent.Builder getCleanRef() {
        return Component.text().style(CLEAN_BASE_STYLE);
    }

    public static Component parseSimpleColorsOnly(String str) {
        TextComponent.Builder root = Component.text();
        int firstChar = str.indexOf(LEGACY_SECTION);
        int lastStart = 0;
        if (firstChar > 0) {
            root.append(Component.text(str.substring(0, firstChar)));
            lastStart = firstChar;
        }
        TextComponent.Builder nextText = Component.text();
        while (firstChar != -1 && firstChar + 1 < str.length()) {
            char c = str.charAt(firstChar + 1);
            if (allowedCharCodes.isMatch(c)) {
                if (c == 'r' || c == 'R') {
                    nextText.content(str.substring(lastStart, firstChar));
                    if (!nextText.content().isEmpty()) {
                        root.append(nextText);
                    }
                    nextText = getCleanRef();
                    lastStart = firstChar + 2;
                }
                else if (c == 'X' || c == 'x' && firstChar + 13 < str.length()) {
                    StringBuilder color = new StringBuilder(12);
                    color.append("#");
                    for (int i = 1; i <= 6; i++) {
                        if (str.charAt(firstChar + i * 2) != LEGACY_SECTION) {
                            color = null;
                            break;
                        }
                        char hexChar = str.charAt(firstChar + 1 + i * 2);
                        if (!hexMatcher.isMatch(hexChar)) {
                            color = null;
                            break;
                        }
                        color.append(hexChar);
                    }
                    if (color != null) {
                        nextText.content(str.substring(lastStart, firstChar));
                        if (!nextText.content().isEmpty()) {
                            root.append(nextText);
                        }
                        nextText = getCleanRef();
                        nextText.color(TextColor.fromHexString(CoreUtilities.toUpperCase(color.toString())));
                        firstChar += 12;
                        lastStart = firstChar + 2;
                    }
                }
                else if (colorCodesOrReset.isMatch(c)) {
                    nextText.content(str.substring(lastStart, firstChar));
                    if (!nextText.content().isEmpty()) {
                        root.append(nextText);
                    }
                    nextText = getCleanRef();
                    nextText.color(LegacyColor.fromChar(c));
                    lastStart = firstChar + 2;
                }
                else { // format code
                    nextText.content(str.substring(lastStart, firstChar));
                    if (!nextText.content().isEmpty()) {
                        root.append(nextText);
                    }
                    nextText = copyFormatToNewText(nextText, false);
                    if (c == 'k' || c == 'K') {
                        nextText.decoration(TextDecoration.OBFUSCATED, true);
                    }
                    else if (c == 'l' || c == 'L') {
                        nextText.decoration(TextDecoration.BOLD, true);
                    }
                    else if (c == 'm' || c == 'M') {
                        nextText.decoration(TextDecoration.STRIKETHROUGH, true);
                    }
                    else if (c == 'n' || c == 'N') {
                        nextText.decoration(TextDecoration.UNDERLINED, true);
                    }
                    else if (c == 'o' || c == 'O') {
                        nextText.decoration(TextDecoration.ITALIC, true);
                    }
                    lastStart = firstChar + 2;
                }
            }
            firstChar = str.indexOf(LEGACY_SECTION, firstChar + 1);
        }
        if (lastStart < str.length()) {
            nextText.content(str.substring(lastStart));
            root.append(nextText);
        }
        return root.build();
    }

    public static Component parse(String str, TextColor baseColor, boolean cleanBase) {
        if (str == null) {
            return null;
        }
        try {
            return parseInternal(str, baseColor, cleanBase, false);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
        return Component.text(str);
    }

    // TODO cleanup handling here?
    private static Component parseTranslatable(String str, TextColor baseColor, boolean optimize) {
        if (!str.startsWith("map@")) {
            List<String> innardParts = CoreUtilities.split(str, ';');
            String translation = unescape(innardParts.get(0));
            if (innardParts.size() == 1) {
                return Component.translatable(translation);
            }
            List<Component> args = new ArrayList<>(innardParts.size() - 1);
            for (int i = 1; i < innardParts.size(); i++) {
                args.add(parseInternal(unescape(innardParts.get(i)), baseColor, false, optimize));
            }
            return Component.translatable(translation, args);
        }
        MapTag map = MapTag.valueOf(unescape(str), CoreUtilities.noDebugContext);
        if (map == null) {
            return Component.text(str);
        }
        ElementTag translationKey = map.getElement("key");
        if (translationKey == null) {
            return Component.text(str);
        }
        ListTag withList = map.getObjectAs("with", ListTag.class, CoreUtilities.noDebugContext);
        List<Component> args;
        if (withList != null) {
            args = new ArrayList<>(withList.size());
            for (String with : withList) {
                args.add(parseInternal(with, baseColor, false, optimize));
            }
        }
        else {
            args = List.of();
        }
        if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_19)) {
            return Component.translatable(translationKey.asString(), args);
        }
        ElementTag fallback = map.getElement("fallback");
        return Component.translatable(translationKey.asString(), fallback != null ? fallback.asString() : null, args, List.of());
    }

    public static Component parseInternal(String str, TextColor baseColor, boolean cleanBase, boolean optimize) {
        str = CoreUtilities.clearNBSPs(str);
        int firstChar = str.indexOf(LEGACY_SECTION);
        if (firstChar == -1) {
            if (str.contains("://")) {
                firstChar = 0;
            }
            else {
                // This is for compact with how Spigot does parsing of plaintext.
                return Component.textOfChildren(Component.text(str));
            }
        }
        str = cleanRedundantCodes(str);
        if (cleanBase && str.length() < 512) {
            if (!str.contains(LEGACY_SECTION + "[") && !str.contains("://")) {
                return parseSimpleColorsOnly(str);
            }
            // Ensure compact with certain weird vanilla translate strings.
            if (str.startsWith(LEGACY_SECTION + "[translate=") && str.indexOf(']') == str.length() - 1) {
                return parseTranslatable(str.substring("&[translate=".length(), str.length() - 1), baseColor, optimize);
            }
            if (str.length() > 3 && str.startsWith(LEGACY_SECTION + "") && hexMatcher.isMatch(str.charAt(1))
                    && str.startsWith(LEGACY_SECTION + "[translate=", 2) && str.indexOf(']') == str.length() - 1) { // eg "&6&[translate=block.minecraft.ominous_banner]"
                Component component = parseTranslatable(str.substring("&[translate=".length() + 2, str.length() - 1), baseColor, optimize);
                return component.color(LegacyColor.fromChar(str.charAt(1)));
            }
        }
        if (!optimize) {
            optimize = str.contains(LEGACY_SECTION + "[optimize=true]");
        }
        TextComponent.Builder root = Component.text();
        TextComponent.Builder base;
        if (cleanBase && !optimize) {
            base = getCleanRef();
            base.color(baseColor);
            if (firstChar > 0) {
                root.append(Component.text(str.substring(0, firstChar)));
            }
        }
        else {
            base = Component.text();
            base.content(str.substring(0, firstChar));
        }
        str = str.substring(firstChar);
        char[] chars = str.toCharArray();
        int started = 0;
        TextComponent.Builder nextText = Component.text();
        TextComponent.Builder lastText;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == LEGACY_SECTION && i + 1 < chars.length) {
                char code = chars[i + 1];
                if (!allowedCharCodes.isMatch(code)) {
                    continue;
                }
                if (code == '[') {
                    int endBracket = str.indexOf(']', i + 2);
                    if (endBracket == -1) {
                        continue;
                    }
                    String innards = str.substring(i + 2, endBracket);
                    List<String> innardParts = CoreUtilities.split(innards, ';');
                    List<String> innardBase = CoreUtilities.split(innardParts.get(0), '=', 2);
                    innardParts.remove(0);
                    String innardType = CoreUtilities.toLowerCase(innardBase.get(0));
                    if (innardBase.size() == 2) {
                        nextText.content(nextText.content() + str.substring(started, i));
                        lastText = nextText;
                        nextText = copyFormatToNewText(lastText, optimize);
                        nextText.content("");
                        if (innardType.equals("score") && innardParts.size() == 2) {
                            // TODO value is no longer a thing?
                            ScoreComponent component = Component.score(unescape(innardBase.get(1)), unescape(innardParts.get(0)), unescape(innardParts.get(1)));
                            lastText.append(component);
                        }
                        else if (innardType.equals("keybind") && Utilities.matchesNamespacedKeyButCaseInsensitive(innardBase.get(1))) {
                            KeybindComponent component = Component.keybind(unescape(innardBase.get(1)));
                            lastText.append(component);
                        }
                        else if (innardType.equals("selector")) {
                            SelectorComponent component = Component.selector(unescape(innardBase.get(1)));
                            lastText.append(component);
                        }
                        else if (innardType.equals("translate")) {
                            lastText.append(parseTranslatable(innards.substring("translate=".length()), baseColor, optimize));
                        }
                        else if (innardType.equals("click") && innardParts.size() == 1) {
                            int endIndex = findEndIndexFor(str, "click", endBracket);
                            if (endIndex == -1) {
                                continue;
                            }
                            TextComponent.Builder clickableText = Component.text();
                            ClickEvent.Action action = ElementTag.asEnum(ClickEvent.Action.class, innardBase.get(1));
                            // TODO click event types
                            clickableText.clickEvent(ClickEvent.clickEvent(action == null ? ClickEvent.Action.SUGGEST_COMMAND : action, unescape(innardParts.get(0))));
                            clickableText.append(parseInternal(str.substring(endBracket + 1, endIndex), baseColor, false, optimize));
                            lastText.append(clickableText);
                            endBracket = endIndex + "&[/click".length();
                        }
                        else if (innardType.equals("hover")) {
                            int endIndex = findEndIndexFor(str, "hover", endBracket);
                            if (endIndex == -1) {
                                continue;
                            }
                            TextComponent.Builder hoverableText = Component.text();
                            HoverEvent.Action<?> action = ElementTag.asEnum(HoverEvent.Action.class, innardBase.get(1));
                            if (HoverFormatHelper.processHoverInput(action == null ? HoverEvent.Action.SHOW_TEXT : action, hoverableText, innardParts.get(0))) {
                                continue;
                            }
                            hoverableText.append(parseInternal(str.substring(endBracket + 1, endIndex), baseColor, false, optimize));
                            lastText.append(hoverableText);
                            endBracket = endIndex + "&[/hover".length();
                        }
                        else if (innardType.equals("insertion")) {
                            int endIndex = str.indexOf(LEGACY_SECTION + "[/insertion]", i);
                            int backupEndIndex = str.indexOf(LEGACY_SECTION + "[insertion=", i + 5);
                            if (backupEndIndex > 0 && backupEndIndex < endIndex) {
                                endIndex = backupEndIndex;
                            }
                            if (endIndex == -1) {
                                continue;
                            }
                            TextComponent.Builder insertableText = Component.text();
                            insertableText.insertion(unescape(innardBase.get(1)));
                            insertableText.append(parseInternal(str.substring(endBracket + 1, endIndex), baseColor, false, optimize));
                            lastText.append(insertableText);
                            endBracket = endIndex + "&[/insertion".length();
                        }
                        else if (innardType.equals("reset")) {
                            if (innardBase.get(1).length() == 1) {
                                char subCode = innardBase.get(1).charAt(0);
                                if (subCode == 'k' || subCode == 'K') {
                                    nextText.decoration(TextDecoration.OBFUSCATED, false);
                                }
                                else if (subCode == 'l' || subCode == 'L') {
                                    nextText.decoration(TextDecoration.BOLD, false);
                                }
                                else if (subCode == 'm' || subCode == 'M') {
                                    nextText.decoration(TextDecoration.STRIKETHROUGH, false);
                                }
                                else if (subCode == 'n' || subCode == 'N') {
                                    nextText.decoration(TextDecoration.UNDERLINED, false);
                                }
                                else if (subCode == 'o' || subCode == 'O') {
                                    nextText.decoration(TextDecoration.ITALIC, false);
                                }
                            }
                            else if (innardBase.get(1).equals("font")) {
                                // TODO builder
                                nextText.font(base.build().font());
                            }
                            else {
                                // TODO builder
                                nextText.color(base.build().color());
                            }
                        }
                        else if (innardType.equals("color")) {
                            String colorChar = innardBase.get(1);
                            TextColor color = null;
                            if (colorChar.length() == 1) {
                                color = LegacyColor.fromChar(colorChar.charAt(0));
                            }
                            else if (colorChar.length() == 7) {
                                color = TextColor.fromHexString(CoreUtilities.toUpperCase(colorChar));
                            }
                            else if (CoreConfiguration.debugVerbose) {
                                Debug.echoError("Text parse issue: cannot interpret color '" + innardBase.get(1) + "'.");
                            }
                            if (color != null) {
                                int endIndex = findEndIndexFor(str, "[color=", "[reset=color]", endBracket);
                                if (endIndex == -1) {
                                    nextText.color(color);
                                }
                                else {
                                    TextComponent.Builder colorText = Component.text();
                                    colorText.color(color);
                                    colorText.append(parseInternal(str.substring(endBracket + 1, endIndex), color, false, optimize));
                                    lastText.append(colorText);
                                    endBracket = endIndex + "&[reset=color".length();
                                }
                            }
                        }
                        else if (innardType.equals("gradient") && innardParts.size() == 2) {
                            String from = innardBase.get(1), to = innardParts.get(0), style = innardParts.get(1);
                            ColorTag fromColor = ColorTag.valueOf(from, CoreUtilities.noDebugContext);
                            ColorTag toColor = ColorTag.valueOf(to, CoreUtilities.noDebugContext);
                            PaperElementExtensions.GradientStyle styleEnum = new ElementTag(style).asEnum(PaperElementExtensions.GradientStyle.class);
                            if (fromColor == null || toColor == null || styleEnum == null) {
                                if (CoreConfiguration.debugVerbose) {
                                    Debug.echoError("Text parse issue: cannot interpret gradient input '" + innards + "'.");
                                }
                            }
                            else {
                                int endIndex = findNextNormalColorSymbol(str, i + 1);
                                if (endIndex == -1) {
                                    endIndex = str.length();
                                }
                                String gradientText = PaperElementExtensions.doGradient(str.substring(endBracket + 1, endIndex), fromColor, toColor, styleEnum);
                                lastText.append(parseInternal(gradientText, baseColor, false, optimize));
                                endBracket = endIndex - 1;
                            }
                        }
                        else if (innardType.equals("font") && Utilities.matchesNamespacedKey(innardBase.get(1))) {
                            int endIndex = findEndIndexFor(str, "[font=", "[reset=font]", endBracket);
                            if (endIndex == -1) {
                                nextText.font(Key.key(innardBase.get(1)));
                            }
                            else {
                                TextComponent.Builder fontText = Component.text();
                                fontText.font(Key.key(innardBase.get(1)));
                                fontText.append(parseInternal(str.substring(endBracket + 1, endIndex), baseColor, false, optimize));
                                lastText.append(fontText);
                                endBracket = endIndex + "&[reset=font".length();
                            }
                        }
                        else if (innardType.equals("optimize")) {
                            // Ignore
                        }
                        else {
                            if (CoreConfiguration.debugVerbose) {
                                Debug.echoError("Text parse issue: cannot interpret type '" + innardType + "' with " + innardParts.size() + " parts.");
                            }
                        }
                        base.append(lastText);
                    }
                    i = endBracket;
                    started = endBracket + 1;
                    continue;
                }
                else if (code == 'r' || code == 'R') {
                    nextText.content(nextText.content() + str.substring(started, i));
                    if (!nextText.content().isEmpty()) {
                        base.append(nextText);
                    }
                    nextText = Component.text();
                    nextText.color(baseColor);
                }
                else if (colorCodesOrReset.isMatch(code)) {
                    nextText.content(nextText.content() + str.substring(started, i));
                    if (!nextText.content().isEmpty()) {
                        base.append(nextText);
                    }
                    nextText = Component.text();
                    nextText.color(LegacyColor.fromChar(code));
                }
                else if (code == 'x') {
                    if (i + 13 >= chars.length) {
                        continue;
                    }
                    StringBuilder color = new StringBuilder(12);
                    color.append("#");
                    for (int c = 1; c <= 6; c++) {
                        if (chars[i + c * 2] != LEGACY_SECTION) {
                            color = null;
                            break;
                        }
                        char hexPart = chars[i + 1 + c * 2];
                        if (!hexMatcher.isMatch(hexPart)) {
                            color = null;
                            break;
                        }
                        color.append(hexPart);
                    }
                    if (color == null) {
                        continue;
                    }
                    nextText.content(nextText.content() + str.substring(started, i));
                    if (!nextText.content().isEmpty()) {
                        base.append(nextText);
                    }
                    nextText = Component.text();
                    nextText.color(TextColor.fromHexString(CoreUtilities.toUpperCase(color.toString())));
                    i += 13;
                    started = i + 1;
                    continue;
                }
                else {
                    nextText.content(nextText.content() + str.substring(started, i));
                    if (!nextText.content().isEmpty()) {
                        base.append(nextText);
                    }
                    nextText = copyFormatToNewText(nextText, optimize);
                    if (code == 'k' || code == 'K') {
                        nextText.decoration(TextDecoration.OBFUSCATED, true);
                    }
                    else if (code == 'l' || code == 'L') {
                        nextText.decoration(TextDecoration.BOLD, true);
                    }
                    else if (code == 'm' || code == 'M') {
                        nextText.decoration(TextDecoration.STRIKETHROUGH, true);
                    }
                    else if (code == 'n' || code == 'N') {
                        nextText.decoration(TextDecoration.UNDERLINED, true);
                    }
                    else if (code == 'o' || code == 'O') {
                        nextText.decoration(TextDecoration.ITALIC, true);
                    }
                }
                i++;
                started = i + 1;
            }
            else if (i + "https://a.".length() < chars.length && chars[i] == 'h' && chars[i + 1] == 't' && chars[i + 2] == 't' && chars[i  + 3] == 'p') {
                String subStr = str.substring(i, i + "https://a.".length());
                if (subStr.startsWith("https://") || subStr.startsWith("http://")) {
                    int nextSpace = CoreUtilities.indexOfAny(str, i, ' ', '\t', '\n', LEGACY_SECTION);
                    if (nextSpace == -1) {
                        nextSpace = str.length();
                    }
                    String url = str.substring(i, nextSpace);
                    nextText.content(nextText.content() + str.substring(started, i));
                    lastText = nextText;
                    // TODO builder copying
                    nextText = lastText.build().toBuilder();
                    nextText.content("");
                    TextComponent.Builder clickableText = Component.text().content(url);
                    clickableText.clickEvent(ClickEvent.openUrl(url));
                    lastText.append(clickableText);
                    base.append(lastText);
                    i = nextSpace - 1;
                    started = nextSpace;
                    continue;
                }
            }
        }
        nextText.content(nextText.content() + str.substring(started));
        if (!nextText.content().isEmpty()) {
            base.append(nextText);
        }
        return cleanBase && !optimize ? root.append(base).build() : base.build();
    }

    public static int indexOfLastColorBlockStart(String text) {
        int result = text.lastIndexOf(LEGACY_SECTION + "[");
        if (result == -1 || text.indexOf(']', result + 2) != -1) {
            return -1;
        }
        return result;
    }

    /**
     * Equivalent to DebugInternals.trimMessage, with a special check:
     * If a message is cut in the middle of a format block like "&[font=x:y]", cut that block entirely out.
     * (This is needed because a snip in the middle of this will explode with parsing errors).
     */
    public static String bukkitSafeDebugTrimming(String message) {
        int trimSize = CoreConfiguration.debugTrimLength;
        if (message.length() > trimSize) {
            int firstCut = (trimSize / 2) - 10, secondCut = message.length() - ((trimSize / 2) - 10);
            String prePart = message.substring(0, firstCut);
            String cutPart = message.substring(firstCut, secondCut);
            String postPart = message.substring(secondCut);
            int preEarlyCut = indexOfLastColorBlockStart(prePart);
            if (preEarlyCut != -1) {
                prePart = message.substring(0, preEarlyCut);
            }
            if (indexOfLastColorBlockStart(cutPart) != -1 || (preEarlyCut != -1 && cutPart.indexOf(']') == -1)) {
                int lateCut = postPart.indexOf(']');
                if (lateCut != -1) {
                    postPart = postPart.substring(lateCut + 1);
                }
            }
            message = prePart + "... *snip!*..." + postPart;
        }
        return message;
    }
}

package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.ColorTag;
import com.denizenscript.denizen.objects.properties.bukkit.BukkitElementExtensions;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.utilities.AsciiMatcher;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;

import java.util.List;

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

    public static String escape(String input) {
        if (needsEscapeMatcher.containsAnyMatch(input)) {
            input = input.replace("&", "&amp").replace(";", "&sc").replace("[", "&lb").replace("]", "&rb").replace("\n", "&nl");
        }
        return input.replace(String.valueOf(ChatColor.COLOR_CHAR), "&ss");
    }

    public static String unescape(String input) {
        if (input.indexOf('&') != -1) {
            return input.replace("&sc", ";").replace("&lb", "[").replace("&rb", "]").replace("&nl", "\n").replace("&ss", String.valueOf(ChatColor.COLOR_CHAR)).replace("&amp", "&");
        }
        return input;
    }

    public static boolean hasRootFormat(BaseComponent component) {
        if (component.hasFormatting()) {
            return true;
        }
        if (!(component instanceof TextComponent)) {
            return false;
        }
        if (!((TextComponent) component).getText().isEmpty()) {
            return false;
        }
        List<BaseComponent> extra = component.getExtra();
        if (extra == null || extra.isEmpty()) {
            return false;
        }
        return hasRootFormat(extra.get(0));
    }

    public static String stringify(BaseComponent[] components) {
        if (components == null) {
            return null;
        }
        if (components.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder(128 * components.length);
        if (hasRootFormat(components[0])) {
            builder.append(RESET);
        }
        for (BaseComponent component : components) {
            builder.append(stringify(component));
        }
        String output = builder.toString();
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
            outColor.append(org.bukkit.ChatColor.COLOR_CHAR).append(c);
        }
        return outColor.toString();
    }

    public static String stringify(BaseComponent component) {
        return stringifySub(component, null);
    }

    public static String stringifySub(BaseComponent component, ChatColor parentColor) {
        if (component == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder(128);
        ChatColor color = component.getColorRaw();
        if (color == null) {
            color = parentColor;
        }
        if (color != null) {
            builder.append(color);
        }
        if (component.isBold()) {
            builder.append(ChatColor.BOLD);
        }
        if (component.isItalic()) {
            builder.append(ChatColor.ITALIC);
        }
        if (component.isStrikethrough()) {
            builder.append(ChatColor.STRIKETHROUGH);
        }
        if (component.isUnderlined()) {
            builder.append(ChatColor.UNDERLINE);
        }
        if (component.isObfuscated()) {
            builder.append(ChatColor.MAGIC);
        }
        if (component.getFontRaw() != null || (component.getFont() != null && component.getColorRaw() != null)) {
            builder.append(ChatColor.COLOR_CHAR).append("[font=").append(component.getFont()).append("]");
        }
        boolean hasInsertion = component.getInsertion() != null;
        if (hasInsertion) {
            builder.append(ChatColor.COLOR_CHAR).append("[insertion=").append(escape(component.getInsertion())).append("]");
        }
        boolean hasHover = component.getHoverEvent() != null;
        if (hasHover) {
            HoverEvent hover = component.getHoverEvent();
            builder.append(ChatColor.COLOR_CHAR).append("[hover=").append(hover.getAction().name()).append(";").append(escape(NMSHandler.instance.stringForHover(hover))).append("]");
        }
        boolean hasClick = component.getClickEvent() != null;
        if (hasClick) {
            ClickEvent click = component.getClickEvent();
            builder.append(ChatColor.COLOR_CHAR).append("[click=").append(click.getAction().name()).append(";").append(escape(click.getValue())).append("]");
        }
        if (component instanceof TextComponent) {
            builder.append(((TextComponent) component).getText());
        }
        else if (component instanceof TranslatableComponent) {
            builder.append(ChatColor.COLOR_CHAR).append("[translate=").append(escape(((TranslatableComponent) component).getTranslate()));
            List<BaseComponent> with = ((TranslatableComponent) component).getWith();
            if (with != null) {
                for (BaseComponent withComponent : with) {
                    builder.append(";").append(escape(stringify(withComponent)));
                }
            }
            builder.append("]");
        }
        else if (component instanceof SelectorComponent) {
            builder.append(ChatColor.COLOR_CHAR).append("[selector=").append(escape(((SelectorComponent) component).getSelector())).append("]");
        }
        else if (component instanceof KeybindComponent) {
            builder.append(ChatColor.COLOR_CHAR).append("[keybind=").append(escape(((KeybindComponent) component).getKeybind())).append("]");
        }
        else if (component instanceof ScoreComponent) {
            builder.append(ChatColor.COLOR_CHAR).append("[score=").append(escape(((ScoreComponent) component).getName()))
                    .append(";").append(escape(((ScoreComponent) component).getObjective()))
                    .append(";").append(escape(((ScoreComponent) component).getValue())).append("]");
        }
        List<BaseComponent> after = component.getExtra();
        if (after != null) {
            for (BaseComponent afterComponent : after) {
                builder.append(stringifySub(afterComponent, color));
            }
        }
        if (hasClick) {
            builder.append(ChatColor.COLOR_CHAR + "[/click]");
        }
        if (hasHover) {
            builder.append(ChatColor.COLOR_CHAR + "[/hover]");
        }
        if (hasInsertion) {
            builder.append(ChatColor.COLOR_CHAR + "[/insertion]");
        }
        builder.append(RESET);
        String output = builder.toString();
        return cleanRedundantCodes(output);
    }

    public static final String RESET = ChatColor.RESET.toString(), POSSIBLE_RESET_PREFIX = RESET + ChatColor.COLOR_CHAR;

    public static TextComponent copyFormatToNewText(TextComponent last) {
        TextComponent toRet = new TextComponent();
        toRet.setObfuscated(last.isObfuscatedRaw());
        toRet.setBold(last.isBoldRaw());
        toRet.setStrikethrough(last.isStrikethroughRaw());
        toRet.setUnderlined(last.isUnderlinedRaw());
        toRet.setItalic(last.isItalicRaw());
        toRet.setColor(last.getColorRaw());
        return toRet;
    }

    public static BaseComponent[] parse(String str, ChatColor baseColor) {
        if (str == null) {
            return null;
        }
        return parse(str, baseColor, true);
    }

    public static int findNextNormalColorSymbol(String base, int startAt) {
        while (true) {
            int next = base.indexOf(ChatColor.COLOR_CHAR, startAt);
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
            int next = base.indexOf(ChatColor.COLOR_CHAR, startAt);
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
        int index = str.indexOf(ChatColor.COLOR_CHAR);
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
                    index = str.indexOf(ChatColor.COLOR_CHAR, index + 14);
                    continue;
                }
                int nextIndex = str.indexOf(ChatColor.COLOR_CHAR, index + 1);
                if (colorCodesOrReset.isMatch(symbol) && nextIndex == index + 2 && nextIndex + 1 < str.length()) {
                    char nextSymbol = str.charAt(nextIndex + 1);
                    if (colorCodeInvalidator.isMatch(nextSymbol)) {
                        start = index + 2; // Exclude from output the initial (redundant) color code
                        index = nextIndex;
                        continue;
                    }
                }
            }
            index = str.indexOf(ChatColor.COLOR_CHAR, index + 1);
        }
        output.append(str, start, str.length());
        return output.toString();
    }

    public static TextComponent getCleanRef() {
        TextComponent reference = new TextComponent();
        reference.setBold(false);
        reference.setItalic(false);
        reference.setStrikethrough(false);
        reference.setUnderlined(false);
        reference.setObfuscated(false);
        return reference;
    }

    public static BaseComponent[] parseSimpleColorsOnly(String str) {
        TextComponent root = new TextComponent();
        int firstChar = str.indexOf(ChatColor.COLOR_CHAR);
        int lastStart = 0;
        if (firstChar > 0) {
            root.addExtra(new TextComponent(str.substring(0, firstChar)));
            lastStart = firstChar;
        }
        TextComponent nextText = new TextComponent();
        while (firstChar != -1 && firstChar + 1 < str.length()) {
            char c = str.charAt(firstChar + 1);
            if (allowedCharCodes.isMatch(c)) {
                if (c == 'r' || c == 'R') {
                    nextText.setText(str.substring(lastStart, firstChar));
                    if (!nextText.getText().isEmpty()) {
                        root.addExtra(nextText);
                    }
                    nextText = getCleanRef();
                    lastStart = firstChar + 2;
                }
                else if (c == 'X' || c == 'x' && firstChar + 13 < str.length()) {
                    StringBuilder color = new StringBuilder(12);
                    color.append("#");
                    for (int i = 1; i <= 6; i++) {
                        if (str.charAt(firstChar + i * 2) != ChatColor.COLOR_CHAR) {
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
                        nextText.setText(str.substring(lastStart, firstChar));
                        if (!nextText.getText().isEmpty()) {
                            root.addExtra(nextText);
                        }
                        nextText = getCleanRef();
                        nextText.setColor(ChatColor.of(color.toString()));
                        firstChar += 12;
                        lastStart = firstChar + 2;
                    }
                }
                else if (colorCodesOrReset.isMatch(c)) {
                    nextText.setText(str.substring(lastStart, firstChar));
                    if (!nextText.getText().isEmpty()) {
                        root.addExtra(nextText);
                    }
                    nextText = getCleanRef();
                    nextText.setColor(ChatColor.getByChar(c));
                    lastStart = firstChar + 2;
                }
                else { // format code
                    nextText.setText(str.substring(lastStart, firstChar));
                    if (!nextText.getText().isEmpty()) {
                        root.addExtra(nextText);
                    }
                    nextText = copyFormatToNewText(nextText);
                    if (c == 'k' || c == 'K') {
                        nextText.setObfuscated(true);
                    }
                    else if (c == 'l' || c == 'L') {
                        nextText.setBold(true);
                    }
                    else if (c == 'm' || c == 'M') {
                        nextText.setStrikethrough(true);
                    }
                    else if (c == 'n' || c == 'N') {
                        nextText.setUnderlined(true);
                    }
                    else if (c == 'o' || c == 'O') {
                        nextText.setItalic(true);
                    }
                    lastStart = firstChar + 2;
                }
            }
            firstChar = str.indexOf(ChatColor.COLOR_CHAR, firstChar + 1);
        }
        if (lastStart < str.length()) {
            nextText.setText(str.substring(lastStart));
            root.addExtra(nextText);
        }
        return new BaseComponent[] { root };
    }

    public static BaseComponent[] parse(String str, ChatColor baseColor, boolean cleanBase) {
        if (str == null) {
            return null;
        }
        try {
            return parseInternal(str, baseColor, cleanBase);
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
        return new BaseComponent[]{new TextComponent(str)};
    }

    public static BaseComponent[] parseInternal(String str, ChatColor baseColor, boolean cleanBase) {
        str = CoreUtilities.clearNBSPs(str);
        int firstChar = str.indexOf(ChatColor.COLOR_CHAR);
        if (firstChar == -1) {
            if (str.contains("://")) {
                firstChar = 0;
            }
            else {
                TextComponent base = new TextComponent();
                base.addExtra(new TextComponent(str)); // This is for compat with how Spigot does parsing of plaintext.
                return new BaseComponent[]{base};
            }
        }
        str = cleanRedundantCodes(str);
        if (cleanBase && str.length() < 512) {
            if (!str.contains(ChatColor.COLOR_CHAR + "[") && !str.contains("://")) {
                return parseSimpleColorsOnly(str);
            }
            // Ensure compat with certain weird vanilla translate strings.
            if (str.startsWith(ChatColor.COLOR_CHAR + "[translate=") && str.indexOf(']') == str.length() - 1) {
                String translatable = str.substring("&[translate=".length(), str.length() - 1);
                TranslatableComponent component = new TranslatableComponent();
                List<String> innardParts = CoreUtilities.split(translatable, ';');
                component.setTranslate(unescape(innardParts.get(0)));
                for (int i = 1; i < innardParts.size(); i++) {
                    for (BaseComponent subComponent : parseInternal(unescape(innardParts.get(i)), baseColor, false)) {
                        component.addWith(subComponent);
                    }
                }
                return new BaseComponent[] { component };
            }
        }
        TextComponent root = new TextComponent();
        TextComponent base = new TextComponent();
        if (cleanBase) {
            base.setBold(false);
            base.setItalic(false);
            base.setStrikethrough(false);
            base.setUnderlined(false);
            base.setObfuscated(false);
            base.setColor(baseColor);
            if (firstChar > 0) {
                root.addExtra(new TextComponent(str.substring(0, firstChar)));
            }
        }
        else {
            base.setText(str.substring(0, firstChar));
        }
        root.addExtra(base);
        str = str.substring(firstChar);
        char[] chars = str.toCharArray();
        int started = 0;
        TextComponent nextText = new TextComponent();
        TextComponent lastText;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == ChatColor.COLOR_CHAR && i + 1 < chars.length) {
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
                        nextText.setText(nextText.getText() + str.substring(started, i));
                        base.addExtra(nextText);
                        lastText = nextText;
                        nextText = copyFormatToNewText(lastText);
                        nextText.setText("");
                        if (innardType.equals("score") && innardParts.size() == 2) {
                            ScoreComponent component = new ScoreComponent(unescape(innardBase.get(1)), unescape(innardParts.get(0)), unescape(innardParts.get(1)));
                            lastText.addExtra(component);
                        }
                        else if (innardType.equals("keybind") && Utilities.matchesNamespacedKey(innardBase.get(1))) {
                            KeybindComponent component = new KeybindComponent();
                            component.setKeybind(unescape(innardBase.get(1)));
                            lastText.addExtra(component);
                        }
                        else if (innardType.equals("selector")) {
                            SelectorComponent component = new SelectorComponent(unescape(innardBase.get(1)));
                            lastText.addExtra(component);
                        }
                        else if (innardType.equals("translate")) {
                            TranslatableComponent component = new TranslatableComponent();
                            component.setTranslate(unescape(innardBase.get(1)));
                            for (String extra : innardParts) {
                                for (BaseComponent subComponent : parseInternal(unescape(extra), baseColor, false)) {
                                    component.addWith(subComponent);
                                }
                            }
                            lastText.addExtra(component);
                        }
                        else if (innardType.equals("click") && innardParts.size() == 1) {
                            int endIndex = findEndIndexFor(str, "click", endBracket);
                            if (endIndex == -1) {
                                continue;
                            }
                            TextComponent clickableText = new TextComponent();
                            ClickEvent.Action action = ElementTag.asEnum(ClickEvent.Action.class, innardBase.get(1));
                            clickableText.setClickEvent(new ClickEvent(action == null ? ClickEvent.Action.SUGGEST_COMMAND : action, unescape(innardParts.get(0))));
                            for (BaseComponent subComponent : parseInternal(str.substring(endBracket + 1, endIndex), baseColor, false)) {
                                clickableText.addExtra(subComponent);
                            }
                            lastText.addExtra(clickableText);
                            endBracket = endIndex + "&[/click".length();
                        }
                        else if (innardType.equals("hover")) {
                            int endIndex = findEndIndexFor(str, "hover", endBracket);
                            if (endIndex == -1) {
                                continue;
                            }
                            TextComponent hoverableText = new TextComponent();
                            HoverEvent.Action action = ElementTag.asEnum(HoverEvent.Action.class, innardBase.get(1));
                            if (HoverFormatHelper.processHoverInput(action == null ? HoverEvent.Action.SHOW_TEXT : action, hoverableText, innardParts.get(0))) {
                                continue;
                            }
                            for (BaseComponent subComponent : parseInternal(str.substring(endBracket + 1, endIndex), baseColor, false)) {
                                hoverableText.addExtra(subComponent);
                            }
                            lastText.addExtra(hoverableText);
                            endBracket = endIndex + "&[/hover".length();
                        }
                        else if (innardType.equals("insertion")) {
                            int endIndex = str.indexOf(ChatColor.COLOR_CHAR + "[/insertion]", i);
                            int backupEndIndex = str.indexOf(ChatColor.COLOR_CHAR + "[insertion=", i + 5);
                            if (backupEndIndex > 0 && backupEndIndex < endIndex) {
                                endIndex = backupEndIndex;
                            }
                            if (endIndex == -1) {
                                continue;
                            }
                            TextComponent insertableText = new TextComponent();
                            insertableText.setInsertion(unescape(innardBase.get(1)));
                            for (BaseComponent subComponent : parseInternal(str.substring(endBracket + 1, endIndex), baseColor, false)) {
                                insertableText.addExtra(subComponent);
                            }
                            lastText.addExtra(insertableText);
                            endBracket = endIndex + "&[/insertion".length();
                        }
                        else if (innardType.equals("reset")) {
                            if (innardBase.get(1).length() == 1) {
                                char subCode = innardBase.get(1).charAt(0);
                                if (subCode == 'k' || subCode == 'K') {
                                    nextText.setObfuscated(false);
                                }
                                else if (subCode == 'l' || subCode == 'L') {
                                    nextText.setBold(false);
                                }
                                else if (subCode == 'm' || subCode == 'M') {
                                    nextText.setStrikethrough(false);
                                }
                                else if (subCode == 'n' || subCode == 'N') {
                                    nextText.setUnderlined(false);
                                }
                                else if (subCode == 'o' || subCode == 'O') {
                                    nextText.setItalic(false);
                                }
                            }
                            else if (innardBase.get(1).equals("font")) {
                                nextText.setFont(base.getFont());
                            }
                            else {
                                nextText.setColor(base.getColor());
                            }
                        }
                        else if (innardType.equals("color")) {
                            String colorChar = innardBase.get(1);
                            ChatColor color = null;
                            if (colorChar.length() == 1) {
                                color = ChatColor.getByChar(colorChar.charAt(0));
                            }
                            else if (colorChar.length() == 7) {
                                color = ChatColor.of(colorChar);
                            }
                            else if (CoreConfiguration.debugVerbose) {
                                Debug.echoError("Text parse issue: cannot interpret color '" + innardBase.get(1) + "'.");
                            }
                            if (color != null) {
                                int endIndex = findEndIndexFor(str, "[color=", "[reset=color]", endBracket);
                                if (endIndex == -1) {
                                    nextText.setColor(color);
                                }
                                else {
                                    TextComponent colorText = new TextComponent();
                                    colorText.setColor(color);
                                    for (BaseComponent subComponent : parseInternal(str.substring(endBracket + 1, endIndex), color, false)) {
                                        colorText.addExtra(subComponent);
                                    }
                                    lastText.addExtra(colorText);
                                    endBracket = endIndex + "&[reset=color".length();
                                }
                            }
                        }
                        else if (innardType.equals("gradient") && innardParts.size() == 2) {
                            String from = innardBase.get(1), to = innardParts.get(0), style = innardParts.get(1);
                            ColorTag fromColor = ColorTag.valueOf(from, CoreUtilities.noDebugContext);
                            ColorTag toColor = ColorTag.valueOf(to, CoreUtilities.noDebugContext);
                            BukkitElementExtensions.GradientStyle styleEnum = new ElementTag(style).asEnum(BukkitElementExtensions.GradientStyle.class);
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
                                String gradientText = BukkitElementExtensions.doGradient(str.substring(endBracket + 1, endIndex), fromColor, toColor, styleEnum);
                                for (BaseComponent subComponent : parseInternal(gradientText, baseColor, false)) {
                                    lastText.addExtra(subComponent);
                                }
                                endBracket = endIndex - 1;
                            }
                        }
                        else if (innardType.equals("font") && Utilities.matchesNamespacedKey(innardBase.get(1))) {
                            int endIndex = findEndIndexFor(str, "[font=", "[reset=font]", endBracket);
                            if (endIndex == -1) {
                                nextText.setFont(innardBase.get(1));
                            }
                            else {
                                TextComponent fontText = new TextComponent();
                                fontText.setFont(innardBase.get(1));
                                for (BaseComponent subComponent : parseInternal(str.substring(endBracket + 1, endIndex), baseColor, false)) {
                                    fontText.addExtra(subComponent);
                                }
                                lastText.addExtra(fontText);
                                endBracket = endIndex + "&[reset=font".length();
                            }
                        }
                        else {
                            if (CoreConfiguration.debugVerbose) {
                                Debug.echoError("Text parse issue: cannot interpret type '" + innardType + "' with " + innardParts.size() + " parts.");
                            }
                        }
                    }
                    i = endBracket;
                    started = endBracket + 1;
                    continue;
                }
                else if (code == 'r' || code == 'R') {
                    nextText.setText(nextText.getText() + str.substring(started, i));
                    if (!nextText.getText().isEmpty()) {
                        base.addExtra(nextText);
                    }
                    nextText = new TextComponent();
                    nextText.setColor(baseColor);
                }
                else if (colorCodesOrReset.isMatch(code)) {
                    nextText.setText(nextText.getText() + str.substring(started, i));
                    if (!nextText.getText().isEmpty()) {
                        base.addExtra(nextText);
                    }
                    nextText = new TextComponent();
                    nextText.setColor(ChatColor.getByChar(code));
                }
                else if (code == 'x') {
                    if (i + 13 >= chars.length) {
                        continue;
                    }
                    StringBuilder color = new StringBuilder(12);
                    color.append("#");
                    for (int c = 1; c <= 6; c++) {
                        if (chars[i + c * 2] != ChatColor.COLOR_CHAR) {
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
                    nextText.setText(nextText.getText() + str.substring(started, i));
                    if (!nextText.getText().isEmpty()) {
                        base.addExtra(nextText);
                    }
                    nextText = new TextComponent();
                    nextText.setColor(ChatColor.of(color.toString()));
                    i += 13;
                    started = i + 1;
                    continue;
                }
                else {
                    nextText.setText(nextText.getText() + str.substring(started, i));
                    if (!nextText.getText().isEmpty()) {
                        base.addExtra(nextText);
                    }
                    nextText = copyFormatToNewText(nextText);
                    if (code == 'k' || code == 'K') {
                        nextText.setObfuscated(true);
                    }
                    else if (code == 'l' || code == 'L') {
                        nextText.setBold(true);
                    }
                    else if (code == 'm' || code == 'M') {
                        nextText.setStrikethrough(true);
                    }
                    else if (code == 'n' || code == 'N') {
                        nextText.setUnderlined(true);
                    }
                    else if (code == 'o' || code == 'O') {
                        nextText.setItalic(true);
                    }
                }
                i++;
                started = i + 1;
            }
            else if (i + "https://a.".length() < chars.length && chars[i] == 'h' && chars[i + 1] == 't' && chars[i + 2] == 't' && chars[i  + 3] == 'p') {
                String subStr = str.substring(i, i + "https://a.".length());
                if (subStr.startsWith("https://") || subStr.startsWith("http://")) {
                    int nextSpace = CoreUtilities.indexOfAny(str, i, ' ', '\t', '\n', ChatColor.COLOR_CHAR);
                    if (nextSpace == -1) {
                        nextSpace = str.length();
                    }
                    String url = str.substring(i, nextSpace);
                    nextText.setText(nextText.getText() + str.substring(started, i));
                    base.addExtra(nextText);
                    lastText = nextText;
                    nextText = new TextComponent(lastText);
                    nextText.setText("");
                    TextComponent clickableText = new TextComponent(url);
                    clickableText.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
                    lastText.addExtra(clickableText);
                    i = nextSpace - 1;
                    started = nextSpace;
                    continue;
                }
            }
        }
        nextText.setText(nextText.getText() + str.substring(started));
        if (!nextText.getText().isEmpty()) {
            base.addExtra(nextText);
        }
        return new BaseComponent[] { cleanBase ? root : base };
    }

    public static int indexOfLastColorBlockStart(String text) {
        int result = text.lastIndexOf(ChatColor.COLOR_CHAR + "[");
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

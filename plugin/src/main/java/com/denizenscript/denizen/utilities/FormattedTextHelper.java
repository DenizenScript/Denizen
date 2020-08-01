package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;

import java.util.ArrayList;
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
    // Also be aware that click events, hover evers, etc. are exclusively limited to the chat bar and the pages of books, as you cannot mouse over anything else.
    // -->


    public static String escape(String input) {
        return input.replace("&", "&amp").replace(";", "&sc").replace("[", "&lb").replace("]", "&rb").replace(String.valueOf(ChatColor.COLOR_CHAR), "&ss");
    }

    public static String unescape(String input) {
        return input.replace("&sc", ";").replace("&lb", "[").replace("&rb", "]").replace("&ss", String.valueOf(ChatColor.COLOR_CHAR)).replace("&amp", "&");
    }

    public static String stringify(BaseComponent[] components) {
        StringBuilder builder = new StringBuilder(128 * components.length);
        for (BaseComponent component : components) {
            builder.append(stringify(component));
        }
        return builder.toString();
    }

    public static boolean boolNotNull(Boolean bool) {
        return bool != null && bool;
    }

    public static String stringifyRGBSpigot(String hex) {
        while (hex.length() < 6) {
            hex = "0" + hex;
        }
        hex = "x" + hex;
        StringBuilder outColor = new StringBuilder();
        for (char c : hex.toCharArray()) {
            outColor.append(org.bukkit.ChatColor.COLOR_CHAR).append(c);
        }
        return outColor.toString();
    }

    public static String stringify(BaseComponent component) {
        StringBuilder builder = new StringBuilder(128);
        ChatColor color = component.getColorRaw();
        if (color != null) {
            builder.append(color.toString());
        }
        if (boolNotNull(component.isBoldRaw())) {
            builder.append(ChatColor.BOLD.toString());
        }
        if (boolNotNull(component.isItalicRaw())) {
            builder.append(ChatColor.ITALIC.toString());
        }
        if (boolNotNull(component.isStrikethroughRaw())) {
            builder.append(ChatColor.STRIKETHROUGH.toString());
        }
        if (boolNotNull(component.isUnderlinedRaw())) {
            builder.append(ChatColor.UNDERLINE.toString());
        }
        if (boolNotNull(component.isObfuscatedRaw())) {
            builder.append(ChatColor.MAGIC.toString());
        }
        boolean hasInsertion = component.getInsertion() != null;
        if (hasInsertion) {
            builder.append(ChatColor.COLOR_CHAR).append("[insertion=").append(escape(component.getInsertion())).append("]");
        }
        boolean hasHover = component.getHoverEvent() != null;
        if (hasHover) {
            HoverEvent hover = component.getHoverEvent();
            builder.append(ChatColor.COLOR_CHAR).append("[hover=").append(hover.getAction().name()).append(";").append(escape(NMSHandler.getInstance().stringForHover(hover))).append("]");
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
                builder.append(stringify(afterComponent));
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
        while (output.contains(RESET + RESET)) {
            output = output.replace(RESET + RESET, RESET);
        }
        return output;
    }

    public static final String RESET = ChatColor.RESET.toString();

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

    public static BaseComponent[] parse(String str) {
        str = CoreUtilities.clearNBSPs(str);
        char[] chars = str.toCharArray();
        List<BaseComponent> outputList = new ArrayList<>(2);
        int started = 0;
        TextComponent nextText = new TextComponent();
        TextComponent lastText = new TextComponent();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == ChatColor.COLOR_CHAR && i + 1 < chars.length) {
                char code = chars[i + 1];
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
                        outputList.add(nextText);
                        TextComponent doublelasttext = lastText;
                        lastText = nextText;
                        nextText = copyFormatToNewText(lastText);
                        nextText.setText("");
                        if (innardType.equals("score") && innardParts.size() == 2) {
                            ScoreComponent component = new ScoreComponent(unescape(innardBase.get(1)), unescape(innardParts.get(0)), unescape(innardParts.get(1)));
                            lastText.addExtra(component);
                        }
                        else if (innardType.equals("keybind")) {
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
                                for (BaseComponent subComponent : parse(unescape(extra))) {
                                    component.addWith(subComponent);
                                }
                            }
                            lastText.addExtra(component);
                        }
                        else if (innardType.equals("click") && innardParts.size() == 1) {
                            int endIndex = str.indexOf(ChatColor.COLOR_CHAR + "[/click]", i);
                            int backupEndIndex = str.indexOf(ChatColor.COLOR_CHAR + "[click=", i + 5);
                            if (backupEndIndex > 0 && backupEndIndex < endIndex) {
                                endIndex = backupEndIndex;
                            }
                            if (endIndex == -1) {
                                continue;
                            }
                            TextComponent clickableText = new TextComponent();
                            clickableText.setClickEvent(new ClickEvent(ClickEvent.Action.valueOf(innardBase.get(1).toUpperCase()), unescape(innardParts.get(0))));
                            for (BaseComponent subComponent : parse(str.substring(endBracket + 1, endIndex))) {
                                clickableText.addExtra(subComponent);
                            }
                            lastText.addExtra(clickableText);
                            endBracket = endIndex + "&[/click".length();
                        }
                        else if (innardType.equals("hover")) {
                            int endIndex = str.indexOf(ChatColor.COLOR_CHAR + "[/hover]", i);
                            int backupEndIndex = str.indexOf(ChatColor.COLOR_CHAR + "[hover=", i + 5);
                            if (backupEndIndex > 0 && backupEndIndex < endIndex) {
                                endIndex = backupEndIndex;
                            }
                            if (endIndex == -1) {
                                continue;
                            }
                            TextComponent hoverableText = new TextComponent();
                            HoverEvent.Action action = HoverEvent.Action.valueOf(innardBase.get(1).toUpperCase());
                            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_16)) {
                                if (HoverFormatHelper.processHoverInput(action, hoverableText, innardParts.get(0))) {
                                    continue;
                                }
                            }
                            else {
                                BaseComponent[] hoverValue;
                                if (action == HoverEvent.Action.SHOW_ITEM) {
                                    ItemTag item = ItemTag.valueOf(unescape(innardParts.get(0)), CoreUtilities.noDebugContext);
                                    if (item == null) {
                                        continue;
                                    }
                                    hoverValue = new BaseComponent[] {new TextComponent(NMSHandler.getItemHelper().getRawHoverText(item.getItemStack()))};
                                }
                                else if (action == HoverEvent.Action.SHOW_ENTITY) {
                                    EntityTag entity = EntityTag.valueOf(unescape(innardParts.get(0)), CoreUtilities.basicContext);
                                    if (entity == null) {
                                        continue;
                                    }
                                    hoverValue = new BaseComponent[] {new TextComponent(NMSHandler.getEntityHelper().getRawHoverText(entity.getBukkitEntity()))};
                                }
                                else {
                                    hoverValue = parse(unescape(innardParts.get(0)));
                                }
                                hoverableText.setHoverEvent(new HoverEvent(action, hoverValue));
                            }
                            for (BaseComponent subComponent : parse(str.substring(endBracket + 1, endIndex))) {
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
                            for (BaseComponent subComponent : parse(str.substring(endBracket + 1, endIndex))) {
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
                                nextText.setFont(doublelasttext.getFont());
                            }
                            else {
                                nextText.setColor(doublelasttext.getColor());
                            }
                        }
                        else if (innardType.equals("color")) {
                            String colorChar = innardBase.get(1);
                            if (colorChar.length() == 1) {
                                nextText.setColor(ChatColor.getByChar(colorChar.charAt(0)));
                            }
                            else if (colorChar.length() == 7) {
                                nextText.setColor(ChatColor.of(colorChar));
                            }
                        }
                        else if (innardType.equals("font")) {
                            nextText.setFont(innardBase.get(1));
                        }
                    }
                    i = endBracket;
                    started = endBracket + 1;
                    continue;
                }
                else if ((code >= '0' && code <= '9') || (code >= 'a' && code <= 'f') || (code >= 'A' && code <= 'F')) {
                    nextText.setText(nextText.getText() + str.substring(started, i));
                    outputList.add(nextText);
                    nextText = new TextComponent();
                    nextText.setColor(ChatColor.getByChar(code));
                }
                else if ((code >= 'k' && code <= 'o') || (code >= 'K' && code <= 'O')) {
                    nextText.setText(nextText.getText() + str.substring(started, i));
                    outputList.add(nextText);
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
                else if (code == 'r' || code == 'R') {
                    nextText.setText(nextText.getText() + str.substring(started, i));
                    outputList.add(nextText);
                    nextText = new TextComponent();
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
                        color.append(chars[i + 1 + c * 2]);
                    }
                    if (color == null) {
                        continue;
                    }
                    nextText.setText(nextText.getText() + str.substring(started, i));
                    outputList.add(nextText);
                    nextText = new TextComponent();
                    nextText.setColor(ChatColor.of(color.toString()));
                    i += 13;
                    started = i + 1;
                    continue;
                }
                else {
                    continue;
                }
                i++;
                started = i + 1;
            }
            else if (i + "https://a.".length() < chars.length && chars[i] == 'h' && chars[i + 1] == 't' && chars[i + 2] == 't' && chars[i  + 3] == 'p') {
                String subStr = str.substring(i, i + "https://a.".length());
                if (subStr.startsWith("https://") || subStr.startsWith("http://")) {
                    int nextSpace = str.indexOf(' ', i);
                    if (nextSpace == -1) {
                        nextSpace = str.length();
                    }
                    String url = str.substring(i, nextSpace);
                    nextText.setText(nextText.getText() + str.substring(started, i));
                    outputList.add(nextText);
                    lastText = nextText;
                    nextText = new TextComponent(lastText);
                    nextText.setText("");
                    TextComponent clickableText = new TextComponent(url);
                    clickableText.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
                    lastText.addExtra(clickableText);
                    i = nextSpace;
                    started = nextSpace;
                    continue;
                }
            }
        }
        nextText.setText(nextText.getText() + str.substring(started));
        if (!nextText.getText().isEmpty()) {
            outputList.add(nextText);
        }
        return outputList.toArray(new BaseComponent[0]);
    }
}

package com.denizenscript.denizen.utilities;

import com.denizenscript.denizencore.utilities.AsciiMatcher;
import org.bukkit.ChatColor;

import java.util.HashMap;

public class TextWidthHelper {

    public static int[] asciiWidthMap = new int[128];
    public static HashMap<Character, Integer> characterWidthMap = new HashMap<>();

    public static void setWidth(int width, String chars) {
        for (char c : chars.toCharArray()) {
            if (c < 128) {
                asciiWidthMap[c] = width;
            }
            else {
                characterWidthMap.put(c, width);
            }
        }
    }

    static {
        for (int i = 0; i < 128; i++) {
            asciiWidthMap[i] = 6;
        }
        // Covers all symbols in the default ascii.png texture file
        setWidth(2, "!,.:;|i'");
        setWidth(3, "l`");
        setWidth(4, " (){}[]tI\"*");
        setWidth(5, "<>fkªº▌⌡°ⁿ²");
        // all other characters are length=6
        setWidth(7, "@~«»≡≈√");
        setWidth(8, "░╢╖╣║╗╝╜∅⌠");
        setWidth(9, "▒▓└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┌█▄▐▀");
    }

    public static int getWidth(char c) {
        if (c < 128) {
            return asciiWidthMap[c];
        }
        return characterWidthMap.getOrDefault(c, 6);
    }

    public static AsciiMatcher formatCharCodeMatcher = new AsciiMatcher("klmnoKLMNO");

    public static int getWidth(String str) {
        return getWidth(false, str);
    }

    public static int getWidth(boolean wasBold, String str) {
        int maxWidth = 0;
        int total = 0;
        boolean bold = wasBold;
        char[] rawChars = str.toCharArray();
        for (int i = 0; i < rawChars.length; i++) {
            char c = rawChars[i];
            if (c == ChatColor.COLOR_CHAR && (i + 1) < rawChars.length) {
                char c2 = rawChars[i + 1];
                if (c2 == '[') {
                    while (i < rawChars.length && rawChars[i] != ']') {
                        i++;
                    }
                    continue;
                }
                else if (c2 == 'l' || c2 == 'L') {
                    bold = true;
                }
                else if (!formatCharCodeMatcher.isMatch(c2)) {
                    bold = false;
                }
                i++;
                continue;
            }
            total += getWidth(c) + (bold ? 1 : 0);
            if (c == '\n') {
                if (total > maxWidth) {
                    maxWidth = total;
                }
                total = 0;
            }
        }
        return Math.max(total, maxWidth);
    }

    public static boolean isBold(boolean wasBold, String str) {
        boolean bold = wasBold;
        char[] rawChars = str.toCharArray();
        for (int i = 0; i < rawChars.length; i++) {
            char c = rawChars[i];
            if (c == ChatColor.COLOR_CHAR && (i + 1) < rawChars.length) {
                char c2 = rawChars[i + 1];
                if (c2 == 'l' || c2 == 'L') {
                    bold = true;
                }
                else if (!formatCharCodeMatcher.isMatch(c2)) {
                    bold = false;
                }
            }
        }
        return bold;
    }

    public static String splitLines(String str, int width) {
        if (width < 8) {
            return str;
        }
        StringBuilder output = new StringBuilder(str.length() * 2);
        int lineStart = 0;
        boolean bold = false;
        mainloop:
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == ChatColor.COLOR_CHAR) {
                i++;
                continue;
            }
            if (c == '\n') {
                String lastLine = str.substring(lineStart, i + 1);
                bold = isBold(bold, lastLine);
                output.append(lastLine);
                lineStart = i + 1;
                continue;
            }
            if (getWidth(bold, str.substring(lineStart, i)) > width) {
                for (int x = i - 1; x > lineStart; x--) {
                    char xc = str.charAt(x);
                    if (xc == ' ') {
                        String lastLine = str.substring(lineStart, x);
                        bold = isBold(bold, lastLine);
                        output.append(lastLine).append("\n");
                        lineStart = x + 1;
                        i = x;
                        continue mainloop;
                    }
                }
                String lastLine = str.substring(lineStart, i);
                bold = isBold(bold, lastLine);
                output.append(lastLine).append("\n");
                lineStart = i;
            }
        }
        output.append(str, lineStart, str.length());
        return output.toString();
    }
}

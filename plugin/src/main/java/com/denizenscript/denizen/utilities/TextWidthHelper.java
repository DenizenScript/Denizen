package com.denizenscript.denizen.utilities;

public class TextWidthHelper {

    public static int[] characterWidthMap = new int[128];

    public static void setWidth(int width, String chars) {
        for (char c : chars.toCharArray()) {
            characterWidthMap[c] = width;
        }
    }

    static {
        for (int i = 0; i < 128; i++) {
            characterWidthMap[i] = 6;
        }
        setWidth(2, "!,.:;|i`");
        setWidth(3, "'l");
        setWidth(4, " []tI");
        setWidth(5, "\"()*<>fk{}");
        // all other ASCII characters are length=6
        setWidth(7, "@~");
    }

    public static int getWidth(char c) {
        return c > 127 ? 6 : characterWidthMap[c];
    }

    public static int getWidth(String str) {
        int total = 0;
        for (char c : str.toCharArray()) {
            total += getWidth(c);
            if (c == '\n') {
                total = 0;
            }
        }
        return total;
    }

    public static String splitLines(String str, int width) {
        if (width < 8) {
            return str;
        }
        StringBuilder output = new StringBuilder(str.length() * 2);
        int curLineWidth = 0;
        int lineStart = 0;
        mainloop:
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\n') {
                output.append(str, lineStart, i);
                curLineWidth = 0;
                lineStart = i + 1;
                continue;
            }
            curLineWidth += getWidth(c);
            if (curLineWidth > width) {
                for (int x = i - 1; x > lineStart; x--) {
                    char xc = str.charAt(x);
                    if (xc == ' ') {
                        output.append(str, lineStart, x).append("\n");
                        curLineWidth = 0;
                        lineStart = x + 1;
                        i = x;
                        continue mainloop;
                    }
                }
                output.append(str, lineStart, i).append("\n");
                curLineWidth = 0;
                lineStart = i;
            }
        }
        output.append(str, lineStart, str.length());
        return output.toString();
    }
}

package net.aufdemrand.denizen.tags;


import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Jeremy Schroeder
 *
 */

public class Attribute {


    private static List<String> separate_attributes(String attributes) {

        List<String> matches = new ArrayList<String>();

        int x1 = 0, x2 = -1;
        int braced = 0;

        for (int x = 0; x < attributes.length(); x++) {

            Character chr = attributes.charAt(x);

            if (chr == '[')
                braced++;

            else if (x == attributes.length() - 1) {
                x2 = x + 1;
            }

            else if (chr == ']') {
                if (braced > 0) braced--;
            }

            else if (chr == '.'
                    && !StringUtils.isNumeric(Character.toString(attributes.charAt(x + 1)))
                    && braced == 0)
                x2 = x;

            if (x2 > -1) {
                matches.add(attributes.substring(x1, x2));
                x2 = -1;
                x1 = x + 1;
            }

        }

        return matches;
    }



    public static String RETURN_NULL = "null";

    public List<String> attributes;

    ScriptEntry scriptEntry;

    String raw_tag;
    String origin;

    public ScriptEntry getScriptEntry() {
        return scriptEntry;
    }

    public String getOrigin() {
        return origin;
    }

    public Attribute(String attributes, ScriptEntry scriptEntry) {
        raw_tag = attributes;
        origin = attributes;
        this.scriptEntry = scriptEntry;

        if (attributes == null) {
            this.attributes = Collections.emptyList();
            return;
        }

        this.attributes = separate_attributes(attributes);
    }

    public boolean startsWith(String string) {
        string = string.toLowerCase();
        if (attributes.isEmpty()) return false;
        return raw_tag.toLowerCase().startsWith(string);
    }

    public boolean startsWith(String string, int attribute) {
        if (attributes.isEmpty()) return false;
        if (attributes.size() < attribute) return false;
        return getAttribute(attribute).startsWith(string);
    }

    public Attribute fulfill(int attributes) {
        for (int x = attributes; x > 0; x--) {
            this.attributes.remove(0);
        }
        rebuild_raw_tag();
        return this;
    }

    private void rebuild_raw_tag() {
        if (attributes.size() == 0) raw_tag = "";
        StringBuilder sb = new StringBuilder();
        for (String attribute : attributes)
            sb.append(attribute).append(".");
        raw_tag = sb.toString();
        if (raw_tag.length() > 1)
            raw_tag = raw_tag.substring(0, raw_tag.length() - 1);
    }

    Pattern CONTEXT_PATTERN = Pattern.compile("\\[.+\\]$");
    public boolean hasContext(int attribute) {
        String text = getAttribute(attribute);
        return text.endsWith("]") && text.contains("[");
    }

    public String getContext(int attribute) {
        if (hasContext(attribute)) {

            String text = getAttribute(attribute);
            Matcher contextMatcher = CONTEXT_PATTERN.matcher(text);

            if (contextMatcher.find()) {
                return text.substring(contextMatcher.start() + 1, contextMatcher.end() - 1);
            }
        }
        return null;
    }

    public int getIntContext(int attribute) {
        try {
            if (hasContext(attribute))
                return Integer.valueOf(getContext(attribute));
        } catch (Exception e) { }

        return 0;
    }

    public double getDoubleContext(int attribute) {
        try {
            if (hasContext(attribute))
                return Double.valueOf(getContext(attribute));
        } catch (Exception e) { }
        return 0;
    }

    public String getAttribute(int num) {
        if (attributes.size() < num) return "";
        else return attributes.get(num - 1);
    }

}

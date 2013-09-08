package net.aufdemrand.denizen.tags;


import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.utilities.debugging.dB;

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

    // TODO: Make this private. It's public right now to enable easy debugging.
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

        Pattern attributer = Pattern.compile("[^\\[\\]\\.]+(\\[.*?\\])?");
        List<String> matches = new ArrayList<String>();
        Matcher matcher = attributer.matcher(attributes);

        while (matcher.find()) {
            matches.add(matcher.group());
        }

        this.attributes = matches;
    }

    public boolean startsWith(String string) {
        string = string.toLowerCase();
        if (attributes.isEmpty()) return false;
        return raw_tag.toLowerCase().startsWith(string);
    }

    public boolean startsWith(String string, int attribute) {
        string = string.toLowerCase();
        if (attributes.isEmpty()) return false;
        if (attributes.size() < attribute) return false;
        return raw_tag.split(".", attribute)[attribute - 1].toLowerCase().startsWith(string);
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

    public boolean hasContext(int attribute) {
        return getAttribute(attribute).contains("[");
    }

    public String getContext(int attribute) {
        if (hasContext(attribute)) {

            String text = getAttribute(attribute);
            Matcher contextMatcher = Pattern.compile("\\[.+\\]").matcher(text);

            if (contextMatcher.find()) {
                return text.substring(contextMatcher.start() + 1, contextMatcher.end() - 1);
            }
        }
        return null;
    }

    public int getIntContext(int attribute) {
        try {
        if (hasContext(attribute))
            return Integer.valueOf(getAttribute(attribute).split("\\[", 2)[1].replace("]", ""));
        } catch (Exception e) { }

        return 0;
    }

    public String getAttribute(int num) {
        if (attributes.size() < num) return "";
        else return attributes.get(num - 1);
    }

}

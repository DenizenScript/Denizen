package net.aufdemrand.denizen.tags;


import net.aufdemrand.denizen.scripts.ScriptEntry;

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

    List<String> attributes;

    ScriptEntry scriptEntry;

    String raw_tag;

    public ScriptEntry getScriptEntry() {
        return scriptEntry;
    }

    public Attribute(String attributes, ScriptEntry scriptEntry) {
        raw_tag = attributes;
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
        if (attributes.get(0).toLowerCase().startsWith(string)) return true;
        return false;
    }

    public boolean startsWith(String string, int attribute) {
        string = string.toLowerCase();
        if (attributes.isEmpty()) return false;
        if (attributes.size() < attribute) return false;
        if (attributes.get(attribute - 1).toLowerCase().startsWith(string)) return true;
        return false;
    }

    public Attribute fulfill(int attributes) {
        for (int x = attributes; x > 0; x--)
            this.attributes.remove(0);
        return this;
    }

    public boolean hasContext(int attribute) {
        if (getAttribute(attribute).contains("[")) return true;
        return false;
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

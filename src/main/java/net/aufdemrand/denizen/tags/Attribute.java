package net.aufdemrand.denizen.tags;

import net.aufdemrand.denizen.scripts.ScriptEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                    && !(attributes.charAt(x + 1) >= '0' && attributes.charAt(x + 1) <= '9')
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

    public List<String> attributes;
    public List<String> contexts;
    public List<String> original_attributes;
    public List<String> original_contexts;

    ScriptEntry scriptEntry;

    public String raw_tag;
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
        contexts = new ArrayList<String>(this.attributes.size());
        for (int i = 0; i < this.attributes.size(); i++)
            contexts.add(null);
        original_attributes = new ArrayList<String>(this.attributes);
        original_contexts = new ArrayList<String>(contexts);
    }

    public boolean matches(String string) {
        if (attributes.isEmpty()) return false;
        String attr = attributes.get(0);
        if (attr.contains("[") && attr.endsWith("]"))
            attr = attr.substring(0, attr.indexOf('['));
        return attr.equalsIgnoreCase(string);
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

    int fulfilled = 0;

    public Attribute fulfill(int attributes) {
        for (int x = attributes; x > 0; x--) {
            this.attributes.remove(0);
            this.contexts.remove(0);
            fulfilled++;
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

    public static Pattern CONTEXT_PATTERN = Pattern.compile("\\[.+\\]$", Pattern.DOTALL | Pattern.MULTILINE);
    public boolean hasContext(int attribute) {
        String text = getAttribute(attribute);
        return text.endsWith("]") && text.contains("[");
    }

    public String getContext(int attribute) {
        if (attribute <= attributes.size() && attribute > 0 && hasContext(attribute)) {

            String text = getAttribute(attribute);
            if (contexts.get(attribute - 1) != null) {
                return contexts.get(attribute - 1);
            }
            Matcher contextMatcher = CONTEXT_PATTERN.matcher(text);

            if (contextMatcher.find()) {
                String tagged = TagManager.cleanOutputFully(TagManager.tag(
                        scriptEntry != null ? scriptEntry.getPlayer(): null, scriptEntry != null ? scriptEntry.getNPC(): null,
                        text.substring(contextMatcher.start() + 1,
                        contextMatcher.end() - 1), false, getScriptEntry()));
                contexts.set(attribute - 1, tagged);
                original_contexts.set(attribute - 1 + fulfilled, tagged);
                return tagged;
            }
        }
        return null;
    }

    private boolean hadAlternative = false;

    public boolean hasAlternative() {
        return hadAlternative;
    }

    public void setHadAlternative(boolean hadAlternative) {
        this.hadAlternative = hadAlternative;
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
        if (attributes.size() < num || num <= 0)
            return "";
        else {
            return attributes.get(num - 1);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < original_attributes.size(); i++) {
            if (original_contexts.get(i) != null)
                sb.append(original_attributes.get(i).substring(0, original_attributes.get(i).indexOf('[')))
                        .append("[").append(original_contexts.get(i)).append("].");
            else
                sb.append(original_attributes.get(i)).append(".");
        }
        if (sb.length() > 0)
            return sb.substring(0, sb.length() - 1);
        return "";
    }
}

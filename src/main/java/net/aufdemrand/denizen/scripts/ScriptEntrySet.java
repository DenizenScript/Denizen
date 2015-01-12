package net.aufdemrand.denizen.scripts;

import net.aufdemrand.denizen.utilities.debugging.dB;

import java.util.ArrayList;
import java.util.List;

public class ScriptEntrySet {
    private List<ScriptEntry> entries;

    public ScriptEntrySet(List<ScriptEntry> baseEntries) {
        entries = baseEntries;
    }

    public ScriptEntrySet Duplicate() {
        int count = entries.size();
        List<ScriptEntry> newEntries = new ArrayList<ScriptEntry>(count);
        try {
            for (ScriptEntry entry: entries) {
                newEntries.add(entry.clone());
            }
        }
        catch (CloneNotSupportedException e) {
            dB.echoError(e); // This should never happen
        }
        return new ScriptEntrySet(newEntries);
    }

    public List<ScriptEntry> getEntries() {
        return entries;
    }
}

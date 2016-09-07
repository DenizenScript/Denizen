package net.aufdemrand.denizen.npc.speech;

import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.citizensnpcs.api.ai.speech.SpeechContext;

public class DenizenSpeechContext extends SpeechContext {

    private ScriptEntry scriptEntry;
    private boolean bystandersEnabled;
    private double chatRange;

    public DenizenSpeechContext(String message, ScriptEntry scriptEntry, double chatRange) {
        setMessage(message);
        this.scriptEntry = scriptEntry;
        this.bystandersEnabled = chatRange >= 0;
        this.chatRange = chatRange;
    }

    public ScriptEntry getScriptEntry() {
        return scriptEntry;
    }

    public boolean isBystandersEnabled() {
        return bystandersEnabled;
    }

    public double getChatRange() {
        return chatRange;
    }
}

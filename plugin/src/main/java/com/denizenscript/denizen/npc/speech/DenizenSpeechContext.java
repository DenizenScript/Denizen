package com.denizenscript.denizen.npc.speech;

import com.denizenscript.denizencore.scripts.ScriptEntry;
import net.citizensnpcs.api.ai.speech.SpeechContext;

public class DenizenSpeechContext extends SpeechContext {

    private final ScriptEntry scriptEntry;
    private final double chatRange;

    public DenizenSpeechContext(String message, ScriptEntry scriptEntry, double chatRange) {
        super(message);
        this.scriptEntry = scriptEntry;
        this.chatRange = chatRange;
    }

    public ScriptEntry getScriptEntry() {
        return scriptEntry;
    }

    public boolean isBystandersEnabled() {
        return chatRange >= 0;
    }

    public double getChatRange() {
        return chatRange;
    }
}

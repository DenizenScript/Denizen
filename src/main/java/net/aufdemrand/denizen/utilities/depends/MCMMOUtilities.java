package net.aufdemrand.denizen.utilities.depends;

import net.aufdemrand.denizen.utilities.debugging.dB;

import com.gmail.nossr50.datatypes.PlayerProfile;
import com.gmail.nossr50.skills.utilities.SkillType;

public class MCMMOUtilities {

    public static PlayerProfile getPlayerProfileFrom(String arg) {
        PlayerProfile profile = Depends.mcmmo.getPlayerProfile(arg);
        if(profile == null)
            dB.echoDebug("Could not find mcMMO data for " + arg + "!");
        return profile;
    }
	
    public static SkillType getSkillTypeFrom(String arg) {
        SkillType skill = null;
        try {
		    skill = SkillType.getSkill(arg);
	    } catch(IllegalArgumentException e) {
            dB.echoError("McMMO skill " + arg + " not recognized!");
        }
        return skill;
    }

}

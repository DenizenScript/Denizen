package net.aufdemrand.denizen.npc.traits;

import org.bukkit.event.Listener;

import net.aufdemrand.denizen.Denizen;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

public class NicknameTrait extends Trait implements Listener {

	private String nickname = null;
	private Denizen plugin;
	
	public NicknameTrait() {
		super("nickname");
		plugin = (Denizen) plugin.getServer().getPluginManager().getPlugin("Denizen");
	}
	
	@Override public void load(DataKey key) throws NPCLoadException {
		nickname = key.getString("nickname", null);
	}
	
	@Override public void save(DataKey key) {
		key.setString("nickname", nickname);
	}
	
	public void setNickname(String nickName) {
		this.nickname = nickName;
	}
	
	public String getNickname() {
	    if (nickname == null) return npc.getName();
        return this.nickname;
    }
	
	public void removeNickname() {
	    nickname = null;
	}
	
	public boolean hasNickname() {
	    return (nickname == null) ? false : true; 
	}

}

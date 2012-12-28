package net.aufdemrand.denizen.listeners;


import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.interfaces.RegistrationableInstance;
import net.aufdemrand.denizen.scripts.helpers.ArgumentHelper;

public abstract class AbstractListenerType implements RegistrationableInstance {

	Denizen denizen;
	ArgumentHelper aH;
	String name;
	Class<? extends AbstractListener> instanceClass;
	
	@Override
	public AbstractListenerType activate() {
		this.denizen = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");
		return this;
	}

	@Override
	public AbstractListenerType as(String name) {
		this.name = name.toUpperCase();
		denizen.getListenerRegistry().register(this.name, this);
		onEnable();
		return this;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public AbstractListenerType withClass(Class<? extends AbstractListener> listenerInstanceClass) {
		this.instanceClass = listenerInstanceClass;
		return null;
	}
	
	public AbstractListener createInstance(Player player, String listenerId) {
		try {
			denizen.getListenerRegistry().addListenerFor(player, instanceClass.newInstance(), listenerId);
			return denizen.getListenerRegistry().getListenerFor(player, listenerId);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Part of the Plugin disable sequence.
	 * 
	 * Can be '@Override'n by a Listener which requires a method when bukkit sends a
	 * onDisable() to Denizen. (ie. Server shuts down or restarts)
	 * 
	 */
	public void onDisable() {
	
	}

	
}



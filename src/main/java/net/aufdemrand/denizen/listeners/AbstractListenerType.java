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
	
	public AbstractListener createInstance(Player player) {
		try {
			denizen.getListenerRegistry().addInstanceOfListener(player, instanceClass.newInstance());
			return denizen.getListenerRegistry().getListenersFor(player).get(0);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}



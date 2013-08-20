package net.aufdemrand.denizen.listeners;


import net.aufdemrand.denizen.objects.dPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.interfaces.RegistrationableInstance;
import net.aufdemrand.denizen.objects.aH;

public abstract class AbstractListenerType implements RegistrationableInstance {

    Denizen denizen;
    String name;
    Class<? extends AbstractListener> instanceClass;
    
    @Override
    public AbstractListenerType activate() {
        this.denizen = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");
        return this;
    }

    @Override
    public AbstractListenerType as(String type) {
        name = type.toUpperCase();
        denizen.getListenerRegistry()
                .register(this.name, this);
        onEnable();
        return this;
    }

    public AbstractListener createInstance(dPlayer player, String listenerId) {
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

    @Override
    public String getName() {
        return this.name;
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
    
    public AbstractListenerType withClass(Class<? extends AbstractListener> listenerInstanceClass) {
        this.instanceClass = listenerInstanceClass;
        return null;
    }

    
}



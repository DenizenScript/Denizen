package net.aufdemrand.denizen.scripts.queues.core;

import net.aufdemrand.denizen.objects.Duration;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;

public interface Delayable {

    public Delayable setPaused(boolean paused);

    public boolean isPaused();

    public void delayFor(Duration duration);

    public boolean isDelayed();
}

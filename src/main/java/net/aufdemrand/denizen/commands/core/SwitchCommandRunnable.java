package net.aufdemrand.denizen.commands.core;

public abstract class SwitchCommandRunnable<A> implements Runnable {
	 
    private A a;
   
    public SwitchCommandRunnable(A a) {
        this.a = a;
    }
   
    @Override
    public final void run() {
        run(a);
    }
   
    public abstract void run(A a);
   
}
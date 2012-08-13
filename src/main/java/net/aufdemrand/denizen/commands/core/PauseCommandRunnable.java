package net.aufdemrand.denizen.commands.core;

public abstract class PauseCommandRunnable<A> implements Runnable {
	 
    private A a;
   
    public PauseCommandRunnable(A a) {
        this.a = a;
   }
   
    @Override
    public final void run() {
        run(a);
    }
   
    public abstract void run(A a);
   
}
package net.aufdemrand.denizen.commands.core;

public abstract class SwitchCommandRunnable<A, B> implements Runnable {
	 
    private A a;
    private B b;
   
    public SwitchCommandRunnable(A a, B b) {
        this.a = a;
        this.b = b;
    }
   
    @Override
    public final void run() {
        run(a, b);
    }
   
    public abstract void run(A a, B b);
   
}
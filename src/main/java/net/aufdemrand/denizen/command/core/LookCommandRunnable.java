package net.aufdemrand.denizen.command.core;

public abstract class LookCommandRunnable<A, B> implements Runnable {
	 
    private A a;
    private B b;
   
    public LookCommandRunnable(A a, B b) {
        this.a = a;
        this.b = b;
    }
   
    @Override
    public final void run() {
        run(a, b);
    }
   
    public abstract void run(A a, B b);
   
}
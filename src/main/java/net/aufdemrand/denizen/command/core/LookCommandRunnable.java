package net.aufdemrand.denizen.command.core;

public abstract class LookCommandRunnable<A, B, C> implements Runnable {
	 
    private A a;
    private B b;
    private C c;
   
    public LookCommandRunnable(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }
   
    @Override
    public final void run() {
        run(a, b, c);
    }
   
    public abstract void run(A a, B b, C c);
   
}
package net.aufdemrand.denizen.runnables;

public abstract class ThreeItemRunnable<A, B, C> implements Runnable {
	 
    private A a;
    private B b;
    private C c;
   
    public ThreeItemRunnable(A a, B b, C c) {
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
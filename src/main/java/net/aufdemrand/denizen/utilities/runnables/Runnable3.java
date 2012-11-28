package net.aufdemrand.denizen.utilities.runnables;

public abstract class Runnable3<A, B, C> implements Runnable {
	 
    private A a;
    private B b;
    private C c;
   
    public Runnable3(A a, B b, C c) {
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
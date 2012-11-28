package net.aufdemrand.denizen.utilities.runnables;

public abstract class Runnable2<A, B> implements Runnable {
	 
    private A a;
    private B b;
   
    public Runnable2(A a, B b) {
        this.a = a;
    }
   
    @Override
    public final void run() {
        run(a, b);
    }
   
    public abstract void run(A a, B b);
   
}
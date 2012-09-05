package net.aufdemrand.denizen.runnables;

public abstract class TwoItemRunnable<A, B> implements Runnable {
	 
    private A a;
    private B b;
   
    public TwoItemRunnable(A a, B b) {
        this.a = a;
    }
   
    @Override
    public final void run() {
        run(a, b);
    }
   
    public abstract void run(A a, B b);
   
}
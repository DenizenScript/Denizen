package net.aufdemrand.denizen.utilities.runnables;

public abstract class Runnable1<A> implements Runnable {
	 
    private A a;
   
    public Runnable1(A a) {
        this.a = a;
   }
   
    @Override
    public final void run() {
        run(a);
    }
   
    public abstract void run(A a);
   
}
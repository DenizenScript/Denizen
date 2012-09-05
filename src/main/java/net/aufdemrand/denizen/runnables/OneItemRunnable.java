package net.aufdemrand.denizen.runnables;

public abstract class OneItemRunnable<A> implements Runnable {
	 
    private A a;
   
    public OneItemRunnable(A a) {
        this.a = a;
   }
   
    @Override
    public final void run() {
        run(a);
    }
   
    public abstract void run(A a);
   
}
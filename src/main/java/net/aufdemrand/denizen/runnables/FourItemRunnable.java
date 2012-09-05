package net.aufdemrand.denizen.runnables;

public abstract class FourItemRunnable<A, B, C, D> implements Runnable {
	 
    private A a;
    private B b;
    private C c;
    private D d;
   
    public FourItemRunnable(A a, B b, C c, D d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }
   
    @Override
    public final void run() {
        run(a, b, c, d);
    }
   
    public abstract void run(A a, B b, C c, D d);
   
}
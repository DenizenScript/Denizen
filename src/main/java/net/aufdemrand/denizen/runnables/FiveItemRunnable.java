package net.aufdemrand.denizen.runnables;

public abstract class FiveItemRunnable<A, B, C, D, E> implements Runnable {
	 
    private A a;
    private B b;
    private C c;
    private D d;
    private E e;
   
    public FiveItemRunnable(A a, B b, C c, D d, E e) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
    }
   
    @Override
    public final void run() {
        run(a, b, c, d, e);
    }
   
    public abstract void run(A a, B b, C c, D d, E e);
   
}
package net.aufdemrand.denizen.utilities.runnables;

import org.bukkit.Bukkit;

public abstract class Runnable4<A, B, C, D> implements Runnable {
	 
	private int id;
	private int timesRun;
	
	public int getRuns()
	{ return timesRun; }
	
	public void setRuns(int runs)
	{ this.timesRun = runs; }
	
	public void addRuns()
	{ this.timesRun++; }
	
	public void clearRuns()
	{ this.timesRun = 0; }
	
	public int getId()
	{ return id; }
	
	public void setId(int id)
	{ this.id = id; }
	
	protected void cancel()
	{ Bukkit.getScheduler().cancelTask(id); }
	
    private A a;
    private B b;
    private C c;
    private D d;
   
    public Runnable4(A a, B b, C c, D d) {
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
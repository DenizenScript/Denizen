package net.aufdemrand.denizen.utilities.runnables;

import org.bukkit.Bukkit;

public abstract class Runnable3<A, B, C> implements Runnable {
	 
	private int id;
	private int timesRun;
	
	public int getRuns()
	{ return timesRun; }
	
	public void addRuns()
	{ this.timesRun++;  }
	
	public void clearRuns()
	{ this.timesRun = 0;  }
	
	public int getId()
	{ return id; }
	
	public void setId(int id)
	{ this.id = id; }
	
	protected void cancel()
	{ Bukkit.getScheduler().cancelTask(id); }
	
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
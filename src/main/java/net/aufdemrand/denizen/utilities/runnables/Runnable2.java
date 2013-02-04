package net.aufdemrand.denizen.utilities.runnables;

import org.bukkit.Bukkit;

public abstract class Runnable2<A, B> implements Runnable {

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
   
    public Runnable2(A a, B b) {
        this.a = a;
        this.b = b;
    }
   
    @Override
    public final void run() {
        run(a, b);
    }
   
    public abstract void run(A a, B b);
   
}
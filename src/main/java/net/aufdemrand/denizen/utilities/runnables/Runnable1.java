package net.aufdemrand.denizen.utilities.runnables;

import org.bukkit.Bukkit;

public abstract class Runnable1<A> implements Runnable {

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
   
    public Runnable1(A a) {
        this.a = a;
   }
   
    @Override
    public final void run() {
        run(a);
    }
   
    public abstract void run(A a);
   
}
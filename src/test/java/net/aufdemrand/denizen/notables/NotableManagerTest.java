package net.aufdemrand.denizen.notables;

import static org.junit.Assert.assertEquals;

import org.bukkit.Location;
import org.junit.Test;

public class NotableManagerTest {
  @Test
  public void testNotable () {
  	NotableManager mgr = new NotableManager ();
  	
  	Location loc = new Location (null, 69, 72, 6972);
  	mgr.addNotable("Test Notable", loc);
  	
  	assertEquals (loc, mgr.getNotable("Test Notable"));
  }
}

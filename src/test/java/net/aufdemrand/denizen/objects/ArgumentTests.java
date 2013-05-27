package net.aufdemrand.denizen.objects;

import org.junit.Test;

public class ArgumentTests {

  @Test
  public void testScriptArg() throws Exception {

      dScript test1 = dScript.valueOf("script:script_name");
      dScript test2 = dScript.valueOf("script_name_17");
      dScript test3 = dScript.valueOf("script, the script");

      if (test1 == null || test2 == null || test3 == null)
          throw new Exception("Test failed!");
  }


}

package net.aufdemrand.denizen.arguments;

import org.junit.Test;

public class ArgumentTests {

  @Test
  public void testScriptArg() throws Exception {

      Script test1 = Script.valueOf("script:script_name");
      Script test2 = Script.valueOf("script_name_17");
      Script test3 = Script.valueOf("script, the script");

      if (test1 == null || test2 == null || test3 == null)
          throw new Exception("Test failed!");
  }


}

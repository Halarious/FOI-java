package org.foi.nwtis.robhalar.zadaca_1;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

public class RadnaDretvaTest
{
    
    public RadnaDretvaTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }

    @Ignore 
    @Test
    public void testInterrupt()
    {
        System.out.println("interrupt");
        RadnaDretva instance = null;
        instance.interrupt();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Ignore 
    @Test
    public void testRun()
    {
        System.out.println("run");
        RadnaDretva instance = null;
        instance.run();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Ignore 
    @Test
    public void testStart()
    {
        System.out.println("start");
        RadnaDretva instance = null;
        instance.start();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
    @Test
    public void testProvjeriKomandu()
    {
        System.out.println("provjeriKomandu");
        RadnaDretva instance = new RadnaDretva(null, null, null, "", null);
        
        String userAdd  = "USER korisnik; ADD http://www.regular-expressions.info/repeat.html;";
        String userTest = "USER korisnik; TEST http://192.168.1.1/0_-;";
        
        String userWait  = "USER korisnik; WAIT 1000;";
        String userWait2 = "USER korisnik; WAIT 19999;";
        String userWait3 = "USER korisnik; WAIT 599999;";

        String userWaitFail  = "USER korisnik; WAIT 099999;";
        String userWaitFail2 = "USER korisnik; WAIT 100;";
        String userWaitFail3 = "USER korisnik; WAIT 600001;";

        
        System.out.print("testProvjeriKomandu - userAdd: ");
        assertTrue(instance.provjeriKomandu(userAdd));
        System.out.println("PASSED");
        System.out.println("User: "     + instance.getUserTest());
        System.out.println("Command: "  + instance.getCommandTest());
        System.out.println("Argument: " + instance.getArgumentTest() + "\n");
        
        System.out.print("testProvjeriKomandu - userTest: ");
        assertTrue(instance.provjeriKomandu(userTest));
        System.out.println("PASSED");
        System.out.println("User: "     + instance.getUserTest());
        System.out.println("Command: "  + instance.getCommandTest());
        System.out.println("Argument: " + instance.getArgumentTest() + "\n");
        
        System.out.print("testProvjeriKomandu - userWait: ");
        assertTrue(instance.provjeriKomandu(userWait));
        System.out.println("PASSED");
        System.out.println("User: "     + instance.getUserTest());
        System.out.println("Command: "  + instance.getCommandTest());
        System.out.println("Argument: " + instance.getArgumentTest() + "\n");
        
        System.out.print("testProvjeriKomandu - userWait2: ");
        assertTrue(instance.provjeriKomandu(userWait2));
        System.out.println("PASSED");
        System.out.println("User: "     + instance.getUserTest());
        System.out.println("Command: "  + instance.getCommandTest());
        System.out.println("Argument: " + instance.getArgumentTest() + "\n");

        System.out.print("testProvjeriKomandu - userWait3: ");
        assertTrue(instance.provjeriKomandu(userWait3));
        System.out.println("PASSED");
        System.out.println("User: "     + instance.getUserTest());
        System.out.println("Command: "  + instance.getCommandTest());
        System.out.println("Argument: " + instance.getArgumentTest() + "\n");

        System.out.print("testProvjeriKomandu - userWaitFail: ");
        assertFalse(instance.provjeriKomandu(userWaitFail));
        System.out.println("PASSED");
        
        System.out.print("testProvjeriKomandu - userWaitFail2: ");
        assertFalse(instance.provjeriKomandu(userWaitFail2));
        System.out.println("PASSED");

        System.out.print("testProvjeriKomandu - userWaitFail3: ");
        assertFalse(instance.provjeriKomandu(userWaitFail3));
        System.out.println("PASSED");
    }
}

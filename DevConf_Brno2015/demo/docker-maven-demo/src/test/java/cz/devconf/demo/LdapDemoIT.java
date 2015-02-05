package cz.devconf.demo;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import javax.naming.directory.SearchResult;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class LdapDemoIT {

    private static LdapDemo ldapDemo;
    
    @BeforeClass
    public static void setup() throws Exception {
        ldapDemo = new LdapDemo("172.17.0.40");
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
        ldapDemo.close();
    }
    
	@Test
	public void testLdapQuery() throws Exception {
	    final String searchBase = "ou=People,dc=infinispan,dc=org";
        final String filter = "(uid=*)";
        ArrayList<SearchResult> res = ldapDemo.doQuery(searchBase, filter, null);
        assertEquals(4, res.size());
        assertEquals("uid=admin", res.get(0).getName());
        assertEquals("uid=writer", res.get(1).getName());
        assertEquals("uid=reader", res.get(2).getName());
        assertEquals("uid=unprivileged", res.get(3).getName());
	}
}

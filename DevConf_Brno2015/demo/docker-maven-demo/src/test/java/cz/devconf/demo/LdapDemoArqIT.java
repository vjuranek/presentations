package cz.devconf.demo;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import javax.naming.directory.SearchResult;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class LdapDemoArqIT {

    private static final String LDAP_IP = "127.0.0.1";

    private static LdapDemo ldapDemo;

    @BeforeClass
    public static void setup() throws Exception {
        // I wasn't able to make it running with ARQ injected value (@ArquillianResource URL base), so it's hardcoded
        // for now:-(
        ldapDemo = new LdapDemo(LDAP_IP);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        ldapDemo.close();
    }

    @Test
    @RunAsClient
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

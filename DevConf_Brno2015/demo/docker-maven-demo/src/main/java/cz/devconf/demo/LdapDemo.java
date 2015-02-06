package cz.devconf.demo;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

public class LdapDemo {

    public static final String LDAP_URL_TEMPLATE = "ldap://%s:389";
    public static final String LDAP_USERNAME = "";
    public static final String LDAP_PASSWORD = "";

    private LdapContext ctx = null;
    private SearchControls searchControls = new SearchControls();

    public LdapDemo(String ldapIP) throws NamingException {
        Hashtable<String, Object> env = new Hashtable<String, Object>();
        env.put(Context.PROVIDER_URL, String.format(LDAP_URL_TEMPLATE, ldapIP));
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, LDAP_USERNAME);
        env.put(Context.SECURITY_CREDENTIALS, LDAP_PASSWORD);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");

        ctx = new InitialLdapContext(env, null);
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    }

    public ArrayList<SearchResult> doQuery(final String searchBase, final String filter, final Map<String, String> properties) throws NamingException {
        NamingEnumeration<SearchResult> results = ctx.search(searchBase, filter, searchControls);
        ArrayList<SearchResult> res = new ArrayList<SearchResult>();
        while (results.hasMoreElements()) {
            res.add(results.nextElement());
        }
        return res;
    }

    public void close() throws NamingException {
        ctx.close();
    }

    public static void main(String[] args) throws NamingException {
        final String ldapIP = args.length > 0 ? args[0] : "172.17.0.40";
        final String searchBase = "ou=People,dc=infinispan,dc=org";
        final String filter = "(uid=*)";

        LdapDemo demo = new LdapDemo(ldapIP);
        System.out.println("\n\n\n********************************* LDAP DEMO START *********************************");
        System.out.println(String.format("Quering LDAP on IP %s with base '%s' and filter '%s'", ldapIP, searchBase, filter));

        ArrayList<SearchResult> res = demo.doQuery(searchBase, filter, null);
        for (SearchResult r : res) {
            System.out.println(String.format("Ldap search result: %s", r.getName()));
        }

        System.out.println("********************************* LDAP DEMO END *********************************\n\n\n");
        demo.close();
    }
}

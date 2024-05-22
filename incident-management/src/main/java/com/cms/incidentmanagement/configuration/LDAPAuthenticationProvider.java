package com.cms.incidentmanagement.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

@Component
public class LDAPAuthenticationProvider {

	private static final Logger logger = LoggerFactory.getLogger(LDAPAuthenticationProvider.class);

	@Autowired
	private Environment environment;

	public HashMap<String, Object> authenticateUser(String userName, String password) {
		HashMap<String, Object> map = new HashMap<String, Object>() {{
			put("isAuthenticated", false);
		}};
		String ldapUrl = environment.getProperty("ldap.url");
		String dn = environment.getProperty("ldap.dn");
		String authentication = environment.getProperty("ldap.authentication");
		String filter = environment.getProperty("ldap.filter").replace("ldapUser", userName);
		String principal = environment.getProperty("ldap.principal").replace("ldapUser", userName);

		Hashtable<String, String> environment = new Hashtable<>();
		environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		environment.put(Context.PROVIDER_URL, ldapUrl + dn);
		environment.put(Context.SECURITY_AUTHENTICATION, authentication);
		environment.put(Context.SECURITY_PRINCIPAL, principal);
		environment.put(Context.SECURITY_CREDENTIALS, password);
		try {

			DirContext authContext = new InitialDirContext(environment);
			SearchControls searchControls = new SearchControls();
			searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			if (authContext != null) {
				map.put("isAuthenticated", true);
				map.put("displayName", userName);
//                map.put("CN", null);
				NamingEnumeration objs = authContext.search(ldapUrl + dn, filter, searchControls);
				Flag1:
				while (objs.hasMoreElements()) {
					SearchResult match = (SearchResult) objs.nextElement();
					Attributes attrs = match.getAttributes();
					NamingEnumeration e = attrs.getAll();
					int flag = 0;
					while (e.hasMoreElements()) {
						Attribute attr = (Attribute) e.nextElement();
						if (attr.getID().equalsIgnoreCase("displayName")) {
							map.put("displayName", attr.get(0).toString());
							++flag;
						}
						if (attr.getID().equalsIgnoreCase("memberOf")) {
							String[] keyValuePairs = attr.get(0).toString().split(",");
							Map<String, String> memberOf = new HashMap<>();
							for (String pair : keyValuePairs) {
								String[] entry = pair.split("=");
								memberOf.put(entry[0].trim(), entry[1].trim());
							}
							map.put("CN", memberOf.get("CN"));
							++flag;
						}
						if (flag > 1) {
							break Flag1;
						}
					}
				}
			}
		} catch (Throwable e) {
			logger.error("isUserAuthenticated : user :" + userName + "  " + e.getMessage());
		}
		return map;
	}
}

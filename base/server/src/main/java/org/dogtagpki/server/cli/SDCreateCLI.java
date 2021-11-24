//
// Copyright Red Hat, Inc.
//
// SPDX-License-Identifier: GPL-2.0-or-later
//
package org.dogtagpki.server.cli;

import java.io.File;
import java.util.Enumeration;

import org.apache.commons.cli.CommandLine;
import org.dogtagpki.cli.CLI;
import org.dogtagpki.cli.CommandCLI;
import org.dogtagpki.server.ca.CAEngineConfig;

import com.netscape.cmscore.apps.CMS;
import com.netscape.cmscore.ldapconn.LDAPConfig;
import com.netscape.cmscore.ldapconn.LDAPConnectionConfig;
import com.netscape.cmscore.ldapconn.LdapAuthInfo;
import com.netscape.cmscore.ldapconn.LdapBoundConnection;
import com.netscape.cmscore.ldapconn.LdapConnInfo;
import com.netscape.cmscore.ldapconn.PKISocketConfig;
import com.netscape.cmscore.ldapconn.PKISocketFactory;
import com.netscape.cmsutil.ldap.LDAPUtil;
import com.netscape.cmsutil.password.IPasswordStore;
import com.netscape.cmsutil.password.PasswordStoreConfig;

import netscape.ldap.LDAPAttribute;
import netscape.ldap.LDAPAttributeSet;
import netscape.ldap.LDAPEntry;

/**
 * @author Endi S. Dewata
 */
public class SDCreateCLI extends CommandCLI {

    public SDCreateCLI(CLI parent) {
        super("create", "Create security domain", parent);
    }

    @Override
    public void execute(CommandLine cmd) throws Exception {

        String catalinaBase = System.getProperty("catalina.base");

        initializeTomcatJSS();

        String subsystem = parent.getParent().getName();
        String subsystemDir = catalinaBase + File.separator + subsystem;
        String subsystemConfDir = subsystemDir + File.separator + "conf";
        String configFile = subsystemConfDir + File.separator + CMS.CONFIG_FILE;

        logger.info("Loading " + configFile);
        CAEngineConfig cs = (CAEngineConfig) getEngineConfig(subsystem);
        LDAPConfig ldapConfig = cs.getInternalDBConfig();

        PasswordStoreConfig psc = cs.getPasswordStoreConfig();
        IPasswordStore passwordStore = IPasswordStore.create(psc);

        LDAPConnectionConfig connConfig = ldapConfig.getConnectionConfig();

        LdapConnInfo connInfo = new LdapConnInfo(connConfig);
        LdapAuthInfo authInfo = getAuthInfo(passwordStore, connInfo, ldapConfig);

        PKISocketConfig socketConfig = cs.getSocketConfig();

        PKISocketFactory socketFactory;
        if (authInfo.getAuthType() == LdapAuthInfo.LDAP_AUTHTYPE_SSLCLIENTAUTH) {
            socketFactory = new PKISocketFactory(authInfo.getClientCertNickname());
        } else {
            socketFactory = new PKISocketFactory(connInfo.getSecure());
        }
        socketFactory.init(socketConfig);

        LdapBoundConnection conn = new LdapBoundConnection(socketFactory, connInfo, authInfo);

        try {
            String sdName = cs.getString("securitydomain.name");

            String sdDN = "ou=Security Domain," + ldapConfig.getBaseDN();
            logger.info("Adding " + sdDN);

            LDAPAttributeSet attrs = new LDAPAttributeSet();
            attrs.add(new LDAPAttribute("objectclass", new String[] { "top", "pkiSecurityDomain" }));
            attrs.add(new LDAPAttribute("name", sdName));
            attrs.add(new LDAPAttribute("ou", "Security Domain"));

            for (Enumeration<LDAPAttribute> e = attrs.getAttributes(); e.hasMoreElements(); ) {
                LDAPAttribute attr = e.nextElement();
                for (String value : attr.getStringValueArray()) {
                    logger.debug("- " + attr.getName() + ": " + value);
                }
            }

            LDAPEntry entry = new LDAPEntry(sdDN, attrs);
            conn.add(entry);

            String clist[] = { "CAList", "OCSPList", "KRAList", "RAList", "TKSList", "TPSList" };
            for (int i = 0; i < clist.length; i++) {

                String dn = "cn=" + LDAPUtil.escapeRDNValue(clist[i]) + "," + sdDN;
                logger.info("Adding " + dn);

                attrs = new LDAPAttributeSet();
                attrs.add(new LDAPAttribute("objectclass", new String[] { "top", "pkiSecurityGroup" }));
                attrs.add(new LDAPAttribute("cn", clist[i]));

                for (Enumeration<LDAPAttribute> e = attrs.getAttributes(); e.hasMoreElements(); ) {
                    LDAPAttribute attr = e.nextElement();
                    for (String value : attr.getStringValueArray()) {
                        logger.debug("- " + attr.getName() + ": " + value);
                    }
                }

                entry = new LDAPEntry(dn, attrs);
                conn.add(entry);
            }

        } finally {
            conn.disconnect();
        }
    }
}

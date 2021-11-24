// --- BEGIN COPYRIGHT BLOCK ---
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; version 2 of the License.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//
// (C) 2019 Red Hat, Inc.
// All rights reserved.
// --- END COPYRIGHT BLOCK ---

package org.dogtagpki.cli;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.tomcat.util.net.jss.TomcatJSS;
import org.dogtagpki.util.logging.PKILogger;
import org.dogtagpki.util.logging.PKILogger.Level;

import com.netscape.certsrv.base.EBaseException;
import com.netscape.cmscore.apps.CMS;
import com.netscape.cmscore.apps.EngineConfig;
import com.netscape.cmscore.base.ConfigStorage;
import com.netscape.cmscore.base.FileConfigStore;
import com.netscape.cmscore.ldapconn.LDAPAuthenticationConfig;
import com.netscape.cmscore.ldapconn.LDAPConfig;
import com.netscape.cmscore.ldapconn.LdapAuthInfo;
import com.netscape.cmscore.ldapconn.LdapConnInfo;
import com.netscape.cmsutil.password.IPasswordStore;

/**
 * @author Endi S. Dewata
 */
public class CommandCLI extends CLI {

    public static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CommandCLI.class);

    public CommandCLI(String name, String description, CLI parent) {
        super(name, description, parent);

        createOptions();
    }

    public void createOptions() {
    }

    @Override
    public void execute(String[] args) throws Exception {

        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("help")) {
            printHelp();
            return;
        }

        if (cmd.hasOption("debug")) {
            PKILogger.setLevel(PKILogger.Level.DEBUG);

        } else if (cmd.hasOption("verbose")) {
            PKILogger.setLevel(Level.INFO);
        }

        execute(cmd);
    }

    public void execute(CommandLine cmd) throws Exception {
    }

    protected void initializeTomcatJSS() throws Exception {
        TomcatJSS tomcatjss = TomcatJSS.getInstance();
        tomcatjss.loadConfig();
        tomcatjss.init();
    }

    protected EngineConfig getEngineConfig(String subsystem) throws Exception {
        String catalinaBase = System.getProperty("catalina.base");
        String configDir = catalinaBase + File.separator + subsystem;
        String configFile = configDir + File.separator + "conf" + File.separator + CMS.CONFIG_FILE;
        logger.info("Loading {}", configFile);
        ConfigStorage storage = new FileConfigStore(configFile);
        EngineConfig engineConfig = new EngineConfig(storage);
        engineConfig.load();
        return engineConfig;
    }

    protected LdapAuthInfo getAuthInfo(IPasswordStore passwordStore, LdapConnInfo connInfo, LDAPConfig ldapConfig)
            throws EBaseException {
        LDAPAuthenticationConfig authConfig = ldapConfig.getAuthenticationConfig();
        LdapAuthInfo authInfo = new LdapAuthInfo();
        authInfo.setPasswordStore(passwordStore);
        authInfo.init(
                authConfig,
                connInfo.getHost(),
                connInfo.getPort(),
                connInfo.getSecure());
        return authInfo;
    }

}

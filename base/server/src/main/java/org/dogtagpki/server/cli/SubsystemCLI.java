//
// Copyright Red Hat, Inc.
//
// SPDX-License-Identifier: GPL-2.0-or-later
//
package org.dogtagpki.server.cli;

import org.dogtagpki.cli.CLI;
import org.dogtagpki.cli.CommandCLI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chris S. Kelley
 */
public abstract class SubsystemCLI extends CommandCLI {

    public static final Logger logger = LoggerFactory.getLogger(SubsystemCLI.class);

    protected SubsystemCLI(String name, String description, CLI parent) {
        super(name, description, parent);
    }

}

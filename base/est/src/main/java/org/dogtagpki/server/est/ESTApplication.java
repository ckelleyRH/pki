//
// Copyright Red Hat, Inc.
//
// SPDX-License-Identifier: GPL-2.0-or-later
//
package org.dogtagpki.server.est;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.dogtagpki.est.ESTFrontend;

public class ESTApplication extends Application {

    public static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ESTApplication.class);

    private Set<Class<?>> classes = new LinkedHashSet<>();
    private Set<Object> singletons = new LinkedHashSet<>();

    public ESTApplication() {

        logger.info("Initializing ESTApplication");

        // EST service
        classes.add(ESTFrontend.class);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}

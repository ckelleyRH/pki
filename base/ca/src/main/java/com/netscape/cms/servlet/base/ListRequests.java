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
// (C) 2007 Red Hat, Inc.
// All rights reserved.
// --- END COPYRIGHT BLOCK ---
package com.netscape.cms.servlet.base;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

@WebServlet(
        name = "caListRequests",
        urlPatterns = "/agent/ca/listRequests.html",
        initParams = {
                @WebInitParam(name="GetClientCert",        value="true"),
                @WebInitParam(name="htmlPath",             value="/agent/ca/ListRequests.html"),
                @WebInitParam(name="authority",            value="ca"),
                @WebInitParam(name="interface",            value="agent"),
                @WebInitParam(name="ID",                   value="caListRequests"),
                @WebInitParam(name="unauthorizedTemplate", value="/agent/ca/GenUnauthorized.template"),
                @WebInitParam(name="AuthMgr",              value="certUserDBAuthMgr")
        }
)
public class ListRequests extends DisplayHtmlServlet {
    private static final long serialVersionUID = 1L;
}

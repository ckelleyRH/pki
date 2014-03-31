/* --- BEGIN COPYRIGHT BLOCK ---
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Copyright (C) 2013 Red Hat, Inc.
 * All rights reserved.
 * --- END COPYRIGHT BLOCK ---
 *
 * @author Endi S. Dewata
 */

var ConnectionModel = Model.extend({
    urlRoot: "/tps/rest/connections",
    parseResponse: function(response) {
        return {
            id: response.id,
            connectionID: response.id,
            status: response.Status,
            properties: response.Properties.Property
        };
    },
    createRequest: function(attributes) {
        return {
            id: attributes.connectionID,
            Status: attributes.status,
            Properties: {
                Property: attributes.properties
            }
        };
    },
    enable: function(options) {
        var self = this;
        $.ajax({
            type: "POST",
            url: self.url() + "?action=enable",
            dataType: "json"
        }).done(function(data, textStatus, jqXHR) {
            self.set(self.parseResponse(data));
            if (options.success) options.success.call(self, data, textStatus, jqXHR);
        }).fail(function(jqXHR, textStatus, errorThrown) {
            if (options.error) options.error.call(self, jqXHR, textStatus, errorThrown);
        });
    },
    disable: function(options) {
        var self = this;
        $.ajax({
            type: "POST",
            url: self.url() + "?action=disable",
            dataType: "json"
        }).done(function(data, textStatus, jqXHR) {
            self.set(self.parseResponse(data));
            if (options.success) options.success.call(self, data, textStatus, jqXHR);
        }).fail(function(jqXHR, textStatus, errorThrown) {
            if (options.error) options.error.call(self, jqXHR, textStatus, errorThrown);
        });
    }
});

var ConnectionCollection = Collection.extend({
    urlRoot: "/tps/rest/connections",
    getEntries: function(response) {
        return response.entries;
    },
    getLinks: function(response) {
        return response.Link;
    },
    parseEntry: function(entry) {
        return new ConnectionModel({
            id: entry.id,
            status: entry.Status
        });
    }
});

var ConnectionsTable = Table.extend({
    initialize: function(options) {
        var self = this;
        ConnectionsTable.__super__.initialize.call(self, options);
        self.parentPage = options.parentPage;
    },
    open: function(item) {
        var self = this;

        var page = new EntryPage({
            el: self.parentPage.$el,
            url: "connection.html",
            model: item.model
        });

        page.open();
    },
    add: function() {
        var self = this;

        var page = new AddEntryPage({
            el: self.parentPage.$el,
            url: "connection.html",
            model: new ConnectionModel(),
            mode: "add",
            parentPage: self.parentPage
        });

        page.open();
    }
});

var ConnectionsPage = Page.extend({
    load: function() {
        var self = this;

        var table = new ConnectionsTable({
            el: $("table[name='connections']"),
            collection: new ConnectionCollection(),
            parentPage: self
        });

        table.render();
    }
});

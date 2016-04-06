/**
 * Created with IntelliJ IDEA.
 * User: berkich
 * Date: 6/21/13
 *
 * $.securityLabelRest is the global jQuery identifier to access this widget
 */
/*jslint plusplus: true */

jQuery(function ($, undefined) {

    var _Settings = {};

    $.widget("swif.securityLabelRest", {
        options: {
            ajaxTimeout: 120,
            baseURL: "",
            restPath: "/swif/svcs/",
            restURLs: {
                settings: "settings",
                collection: "",
                query: "/query",
                //find: "/find",
                data: "/data",
                securityLabel: "securitylabel",
                retrieveUsers: "retrieveUsers",
                retrieveGroups: "retrieveGroups"
            },
            ajax: {
                type: 'GET', contentType: 'application/json', dataType: 'json', context: this, timeout: 120 * 1000
            }
        },
        _create: function () {
            console.log("SWIF_REST_CREATE()");
            this._super();
            _Settings = this.options;
            $.securityLabelRest = this.element.data("swifSecurityLabelRest");
        },
        _destroy: function () {
            console.log("SWIF_REST_DESTROY()");
            return this._super();
        },
        _ajax: function (url, options) {
            var settings = [];

            /* Workaround to resolve broken JSON returned from queries on the 'binary' collection. wms 2/28/15 */
            if (url.indexOf("binary") >= 0) {
                settings['dataFilter'] = function (data) {
                    //return (typeof data !== 'string') ? data : data.replace('<Binary Data>','"Binary Data"');
                    return (typeof data !== 'string') ? data : data.split('<Binary Data>').join('"Binary Data"');
                }
            }

            $.extend(settings, _Settings.ajax, options, {
                url: _Settings.baseURL + url,
                timeout: this.options.ajaxTimeout * 1000
            });
            return $.ajax(settings).fail(function (jqXHR, textStatus, context) {
                if (textStatus === 'parsererror') {
                    console.log('Error Sending/Receiving data: You may need to sign out and login in!');
                } else {
                    console.log('Error Sending/Receiving data: ' + textStatus, 'Server Error');
                }
            });
        },
        _ajaxGetRest: function (url, options) {
            options = options || {};
            $.extend(options, {type: 'GET'});
            return this._ajax(_Settings.restPath + url, options);
        },
        _ajaxPostRest: function (url, options) {
            options = options || {};
            $.extend(options, {type: 'POST'});
            return this._ajax(_Settings.restPath + url, options);
        },
        _ajaxPutRest: function (url, options) {
            options = options || {};
            $.extend(options, {type: 'PUT'});
            return this._ajax(_Settings.restPath + url, options);
        },
        _ajaxDeleteRest: function (url, options) {
            options = options || {};
            $.extend(options, {type: 'DELETE'});
            return this._ajax(_Settings.restPath + url, options);
        },

        getRecords: function (collectionName, ids) {
            var id;
            var deferreds = [];
            var url = _Settings.restURLs.collection + collectionName;

            //Retrieve Single Records from Collection
            if (typeof ids === 'string') {
                url += '/' + ids;
                return this._ajaxGetRest(url, {context: this});
            }

            //Retrieve Multiple Records from Collection
            for (id in ids) {
                if (ids.hasOwnProperty(id)) {
                    var uri = url + '/' + ids[id];
                    deferreds.push(this._ajaxGetRest(uri, {context: this}))
                }
            }
            return $.when.apply($, deferreds)
                .pipe(function (a1) {
                    var json = [];

                    if (!ids || ids.length == 0) {
                        return json;
                    }

                    if (!$.securityLabelRest.isArray(a1)) {
                        json.push(a1);
                    } else {
                        for (var i = 0; i < arguments.length; i++) {
                            var args = arguments[i];
                            var data = args[0];
                            var jqXHR = args[2];
                            if (jqXHR.status === 200 && data) {
                                json.push(data);
                            }
                            else {
                                //console.log('jqXHR.status=' + jqXHR.status + ' : ' + data);
                            }
                        }
                    }
                    return json;
                });
        },

        queryCollection: function (collectionName, queryString) {
            var url = _Settings.restURLs.collection + collectionName + _Settings.restURLs.query;
            if (queryString) {
                url += '?q=' + queryString.split(' ').join('+');
            }
            return this._ajaxGetRest(url, {context: this});
        },


        createRecord: function (collectionName, collectionJSON) {
            var url = _Settings.restURLs.collection + collectionName;

            var stringJSON = collectionJSON;
            if (typeof stringJSON === 'object') {
                stringJSON = JSON.stringify(collectionJSON);
            }

            return this._ajaxPostRest(url, {data: stringJSON, context: this})
                .done(function (data) {
                    if (!data._id) {
                        console.log('Failed to create record: ' + stringJSON);
                    }
                });
        },

        updateRecord: function (collectionName, id, collectionJSON) {
            var url = _Settings.restURLs.collection + collectionName;
            if (id != null && typeof id !== 'string') {
                collectionJSON = id;
                id = null;
            }
            if (!id) {
                if (!collectionJSON._id || !collectionJSON._id.$oid) {
                    var dfd = $.Deferred();
                    dfd.reject("ID missing on update");
                    return dfd;
                }
                id = collectionJSON._id.$oid;
            }
            if (id) {
                url += '/' + id;
            }
            var stringJSON = collectionJSON;
            if (typeof stringJSON === 'object') {
                stringJSON = JSON.stringify(collectionJSON);
            }

            return this._ajaxPutRest(url, {data: stringJSON, context: this});
        },

        updateData: function (updatedProperties) {

            var url = "data/update";

            var stringJSON = updatedProperties;
            if (typeof stringJSON === 'object') {
                stringJSON = JSON.stringify(updatedProperties);
            }
            return this._ajaxPutRest(url, {data: stringJSON, context: this});
        },

        deleteRecord: function (collectionName, id, childCollections) {
            var url = _Settings.restURLs.collection + collectionName;
            if (id) {
                url += '/' + id;

                if (childCollections) {
                    var stringJSON = childCollections;
                    if (typeof stringJSON === 'object') {
                        stringJSON = JSON.stringify(stringJSON);
                    }

                    url += '?childCollections=' + stringJSON;
                }


                return this._ajaxDeleteRest(url, {context: this});
            }
            return {};
        },

        getData: function (ids) {
            var id;
            var deferreds = [];
            var url = _Settings.restURLs.data;
            if (typeof ids === 'string') {
                url += '/' + ids;
                return this._ajaxGetRest(url, {context: this});
            }
            for (id in ids) {
                if (ids.hasOwnProperty(id)) {
                    var uri = url + '/' + ids[id];
                    deferreds.push(this._ajaxGetRest(uri, {context: this}))
                }
            }
            return $.when.apply($, deferreds)
                .pipe(function (a1) {
                    var json = [];
                    for (var i = 0; i < arguments.length; i++) {
                        var args = arguments[i];
                        var data = args[0];
                        var jqXHR = args[2];
                        if (jqXHR.status == 200) {
                            json.push(data); //$.parseJSON(jqXHR.responseText));
                        }
                        else {
                            //console.log('jqXHR.status=' + jqXHR.status + ' : ' + data);
                        }
                    }
                    return json;
                });
        },

        createData: function (data, securityLabel) {
            var url = _Settings.restURLs.data + '?' + _Settings.restURLs.securityLabel + '=' + securityLabel;
            return this._ajaxPostRest(url, {
                data: data,
                contentType: 'multipart/form-data',
                processData: false,
                context: this
            });
        },
        deleteData: function (id) {
            var url = _Settings.restURLs.data;
            if (id) {
                url += '/' + id;
                return this._ajaxDeleteRest(url, {context: this});
            }
            return {};
        },

        isArray: function (a) {
            return Object.prototype.toString.apply(a) === '[object Array]';
        },

        getAllUsers: function (securityLabel) {
            //TODO: filtering by security label not currently supported by backend service

            var url = _Settings.restURLs.retrieveUsers;
            return this._ajaxGetRest(url, {context: this});
        },

        getUsers: function (users, securityLabel) {
            //TODO: filtering by security label not currently supported by backend service

            if (!$.securityLabelRest.isArray(users) || users.length == 0) {
                console.log("Invalid list of users received");
                return null;
            }

            var url = _Settings.restURLs.retrieveUsers + "?";
            for (i = 0; i < users.length; i++) {
                url += "user=" + users[i] + "&";
            }

            url = url.substring(0, url.length - 1);
            return this._ajaxGetRest(url, {context: this});
        },

        getGroups: function (groups, securityLabel) {
            //TODO: filtering by security label not currently supported by backend service

            if (!$.securityLabelRest.isArray(groups) || groups.length == 0) {
                console.log("Invalid list of groups received");
                return null;
            }

            var url = _Settings.restURLs.retrieveGroups + "?";
            for (i = 0; i < groups.length; i++) {
                url += "group=" + groups[i] + "&";
            }

            url = url.substring(0, url.length - 1);
            return this._ajaxGetRest(url, {context: this});
        },

        lastProperty: 'The END of Widget swif.securityLabelRest' // Do Not move, for convenience only
    });

// Create a jQuery utility pointing to our widget
// Initialize the WIDGET...........
    $('body').securityLabelRest();
});

/**
 * Created with IntelliJ IDEA.
 * User: berkich
 * Date: 6/21/13
 *
 * $.securityLabelRest is the global jQuery identifier to access this widget
 */
/*jslint plusplus: true */


jQuery(function ($, undefined) {

    var bannerContainer = 'body';
    var securityLabelName = 'securityLabel';
//    var securityAggregateName = 'securityAggregate';
    var securityAggregateName = 'securityLabel'; //configured for record level MAC, aggregate is same as reg label
    var _Settings = {};

    $.widget("swif.securityLabelRest", $.swif.securityLabel, {
        //className:'swif-security',
        body: $('body'),
//        $security:$.noop(),
        defaultLabel: ['U'],
        options: {
//            dialogEventId: 'securityDialog',
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
                //"classifications":"classifications",
//                labelUser : "label/user/"
                retrieveUsers: "retrieveUsers",
                retrieveGroups: "retrieveGroups"
            },
            ajax: {
                type: 'GET', contentType: 'application/json', dataType: 'json', context: this, timeout: 120 * 1000
            }
        },
        _create: function () {
            //this.element.addClass( this.className );
            console.log("SWIF_REST_CREATE()");
            this._super();
            _Settings = this.options;
            if (mil.js.swif.restPath !== undefined) {
                _Settings.restPath = mil.js.swif.restPath;
            }
            $.securityLabelRest = this.element.data("swifSecurityLabelRest");
            //this._update();
        },
        _destroy: function () {
            console.log("SWIF_REST_DESTROY()");
            return this._super();
        },
        //// REST private function ////
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
                    //alert('Error Sending/Receiving data: You may need to sign out and login in!');
                    //window.top.location.href = "/owf/j_spring_cas_security_logout";
                } else {
                    SWIF.displayMessage('Error Sending/Receiving data: ' + textStatus, 'Server Error');
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
        getDialogLabel: function (SecurityLabel, title) {
            var dfd;
            //console.log('REST getDialogLabel: CALL this.getDialogLabel() ');
            dfd = this._super(SecurityLabel, title);
            return dfd;
        },

        getLabel: function () {
            if ($.securityLabelBanner) {
                return $.securityLabelBanner.getLabel();
            }
        },

        setLabel: function (label) {
            if ($.securityLabelBanner) {
                $.securityLabelBanner.setLabel(label);
            }
        },

        addLabel: function (label) {
            if ($.securityLabelBanner) {
                $.securityLabelBanner.addLabel(label);
            }
        },

        formatLabel: function (label) {
            if ($.securityLabelBanner) {
                return $.securityLabelBanner.getBanner(label).text;
            } else {
                return "";
            }

        },

        addRecord: function (record) {
            if ($.securityLabelBanner && record && record[securityAggregateName]) {
                $.securityLabelBanner.addLabel(record[securityAggregateName]);
            }
        },

        addRecords: function (records) {
            if ($.securityLabelBanner && records) {
                var securityLabel = {};
                for (var i = 0; i < records.length; i++) {
                    if (records[i] && records[i][securityAggregateName]) {
                        securityLabel = $.securityLabelBanner.addLabelDest(records[i][securityAggregateName], securityLabel);
                    }
                }
                $.securityLabelBanner.addLabel(securityLabel);
            }
        },
        labelCollection: function (collectionName, collection, clear) {

            var $this = this;
            var dfd = $.Deferred();

            if ($.securityLabelBanner) {
                collection[securityLabelName] = $.securityLabelBanner.getLabel();
            }

            if (collection[securityLabelName] === undefined) {
                collection[securityLabelName] = this.defaultLabel;
            }

            var routine;
            if (!collection._id || !collection._id.$oid) {
                routine = this.createCollection
            } else {
                routine = this.updateCollection
            }
            //id = collection._id.$oid;

            $.securityLabelRest.getDialogLabel(collection[securityLabelName], collectionName).done(function (label) {
                if (label) {
                    collection[securityLabelName] = label;
                    if (clear) {
                        $.securityLabelRest.clearLabel();
                    }

                    if ($.securityLabelBanner) {
                        collection[securityAggregateName] = $.securityLabelBanner.aggregateLabels(collection);
                    }

                    routine.call($this, collectionName, collection)
                        .fail(function (jqXHR, textStatus, context) {
                            //--- If the AJAX fails, don't die here. Inform the pending promise.
                            dfd.reject();
                        })
                        .done(function (data, textStatus, jqXHR) {
                            if (data && data._id) {
                                collection._id = {};
                                collection._id.$oid = data._id;
                            }

                            dfd.resolve(collectionName, collection);
                        });
                }
            });
            return dfd.promise();
        },

        getCollection: function (collectionName, ids) {
            var id;
            var deferreds = [];
            var url = _Settings.restURLs.collection + collectionName;
            if (typeof ids === 'string') {
                url += '/' + ids;
                return this._ajaxGetRest(url, {context: this})
                    .done(function (data, textStatus, jqXHR) {
                        $.securityLabelRest.addRecord(data);
                    });
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
                })
                .done(function (data) {
                    $.securityLabelRest.addRecords(data);
                });
        },
        searchCollection: function (collectionName, queryString) {
            var query = 'q=' + queryString.split(' ').join('+');
            return $.securityLabelRest.queryCollection(collectionName, query);
        },

        queryCollection: function (collectionName, queryString) {
            var url = _Settings.restURLs.collection + collectionName + _Settings.restURLs.query;
            if (queryString) {
                url += '?' + queryString;
            }
            return this._ajaxGetRest(url, {context: this})
                .done(function (data) {
                    $.securityLabelRest.addRecords(data);
                });
        },


        //Find has been disabled until we fully assess the security risks of allowing users to perform mongo queries.
        /*
        findCollection: function (collectionName, query) {
            var url = _Settings.restURLs.collection + collectionName + _Settings.restURLs.find;


            if (query) {
                var stringJSON = query;
                if (typeof stringJSON === 'object') {
                    stringJSON = JSON.stringify(stringJSON);
                }

                url += '?q=' + stringJSON;
                //url += '?q=' + encodeURIComponent(stringJSON);
            }


            return this._ajaxGetRest(url, {context: this})
                .done(function (data) {
                    $.securityLabelRest.addRecords(data);
                });
        },*/

        createCollection: function (collectionName, collectionJSON) {
            var url = _Settings.restURLs.collection + collectionName;
            var stringJSON = collectionJSON;
            if (typeof stringJSON === 'object') {
                stringJSON = JSON.stringify(collectionJSON);
            } else {
                collectionJSON = $.parseJSON(collectionJSON);
            }

            return this._ajaxPostRest(url, {data: stringJSON, context: this})
                .done(function (data) {
                    if (!data._id) {
                        alert('Create failed...');
                    } else {
                        $.securityLabelRest.addRecord(collectionJSON);
                    }
                });
        },
        updateCollection: function (collectionName, id, collectionJSON) {
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
            } else {
                collectionJSON = $.parseJSON(collectionJSON);
            }
            return this._ajaxPutRest(url, {data: stringJSON, context: this})
                .done(function () {
                    $.securityLabelRest.addRecord(collectionJSON);
                }
            );
        },

        updateData: function (updatedProperties) {

            var url = "data/update";

            var stringJSON = updatedProperties;
            if (typeof stringJSON === 'object') {
                stringJSON = JSON.stringify(updatedProperties);
            }
            return this._ajaxPutRest(url, {data: stringJSON, context: this});
        },

        deleteCollection: function (collectionName, id, childCollections) {
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
                return this._ajaxGetRest(url, {context: this})
                    .done(function (data) {
                        $.securityLabelRest.addRecord(data);
                    });
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
                })
                .done(function (data) {
                    $.securityLabelRest.addRecords(data);
                });
        },

        createData: function (data, securityLabel) {
            var url = _Settings.restURLs.data + '?' + _Settings.restURLs.securityLabel + '=' + securityLabel;
            return this._ajaxPostRest(url, {
                data: data,
                contentType: 'multipart/form-data',
                processData: false,
                context: this
            })
                .done(function () {
                    $.securityLabelRest.setLabel(securityLabel);
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
        clearLabel: function () {
            if ($.securityLabelBanner) {
                $.securityLabelBanner.clearLabel();
            }
        },


        labelAll: function (obj, label) {
            var k;
            if (obj instanceof Object) {
                obj.securityLabel = label;
                for (k in obj) {
                    if (obj.hasOwnProperty(k)) {  //make sure that the property isn't coming from the prototype
                        if (k != "securityLabel" && k != "_id") {
                            labelAll(obj[k], label);
                        }
                    }
                }
            }
        },

        labelAllNotAlreadyLabeled: function (obj, label) {
            var k;
            if (obj instanceof Object) {
                if (!("securityLabel" in obj)) {
                    obj.securityLabel = label;
                }
                for (k in obj) {
                    if (obj.hasOwnProperty(k)) {  //make sure that the property isn't coming from the prototype
                        if (k != "securityLabel" && k != "_id") {
                            labelAllNotAlreadyLabeled(obj[k], label);
                        }
                    }
                }
            }
        },

        labelInheritFromParentifNotLabeled: function (obj, label) {
            var k;
            if (obj instanceof Object) {
                if (!("securityLabel" in obj)) {
                    obj.securityLabel = label;
                }
                for (k in obj) {
                    if (obj.hasOwnProperty(k)) {  //make sure that the property isn't coming from the prototype
                        if (k != "securityLabel" && k != "_id") {
                            labelInheritFromParentifNotLabeled(obj[k], obj.securityLabel);
                        }
                    }
                }
            }
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

        lastProperty: 'The END of Widget swif.security' // Do Not move, for convenience only
    });
// Create a jQuery utility pointing to our widget
// Initialize the WIDGET...........
    $(bannerContainer).securityLabelRest();
});

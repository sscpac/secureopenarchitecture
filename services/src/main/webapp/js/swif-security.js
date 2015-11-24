/**
 * Created with IntelliJ IDEA.
 * User: berkich
 * Date: 6/20/13
 * $.securityLabel is the global jQuery identifier to access this widget

 */
/*jslint plusplus: true */

var mil = mil || {};
mil.js = mil.js || {};
mil.js.swif = mil.js.swif || {};
if (typeof mil.js.swif.loadAllScripts === 'undefined') {
    mil.js.swif.contextPath = '/swif/';
    mil.js.swif.restPath = mil.js.swif.contextPath + 'svcs/';
//    mil.js.swif.resourcePath = mil.js.swif.contextPath;
//    mil.js.swif.jsPath = mil.js.swif.resourcePath + "js/";
//    mil.js.swif.cssPath = mil.js.swif.resourcePath + "css/";
/*
    mil.js.swif.getCachedScript = function (url, options) {

        // allow user to set any option except for dataType, cache, and url
        options = jQuery.extend(options || {}, {
            dataType:"script",
            cache:true,
            url:url
        });
        // Return the jqXHR object so we can chain callbacks
        return jQuery.ajax(options);
    };
    
    mil.js.swif.loadAllScripts = function (baseURL, scripts) {
        // Load a bunch of scripts and make sure the DOM is ready.
        if (scripts.length > 0) {
            jQuery.holdReady(true);   // Do not let jQuery ready function fire until we load our scripts
            var s, deferreds = [];
            for (s in scripts) {
                if (scripts.hasOwnProperty(s)) {
                    //var sFile = baseURL + scripts[s];
                    deferreds.push(mil.js.swif.getCachedScript(baseURL + scripts[s]));
                }
            }
            jQuery.when.apply(jQuery, deferreds).done(
                function () {
                    // The DOM is ready to be interacted with AND all of the scripts have loaded.
                    jQuery.holdReady(false);
                }
            );
        }
    };
    // Call the Script loader and get the Script files

    mil.js.swif.loadAllScripts(mil.js.swif.jsPath,
        [
        //    'jquery-ui-1.10.3.core.widgets.min.js'
            //'swif-banner.js',
            //'swif-rest.js'
        ]
    );
    mil.js.swif.loadCSS = function (baseURL, cssFiles) {
        var cssFile;
        for (cssFile in cssFiles) {
            if (cssFiles.hasOwnProperty(cssFile)) {
                $("<link>", {
                    rel:"stylesheet",
                    type:"text/css",
                    href:mil.js.swif.cssPath + cssFiles[cssFile]
                }).appendTo("head");
            }
        }

    };
    // Call the CSS loader and get the CSS files
    mil.js.swif.loadCSS(mil.js.swif.cssPath,
        [
            //'jquery-ui-1.10.3.custom.min.css',
            'banner.css'
        ]
    );
    */

    jQuery.wait = function (time) {
        return jQuery.Deferred(function (dfd) {
            setTimeout(dfd.resolve, time);
        });
    };
}

jQuery(function ($, undefined) {

    $.widget("swif.securityLabel", {
        //className:'swif-security',
        body:$('body'),
//        SecurityLabel:{},
//        SecurityLabelSettings:{},
        $jTop: null,
        $topDialog: null,
        options:{
        },
        _create:function () {
            //this.element.addClass( this.className );
            console.log("SWIF_SECURITY_CREATE()");
            $.securityLabel = this.element.data("swifSecurityLabel");

	    if (window.top != window) {
                this.$jTop = window.top.jQuery;//.noConflict();
            } else {
                this.$jTop = $;
            }
            this.$topDialog = this.$jTop('#swifDivDialog');

	    //this._update();
        },
        _init:function () {
            ////initialization////
            //console.log("SWIF_SECURITY_INIT()");

        },
        //Plugin users can change options after a plugin has been invoked using
        // $("elem").filterable("option","className","newName");.
        // If modifiying a particular option requires an immediate state change,
        // use the _setOption method to listen for the change and act on it.
//        _setOption:function (key, value) {
//            //this.options[ key ] = value;
//            var oldValue = this.options[ key ];
//            // Check for a particular option being set
//            if (key === "className") {
//                // Gather all the elements we applied the className to
//                this.filterInput.parent().add(this.filterElems)
//                    // Wwitch the new className in for the old
//                    .toggleClass(oldValue + " " + value);
//            }
//            // Call the base _setOption method
//            //this._superApply( argruments );
//            this._update();
//
//            // The widget factory doesn't fire an callback for options changes by default
//            // In order to allow the user to respond, fire our own callback
//            this._trigger("setOption", null, {
//                option:key,
//                original:oldValue,
//                current:value
//            });
//        },
        _update:function () {
        },
        _destroy:function () {
            console.log("SWIF_SECURITY_DESTROY()");
            return this._super();
        },

//        addLabel:function (destSecurityLabel, newLabel) {
//			 console.log("WARNING: addLabel method should not be called within the swif-security.js");
//
//        },
//        isArray: function(a) {
//            return Object.prototype.toString.apply(a) === '[object Array]';
//        },
        // add all the security labels in the collection.
        // Collection could be an array of objects or a single object
        // If the "deep" parameter is true then look at all "SecurityLabel" objects within the collection
//        _getSecurityLabels: function(deep, collectionObj, newSecurityLabel) {
//            var self = this;
//            Object.keys( collectionObj ).forEach( function( prop ) {
//                // Check if the property is an object
//                var val = this[prop];
//                if (prop === 'SecurityLabel') {
//                    if (collectionObj) {
//                        self.addLabel(newSecurityLabel, val);
//                        // return false; // Not sure if forEach supports returning false to stop iteration
//                    }
//                }
//                else if (deep && typeof val === 'object') {
//                    var sl = self._getSecurityLabels( deep, val, newSecurityLabel);
//                }
//            }, collectionObj );
//            return newSecurityLabel;
//        },
        // if p1 == true then do a DEEP search through the collection (p2), the securityLabel is p3
        // if p1 != true then p1 is the collection and p2 is the securityLabel
        // returns the addition of all security labels
//        getSecurityLabels: function(p1, p2, p3) {
//            var deep = false,
//                collectionObj = p1,
//                securityLabel = p2;
//            if (typeof p1 === "boolean" && p1) {
//                deep = true;
//                collectionObj = p2;
//                securityLabel = p3;
//            }
//            if (securityLabel === undefined) {
//                securityLabel = {};
//            }
//            if (this.isArray(collectionObj )) {
//                var self = this;
//                $.each(collectionObj, function(index, value) {
//                    self.addLabel(securityLabel, value);
//                });
//            } else {
//                this._getSecurityLabels( deep, collectionObj, securityLabel );
//            }
//            return securityLabel ;
//        },
        getDialogLabel: function(currentLabel, title) {
            var t = this.$jTop.securityLabelDialog;
            if (t) {
                var vRet = t.getDialogLabel(currentLabel, title);
                return vRet;
            } else {
                console.log('Could not find the TOP window SWIF Dialog');
                return {};
            }
        },

        lastProperty:'The END of Widget swif.security' // Do Not move, for convenience only
    });
    // Create a jQuery utility pointing to our widget
    //$.securityLabel =  $(":swif-security").data("swifSecurity");

    // Initialize the WIDGET...........
    $('body').securityLabel();
});



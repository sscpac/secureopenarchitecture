/**
 * Created with IntelliJ IDEA.
 * User: berkich
 * Date: 6/6/13
 *
 * $.securityLabelBanner is the global jQuery identifier to access this widget
 */
/*
mil.js.swif.loadAllScripts(mil.js.swif.jsPath,
    [
        'swif-banner-config.js',
        'SwifPubSub.js'
    ]
);
mil.js.swif.loadCSS(mil.js.swif.cssPath,
    [
        //'banner.css'
    ]
);
*/
jQuery(function ($, undefined) {
    $.widget("swif.securityLabelBannerRoot", {

        settings:null,
        $banner:null,
        stickyTop: -1,
        SecurityLabel:{},

        // Functions .....
        _create:function () {
            console.log("BANNER_CREATE()");
            $.securityLabelBannerRoot = this.element.data("swifSecurityLabelBannerRoot");
            this.setSettings(mil.js.swif.banner.settings); // defined in swif-banner-config.js
        },
        _wait : function(time) {
            return $.Deferred(function(dfd) {
                setTimeout(dfd.resolve, time);
            });
        },

        createBanner: function($node) {
            var jid = 'div.' + this.settings.bannerAttributes.class;
            var t = $('<div/>', this.settings.bannerAttributes).wrapAll('<div></div>').parent().html();
            this.$banner = $node.prepend(t).find(jid);
	},

	//// BANNER public function ////
        setSettings:function (newSettings) {
            this.settings = newSettings;
        },

        getBanner:function (jsonSecurityLabel) {
            var self = this;
            var cssName = '';

            if (!jsonSecurityLabel.classification) {
                jsonSecurityLabel.classification = ['U'];
            }

            var bannerText = '';
            $.each(this.settings.fields, function (securityType) {

                var securitySet = jsonSecurityLabel[securityType];
                if (securitySet) {
                    var securityTypeSettings = self.settings.fields[securityType];

                    //if we are going to have this check, should it be done elsewhere?
                    if (!securityTypeSettings.bannerPrefix) {
                        securityTypeSettings.bannerPrefix = '//';
                    }

                    var bFirst = true;
                    $.each(securitySet, function (sInx, securityVal) {

                        var displayText = securityVal;
                        if (securityTypeSettings.label && securityTypeSettings.valueSet
                            && securityTypeSettings.valueSet[securityVal]
                            && securityTypeSettings.valueSet[securityVal].label) {
                            displayText = securityTypeSettings.valueSet[securityVal].label;
                        }
                        //TODO: should prob make this configurable
                        if (securityType == 'classification') {
                            cssName = securityVal;
                        }
                        else {
                            if (bFirst) {
                                bannerText += securityTypeSettings.bannerPrefix;
                                bFirst = false;
                            }
                            else {
                                bannerText += ',';
                            }
                        }
                        bannerText += displayText;
                    });
                }
            });
            return {cssName:cssName, text:bannerText.toUpperCase()};
        },

        //if we think it would be valuable, could add a parameter here to choose which part of the html to add the banner to
        attachBanner:function (where) {

            if (this.$banner) {
                console.log("Banner already exists");
            }
            else {
                if ( ! where) {
                    where = $('body');
                }
                this.createBanner(where);
                this.$banner.click(this.clickEditLabel);
            }
            this.clearLabel();
        },

        _showBanner:function () {
            this.publishUpdate();
            this.setBanner(this.SecurityLabel);
        },

        setBanner:function (securityLabel, banner) {
            var bannerContent = this.getBanner(securityLabel);
            var txt = bannerContent.text;
            //console.log('setBanner: ' + txt);
            if (! banner) {
                banner = this.$banner;
            }
            var cssName = ' ' + bannerContent.cssName;
            banner.removeClass();
            banner.addClass(this.settings.bannerAttributes.class + cssName);
            banner.html(txt);
        },
        addLabelDest:function (newLabel, destSecurityLabel) {
            var self = this;

            $.each(newLabel, function (securityId, valueSet) {

                var securityField = self.settings.fields[securityId];
                if (valueSet && securityField) {

                    if (!destSecurityLabel[securityId]) {
                        destSecurityLabel[securityId] = [];
                    }
                    $.each(valueSet, function (inx, value) {
                        if (-1 == $.inArray(value, destSecurityLabel[securityId])) {
                            if (securityField.multiple || destSecurityLabel[securityId].length == 0) {
                                destSecurityLabel[securityId].push(value);
                            } else {
                                //Check to see proper settings exist and then set based on rank
                                if (securityField.valueSet && securityField.valueSet[value]
                                    && securityField.valueSet[destSecurityLabel[securityId][0]]) {
                                    if (securityField.valueSet[value].rank >
                                        securityField.valueSet[destSecurityLabel[securityId][0]].rank) {
                                        destSecurityLabel[securityId][0] = value;
                                    }
                                } else {
                                    console.log("Ranking unavailable for singular field " + securityId);
                                }
                            }
                        }
                    });
                }
            });
            return destSecurityLabel;
        },
        _addLabel:function (newLabel) {
            this.SecurityLabel = this.addLabelDest(newLabel, this.SecurityLabel);
            return this.SecurityLabel;
        },

        addLabel:function (newLabel) {
            var label = this.addLabelDest(newLabel, this.SecurityLabel);
            this._showBanner();
            return label;
        },
        _setLabel:function (newLabel) {
            this._clearLabel();
            this._addLabel(newLabel);
        },
        setLabel:function (newLabel) {
            this._clearLabel();
            this.addLabel(newLabel);
        },
        _clearLabel:function () {
            this.SecurityLabel = {};
        },
        clearLabel:function () {
            this._clearLabel();
            this._showBanner();
        },

	getLabel:function () {
	    return this.SecurityLabel; 
	},

        publishUpdate:function () {
            this._publishWithType("update", this.SecurityLabel);
        },

        publishClose:function () {
            this._publishWithType("close", {});
        },

        _publish:function (payload) {
            console.log("Publishing: to NULL");
            console.log(payload);
        },

        _publishWithType:function (msgType, payload) {
            var messageToSend = {
                "messageType":msgType,
                "data":payload
            };
            this._publish(messageToSend);
        },

        lastProperty:'END swif.securityLabelBannerRoot' // Do Not move, for convenience only
    }); // End of Widget

    //$('body').securityLabelBannerRoot();
    $.widget("swif.securityLabelBanner", $.swif.securityLabelBannerRoot, {

        eventController: null,
        eventMonitor: {},
        clientWidgets: {},
        bannerServiceName: 'swifBanner',
        // Functions .....
        _create:function () {
            console.log("OWF-BANNER_CREATE()");
            this._super();
            $.securityLabelBanner = this.element.data("swifSecurityLabelBanner");
            this.eventController = new Ozone.eventing.Widget('/owf/js/eventing/rpc_relay.uncompressed.html');   
        },

        _handleStateEvent:function (sender, msg) {
            if (msg.eventName == 'beforeclose') {
                console.log("Widget Closing ");

                //Publish Widget Close
                $.securityLabelBanner.publishClose();

                $.securityLabelBanner.eventMonitor.widgetState.removeStateEventOverrides({
                    events:['beforeclose'],
                    callback:function () {
                        // close widget in callback
                        $.securityLabelBanner.eventMonitor.widgetState.closeWidget();

                        //--- Broadcast message that widget has been closed
                        SWIF.publish(SWIF.Channels.WIDGET_CLOSED, {
                            widgetId: $.securityLabelBanner.eventMonitor.widgetState.widgetIdJSON
                        });
                    }
                });
            }else if (msg.eventName == 'show') {
                //Publish Widget Update when "showing" widgets to maintain proper security banner when changing dashboards
                $.securityLabelBanner.publishUpdate();
            }
        },

        attachBanner:function (where) {

            SWIF.launchWidget(SWIF.WidgetNames.BANNER_SERVICE, null, true, $.noop);
            this._super(where);
            this._wait(1500).then($.proxy(this.publishUpdate, this));
            //Setup Event Controller
            //	console.log('ATTACHBANNER this.evenController: ' + this.evenController);
            //Setup Event Listener for Close
            this.eventMonitor.widgetEventingController = Ozone.eventing.Widget.getInstance();
            this.eventMonitor.widgetState = Ozone.state.WidgetState.getInstance({
                widgetEventingController:this.eventMonitor.widgetEventingController,
                onStateEventReceived:this._handleStateEvent
            });
            this.eventMonitor.widgetState.addStateEventOverrides({ events:['beforeclose']});
            this.eventMonitor.widgetState.addStateEventListeners({ events:['show']});

        },
        // Used by clients
        _publish:function (payload) {
            //console.log("Publishing:");
            //console.log(payload);
            if (this.eventController) {
                this.eventController.publish(this.bannerServiceName, payload);
            } else {
                console.log('this.evenController==NULL');
            }
        },
        _msgReceive: function(sender, msg) {
            var id = $.parseJSON(sender).id;
            console.log("Message Received with type " + msg.messageType + " from ID " + id);
            if (msg.messageType == 'update') {
                this.clientWidgets[id] = msg.data;
            } else if (msg.messageType == 'close') {
                delete this.clientWidgets[id];
            }
            this.bannerServiceUpdate();
        },

        aggregateLabels:function (obj) {
            var aggregate = {};
            this._aggregateLabels(obj, aggregate);
            return aggregate;
        },

        _aggregateLabels:function (obj, aggregate) {
            var k;
            if (obj instanceof Object) {
                for (k in obj) {
                    if (obj.hasOwnProperty(k)) {  //make sure that the property isn't coming from the prototype
                        if (k == "securityLabel") {
                            aggregate = $.securityLabelBanner.addLabelDest(obj[k], aggregate);
                        } else {
                            this._aggregateLabels(obj[k], aggregate);
                        }
                    }
                }
            }
        },

        bannerServiceUpdate: function() {
            var securityLabel = {};
            var self = this;
            $.each(this.clientWidgets, function (inx, value) {
                securityLabel = self.addLabelDest(value, securityLabel); //$.securityLabelBannerOWF
            });
            this.setBanner(securityLabel, this.$banner);
        },
        bannerServiceStart: function($Banner) {
            console.log("Banner Service Widget Starting");
            this.eventController.subscribe(this.bannerServiceName, $.proxy(this._msgReceive,this))
            this.$banner = $Banner;
            this.bannerServiceUpdate();
        },
        lastProperty:'END: swif.securityLabelBannerOWF' // Do Not move, for convenience only
    }); // End of Widget

    $('body').securityLabelBanner();
});

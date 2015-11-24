var SWIF = SWIF ? SWIF : {};

OWF.ready(function(){
	SWIF.eventController = new Ozone.eventing.Widget('/owf/js/eventing/rpc_relay.uncompressed.html');
	SWIF.widgetLauncher = Ozone.launcher.WidgetLauncher.getInstance(SWIF.eventingController);

	/**
	 *Define channels for widgets to listen on
	 */
	SWIF.Channels = {
        USER_SELECT: 'selection_data',
        WIDGET_CLOSED: 'widget_closed'
    };
	
	/**
	 *The value should be the same as the name the widget has in Ozone
	 */
	SWIF.WidgetNames = {
		BANNER_SERVICE: 'Banner Service',
	};
	
	/**
	 *Used for fast retrieval of widget guids
	 */
	SWIF.WidgetGuid = {
		//filled in at runtime as needed
	};
	
	/**
	 *Used to specify different messages types to branch logic while over a single channel
	 */
	SWIF.MessageType = {};

	SWIF.launchWidget = function(widgetName, launchPayload, launchOnlyWhenClosed, callback) {
		var dataString = launchPayload ? Ozone.util.toString(launchPayload) : null;
		var guid = SWIF.WidgetGuid[widgetName];
		if(launchOnlyWhenClosed == undefined || launchOnlyWhenClosed == null) launchOnlyWhenClosed = true;

		if(guid == undefined){
			SWIF.getWidgetId(widgetName, function(){
				SWIF.launchWidget(widgetName, launchPayload, launchOnlyWhenClosed, callback);
			});
		}
		else {
			if(!callback) {
				SWIF.widgetLauncher.launchWidget({
					guid: guid,
					launchOnlyIfClosed: launchOnlyWhenClosed,
					data: dataString
				});
			}
			else {
				SWIF.widgetLauncher.launchWidget({
					guid: guid,
					launchOnlyIfClosed: launchOnlyWhenClosed,
					data: dataString
				}, callback);
			}
		}
	};
	
	SWIF.publish = function(channel, payload) {
      	SWIF.eventController.publish(channel, payload);
	};
	
	SWIF.publishWithType = function(channel, msgType, payload) {
		var messageToSend = {
			"messageType": msgType,
			"data": payload
		};
		SWIF.publish(channel, messageToSend);
	}
	
	SWIF.subscribe = function(channel, callback) {
		SWIF.eventController.subscribe(channel, callback);
	};
	
	SWIF.unsubscribe = function(channel, callback) {
		SWIF.eventController.unsubscribe(channel);
	};
	
	SWIF.getLaunchParams = function() {
		return Ozone.launcher.WidgetLauncherUtils.getLaunchConfigData();
	};

	/**
	 * Get the widget's guid and pass that to a callback function.
	 * @param {String} widgetName
	 * @param {Function} callback - Callback function should check for undefined
	 */
	SWIF.getWidgetId = function(widgetName, callback) {
		var guid = SWIF.WidgetGuid[widgetName];
		
		if(guid && typeof callback == 'function') callback(guid);
		else if(guid && typeof callback != 'function') return guid;
		else{	
			// findWidget, even when passed a name, still returns all widgets
			Ozone.pref.PrefServer.findWidgets({
				onSuccess: function(result) {
					for(var i=0,j=result.length; i<j; i++){
						if(result[i].value.namespace == widgetName){
						  	guid = result[i].id;
						  	break;
						}
					}
					
					if(guid) {
						SWIF.WidgetGuid[widgetName] = guid;
						if(callback) callback(guid);
					}
				},
				onFailure: function(err) {
				//	if(callback) callback();
				}
			});
		}
	};

    /*
     *  Provides mechanism for tighter controls on event-based widget launching,
     *  without destabilizing the original version (SWIF.launchWidget).
     *  1. Verifies existence of specified "payload" document before launching.
     *  2. Prevents duplicate document+widget instances.
     *
     *  NOTE: Using "widgetDocumentId" rather than "documentId", which causes issues with actual DOM documents.
     *
     *  TODO: Non-static way to maintain record-widget associations. Also consider future
     *  cases in which more than one widget may be associated with a specific record type.
     */
    SWIF.launchWidgetUnique = function (collectionName, widgetDocumentId) {
        var me = this,
            collectionWidgets = {
            },
            widgetName = collectionWidgets[collectionName] || undefined;

        if (!widgetName) {
            return;
        }

        //--- Attempt to fetch desired document.
        $.securityLabelRest.getCollection(collectionName, widgetDocumentId).done(function (docObj) {
            if (!docObj) {
                Ext.Msg.alert('Invalid', 'Document no longer exists.');
                return;
            }

            var getOpenWidgetsDeferred = $.Deferred(),
                launchWidget = function() {
                    var payload = {
                        /*
                         *  NOTE: The original intent was to pass the document (record) directly to the target widget,
                         *  to preclude a redundant server trip. However, this is not feasible because of the security
                         *  label activity that is applied to the active widget during AJAX; We *could* conceivably
                         *  circumvent that, but almost certainly a bad idea. Sacrificing the extra bit of efficiency
                         *  for sake of observing established security functionality. wms 2/19/15
                         */
                        //data: docObj,
                        id: widgetDocumentId
                    };
                    SWIF.launchWidget(widgetName, payload, false, function () {});
                };

            //--- Get a collection of all open widgets.
            OWF.getOpenedWidgets(function (openWidgets) {
                //--- Suspend comparison of open widgets pending the value-passing callback.
                getOpenWidgetsDeferred.resolve(openWidgets);
            });

            //--- When references to all open widgets are available, check them for widget+document matches.
            getOpenWidgetsDeferred.done(function (openWidgets) {
                var proxyPromises = [],
                    proxies = [];

                //--- Get promises for proxies of all open widgets of specified type
                $(openWidgets).each(function (index, value) {
                    if (value.name === widgetName) {
                        var widgetProxyDeferred = $.Deferred();
                        OWF.RPC.getWidgetProxy(value.id, function (widgetProxy) {
                            //--- Add the proxy to the collection and release the deferral.
                            proxies.push(widgetProxy);
                            widgetProxyDeferred.resolve();
                        });
                        proxyPromises.push(widgetProxyDeferred);
                    }
                });

                /*  When all proxies have been populated, query them to retrieve documentIds.
                 *  NOTE: Requires that the target function be RPC-registered (OWF.RPC.registerFunctions)
                 */
                $.when.apply($, proxyPromises).done(function () {
                    if (proxies.length == 0) {
                        //-- No widgets of that type are open; go ahead and open as requested.
                        launchWidget();
                        return;
                    }

                    var idPromises = [],
                        proxiesById = [];
                    //--- Get promises for the documentIds of all open widget proxies
                    $(proxies).each(function (index, wp) {
                        //--- Ensure that the target function actually exists. If not, the check is skipped.
                        if (wp && typeof wp.getWidgetDocumentId === 'function') {
                            var proxyCallDeferred = $.Deferred();
                            wp.getWidgetDocumentId(function (id) {
                                    //--- Add the documentId to the collection and release the deferral.
                                    proxiesById[id] = wp;
                                    proxyCallDeferred.resolve();
                                }
                            );
                            idPromises.push(proxyCallDeferred);
                        }
                    });

                    /*  When all id have been retrieved from the open widgets of the specified type,
                     *  compare the requested documentId and only open if the id is not already open
                     *  in the associated widget.
                     *  If the specified widget+documentId is already open, restore that widget
                     *  to its opened state (size, position) and bring to top.
                     */
                    $.when.apply($, idPromises).done(function () {
                        if (proxiesById[widgetDocumentId] !== undefined) {
                            proxiesById[widgetDocumentId].bringWidgetToFront();
                        } else {
                            launchWidget();
                        }
                    });
                });
            });
        });

    };

    /*
     *  Provides a way to launch a warning about unsaved changes when closing a widget.
     *  Parameters: additionalBeforeCloseFunction - function in calling controller to determine whether widget is
     *      dirty.
     *      Return Values:
     *          true - widget is dirty
     *          false - widget is not dirty
     */
    SWIF.addBeforeCloseSaveChangesPrompt = function(additionalBeforeCloseFunction){
        var widgetState = Ozone.state.WidgetState.getInstance({
            onStateEventReceived: function(sender, msg) {
                var event = msg.eventName;
                if(event === 'beforeclose') {

                    // Call function sent from calling controller
                    var isWidgetDirty = additionalBeforeCloseFunction();
                    // If the widget is not dirty, close it
                    if(!isWidgetDirty){
                        $.securityLabelBanner._handleStateEvent(sender, msg);
                    }
                    // If the widget is dirty, prompt the user for confirmation of widget closing
                    else{
                        Ext.Msg.show({
                            title:'Warning',
                            msg: 'You currently have unsaved changes.  Are you sure<br> you wish to proceed?  All unsaved changes will be lost.',
                            icon: Ext.Msg.QUESTION,
                            buttons: Ext.Msg.YESNO,
                            fn: function(btn) {
                                if (btn === 'yes') {
                                    $.securityLabelBanner._handleStateEvent(sender, msg);
                                }else{
                                    return false;
                                }
                            }
                        });
                    }
                }else{
                    $.securityLabelBanner._handleStateEvent(sender, msg);
                }
            }
        });

        // override beforeclose event so that we can clean up
        // widget state data
        widgetState.addStateEventOverrides({
            events: ['beforeclose']
        });
        widgetState.addStateEventListeners({ events:['show']});
    };


    SWIF.displayMessage = function(message, title) {
    	alert(message);
    };    

})


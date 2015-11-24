/**
 * Created with IntelliJ IDEA.
 * User: berkich
 * Date: 8/07/13
 * $.securityLabelDialog is the global jQuery identifier to access this widget

 */
/*jslint plusplus: true */
/*
 if (typeof mil.js.swif.loadAllScripts !== 'undefined') {

 // Call the CSS loader and get the CSS files
 mil.js.swif.loadCSS(mil.js.swif.cssPath,
 [
 'jquery-ui-1.10.3.custom.min.css',
 'chosen.css'
 ]
 );
 mil.js.swif.loadAllScripts(mil.js.swif.jsPath,
 [
 'jquery-ui-1.10.3.custom.min.js',
 'chosen.jquery.min.js'
 ]
 );
 }
 */
jQuery(function ($, undefined) {

    $.widget("swif.securityLabelDialog", {
        //className:'swif-security',
        body:$('body'),
        eventDialog:null,
        deferred:null,
        SecurityLabel:{},
        SecurityLabelSettings:{},
        options:{
            dialogAttributes:{
                id:'swifSecurityDialog',
                class:'swif-security-dialog',
                autoOpen:false,
                height:450,
                width:400,
                //zIndex:1000000, must be changed in in the uiDialog css file
                modal:true
            },
            ajaxTimeout:120,
            labelUrl:'/swif/svcs/dialog'
        },
        _create:function () {
//            console.log("SWIF_DIALOG_CREATE()");
            $.securityLabelDialog = this.element.data("swifSecurityLabelDialog");
//            console.log("SWIF_DIALOG_CREATED");
        },
        _init:function () {
            ////initialization////
            //console.log("SWIF_DIALOG_INIT()");

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
//            console.log("SWIF_DIALOG_UPDATE()");
        },
        _destroy:function () {
//            console.log("SWIF_DIALOG_DESTROY()");
            return this._super();
        },
        //initialize security the security label dialog
        setupDialog:function (where) {
            if (!where) {
                where = this.body; // parent.document;
            }             //check if sec-dialog div exists, if not create it
            if (!this.$dialog) {
                console.log("creating Dialog div");

                $('<div/>', {
                    id:'swifDivDialog',
                    title:"Assign Security Labels to Item"
                }).appendTo(where);
                this.$dialog = $('#swifDivDialog');
                this._initDialog();
            }
        },
        //// DIALOG private functions  ////
        _initDialog:function ($attachTo) {

            console.log("dialog init");

            var self = this;
            this.SecurityLabelSettings = {};
//                console.log("Inside Main Init");
            //retrieve labeling information
            if (mil.js.swif.debug) {
                this.SecurityLabelSettings = mil.js.swif.debug.securityLabelSettings;
                this._buildSecDialog(mil.js.swif.debug.securityLabelSettings);
            } else {
                $.ajax({
                    url:this.options.labelUrl,
                    dataType:"json",
                    context:this
                }).done(function (data, textStatus, jqXHR) {
                        self.securityLabelSettings(data.SecurityLabel);
                        self._buildSecDialog(data.SecurityLabel);
                    }).fail(function (jqXHR, textStatus, context) {
                        if (textStatus === 'parsererror') {
                            alert('Error retrieving security information: You may need to sign out and login in!');
                            //window.top.location.href = "/owf/j_spring_cas_security_logout";
                        } else {
                            alert('Error Sending/Receiving data: ' + textStatus);
                        }
                    });
            }
        },

        //build security labeling dialog
        _buildSecDialog:function (securityFields) {

            //check if sec-dialog div exists, if not create it
            if (!this.$dialog) {
                console.log("creating Dialog div");
            }

            var $form = $("<form>");
	    var $options = null;
            var array = $.map(securityFields,function (value, key) {
                return {key:key, value:value};
            }).sort(function (a, b) {
                    return a.value.rank - b.value.rank;
                });
            //create a label and select box for each security field
            $.each(array, function (index, value) {
                var field = value.key;
                var vals = value.value;		

                if (vals.type == "boolean") {

		  if($options == null) {
                    $options = $("<span/>", {
                            id:"div_options"
                        }
                    ).appendTo($form);
                    
		    $("<h3> Options </h3>").appendTo($options);
		  }	  

		  var $container = $("<div/>").appendTo($options);

                  var $checkbox = $("<input/>", {
                          id:"swifsec_" + field,
                          name:field,
		    	  type: "checkbox"
                      }
                  ).appendTo($container);

		  $('<label />', {
			  'for': "swifsec_" + field, 
			  style: "padding-left:5px;font-size:12px",
			  text: vals.displayName 
		  }).appendTo($container);

		    $options.accordion({
                        collapsible:true,
                        heightStyle:"content"
                    });
                } else if(vals.type === "multiple" || vals.type === "single") {

                    //add accordion label
                    var $accordion = $("<span/>", {
                            id:"div_" + field
                        }
                    ).appendTo($form);
                    $("<h3>" + vals.displayName + "</h3>").appendTo($accordion);
                    var $divContent = $("<div/>").css("overflow", "visible").appendTo($accordion);

                    //add select box
                    var $selectbox = $("<select/>", {
                            id:"swifsec_" + field,
                            name:field,
                            "data-placeholder":"Select an option",
                            style:"width:100%;height:40px;",
                            class:"chosen-select"
                        }
                    );

                    //check whether or not to accept multiple values
                    if (vals.type === "multiple") {
                        $selectbox.attr("multiple", "multiple");
                    }

                    $selectbox.appendTo($divContent);

                    //set the options for this select box
                    var valueSet = vals.valueSet ? vals.valueSet : vals.ValueSet
                    $.each(valueSet, function () {
                        var $option = $("<option>" + this.label + "</option>").val(this.value);
                        $option.appendTo($selectbox);
                    });

                    //configure accordion
                    $accordion.accordion({
                        collapsible:true,
                        heightStyle:"content"
                    });
                }
            });

            this.$dialog.html($form).dialog(this.options.dialogAttributes);
            //apply chosen to select boxes
            this.$dialog.find(".chosen-select").chosen({width:"100%"});
        },
        //perform save
        _getDialogLabel:function () {
            label = {};
            this.$dialog.find('select').each(function () {
                var $this = $(this);
                var vals = [];
                var val = $this.val();
                if (val) {
                    if ($this.attr("multiple")) {
                        vals = val;
                    } else {
                        vals.push(val);
                    }
                }
                label[this.name] = vals;
            });

            this.$dialog.find(':checkbox').each(function () {
		label[this.name] = $(this).is(':checked');
	    });
	    


            //console.log('Dialog Label:' + JSON.stringify(label));
            return label;
        },

        getDialogLabel:function (currentLabel, title) {
            var dfd = new $.Deferred(function (defer) {
            });
            if (this.$dialog) {
                //define the Save Label button
                this.$dialog.dialog({
                    buttons:{
                        "Save Label":$.proxy(function () {
                            this.$dialog.dialog("close");
                            dfd.resolve(this._getDialogLabel());
                        }, this)
                    }
                });
                /*
                 if (title) {
                 var t = this.$dialog.dialog("option", "title");
                 t += ' to ' + title;
                 this.$dialog.dialog("option", "title", t);
                 }
                 */
                if (currentLabel) {
                    //set select boxes default values to the values received in the document
                    this.$dialog.find('select').each(function (a) {
                        if (currentLabel[this.name] != undefined) {
                            $(this).val(currentLabel[this.name]);
                        } else {
                            $(this).val([]);
                        }
                    });
                }
                this.$dialog.find(".chosen-select").trigger("chosen:updated"); //have 'chosen' update select boxes with new values
                this.$dialog.dialog("open"); //show security dialog
            }
            return dfd.promise();
        },

        ////   public DIALOG function   ////
        securityLabelSettings:function (newVal) {
            if (newVal) {
                this.SecurityLabelSettings = newVal;
                //console.log('SET self.SecurityLabelSettings: ' + newVal));
            }
            return this.SecurityLabelSettings;
        },
        lastProperty:'The END of Widget swif.securityDialog' // Do Not move, for convenience only
    });

    // Initialize the WIDGET...........
    var $b = $('body')
    $b.securityLabelDialog();
    $.securityLabelDialog.setupDialog($b);
    //$.securityLabelDialog.setupDialog($('body',window.top.document));
});


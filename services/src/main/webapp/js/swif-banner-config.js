var mil = mil || {};
mil.js = mil.js || {};
mil.js.swif = mil.js.swif || {};
mil.js.swif.banner = mil.js.swif.banner || {};
mil.js.swif.banner.settings = {

    channelName:'swifBanner',
    fields:{
        classification:{
            multiple:false, //must define valueset with ranks when multiple equals false
            label:true, //must define valueset with labels when label equals true
            bannerPrefix:"",
            valueSet:{
                TS:{
                    label:"Top Secret",
                    rank:3
                },
                S:{
                    label:"Secret",
                    rank:2
                },
                C:{
                    label:"Confidential",
                    rank:1
                },
                U:{
                    label:"Unclassified",
                    rank:0
                }
            }
        },
        SCI:{
            multiple:true,
            label:false,
            bannerPrefix:"//"
        },
        SAP:{
            multiple:true,
            label:false,
            bannerPrefix:"//"
        },
	RELTO:{
            multiple:true,
            label:false,
            bannerPrefix:"//REL TO "
        }
    },
    bannerAttributes:{
        id:'swifBanner',
        class:'swif-banner',
        title:'Security Label Banner',
        text:'Default Banner Text',
        height:20,
        width:'100%'
    }
}

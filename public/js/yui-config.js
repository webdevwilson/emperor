(function() {
    var filter = (window.location.search.match(/[?&]filter=([^&]+)/) || [])[1] || 'min',
        cdn = 'http://yui.shirley.im/combo?',
        gallery = 'ii-gallery-2012.05.14';

    // YUI Config.
    YUI_config = {
        filter     : filter,
        combine    : filter === 'min',
        allowRollup: false,
        fetchCSS   : false,
        gallery    : gallery,
        groups     : {
            customgallery: {
                combine   : true,
                base      : cdn + gallery + '/build/',
                comboBase : cdn,
                root      : gallery + '/build/',
                filter    : filter,
                patterns  : {
                    "gallery-"    : {},
                    "gallerycss-" : { type : "css" }
                }
            },
            customgallerycss: {
                combine   : true,
                base      : cdn + gallery + '/build/',
                comboBase : cdn,
                root      : gallery + '/build/'
            }
        }
    };
}());
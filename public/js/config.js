var
filter    = (window.location.search.match(/[?&]filter=([^&]+)/) || [])[1] || 'min',
YUI_config = {
  filter : filter,
  groups : {
    emperor : {
      combine  : false,
      base     : '/assets/js/',
      root     : '/assets/js/',
      filter   : 'raw',
      modules  : {
        'emperor-models' : {
          path : 'emperor-models.js',
          requires : [ 'model', 'model-list', 'model-sync-rest' ]
        }
      }
    }
  }
};

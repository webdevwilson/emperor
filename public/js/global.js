YUI().use('array-extras', 'anim', 'gallery-dynamic-dialog', function(Y) {

  var Aeach = Y.Array.each;

  /* This is really all that is necessary to set up the dialogs */
  var dialogs = new Y.DynamicDialog();
  dialogs.setupDelegates();
  dialogs.on('visibleChange', function(e) {
    var panel = e.panel,
      buttons = panel.get('buttons');
    Aeach( buttons.footer, function(button) {
      button.addClass('btn');
      if ( button.hasClass('yui3-dynamic-dialog-submit') ) {
        button.addClass('btn-primary');
      }
    });
  });
});

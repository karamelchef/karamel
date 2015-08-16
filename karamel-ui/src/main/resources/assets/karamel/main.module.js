'use strict';

angular.module('main.module', [
  'ngRoute'
    , 'ngCookies'
    , 'ui.sortable' // moving cards on the board
    , 'ui.bootstrap' // UI framework
    , 'lr.upload' // uploading files
    , "xeditable"// edit project name
    , "oitozero.ngSweetAlert"
    , 'angular-md5'
    , 'blockUI'
]).run(function(editableOptions) {
  editableOptions.theme = 'bs3'; // bootstrap3 theme for xeditable
});

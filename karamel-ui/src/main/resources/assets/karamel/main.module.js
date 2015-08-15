/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */


'use strict';

// Declare app level module which depends on other modules
angular.module('main.module', [
  'ngRoute'
      , 'ngCookies'
      , 'ui.sortable' // moving cards on the board
      , 'ui.bootstrap' // UI framework
      , 'lr.upload' // uploading files
      , "xeditable"// edit project name
      , "oitozero.ngSweetAlert"
      , 'terminal.module'
      , 'angular-md5'
      , 'blockUI'
      , 'shell-emulator.module'
])
    .run(function(editableOptions) {
      editableOptions.theme = 'bs3'; // bootstrap3 theme for xeditable
    })
//
// We add an interceptor to update the UI board name if the karamel-core application is not available.
//
    .config(['$routeProvider', function($routeProvider) {
        $routeProvider.when('/', {templateUrl: 'karamel/board/board.html', controller: 'board.controller'});
        $routeProvider.when('/terminal', {templateUrl: 'karamel/terminal/terminal.html', controller: 'terminal.controller'});
        $routeProvider.when('/experiment', {templateUrl: 'karamel/experiment/experiment.html', controller: 'experiment.controller'});
        $routeProvider.otherwise({redirectTo: '/'});
      }], function($httpProvider, $rootScope) {
      $httpProvider.interceptors.push(function($q) {
        return {
          responseError: function(rejection) {
            if (rejection.status === 0) {
              $rootScope.activeCluster.name = "Karamel Application has Crashed!!";
              return;
            }
            return $q.reject(rejection);
          }
        }
      });
    }
    );

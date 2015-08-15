'use strict';

angular.module('main.module')
  .service('browser-cache.service', ['$log', '$rootScope', '$window', 'alert.service', function($log, $rootScope, $window, alertService) {
      return {
        updateCache: function() {
          if ($window['sessionStorage'] !== undefined) {
            $window.sessionStorage.setItem('karamel', $rootScope.activeCluster === undefined ? null : angular.toJson($rootScope.activeCluster));
          }
          else {
            $log.error("No Support for session storage.");
            alertService.addAlert({type: 'warning', msg: 'Unable to sync updates with cache.'});
          }
        },
        resetCache: function() {
          $rootScope.activeCluster = undefined;

          if ($window['sessionStorage'] !== undefined) {
            $window.sessionStorage.setItem('karamel', $rootScope.activeCluster === undefined ? null : angular.toJson($rootScope.activeCluster));
          }
        }
      }
    }]);
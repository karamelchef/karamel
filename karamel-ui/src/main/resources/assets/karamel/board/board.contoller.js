/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */

'use strict';

angular.module('main.module')
  .controller('board.controller', ['$rootScope', '$log', '$scope', 'active-cluster.service', '$window', 'alert.service',
    function($rootScope, $log, $scope, clusterService, $window, alertService) {
      $scope.clusterService = clusterService;

      function initScope() {
        if ($window['sessionStorage'] !== undefined) {
          var clusterObj = $window.sessionStorage.getItem('karamel');
          clusterObj = angular.fromJson(clusterObj);

          if (clusterObj !== null) {
            try {
              var cluster = new Cluster();
              cluster.copy(clusterObj);
              $rootScope.activeCluster = cluster;
              $rootScope.context = cluster.name;
              alertService.addAlert({type: 'success', msg: 'Model Loaded Successfully.'});
            }
            catch (err) {
              $log.error(err);
              alertService.addAlert({type: 'danger', msg: 'Unable to parse the json to generate model.'});
            }
          }
          else {
            alertService.addAlert({type: 'info', msg: 'Loading new Karamel session.'});
          }
        }
        else {
          $log.error("No Support for session storage.");
        }
      }

      initScope();

    }]);


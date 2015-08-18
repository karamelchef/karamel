'use strict';

angular.module('main.module')
  .controller("alert.controller", ['$log', '$scope', '$timeout', 'alert.service', function($log, $scope, $timeout, alertService) {

      var _defaultTimeout = 3000;
      $scope.alerts = [];

      // Keep track of incoming alerts.
      $scope.$watch(alertService.getAlert, function(alert) {

        if (!(_.isNull(alert))) {
          var length = $scope.alerts.push(alert);

          $timeout(function() {
            if ($scope.alerts.length > 0) {
              $scope.alerts.splice(0, 1);
            }
          }, _defaultTimeout);
        }

      });

      $scope.closeAlert = function(index) {
        $scope.alerts.splice(index, 1);
      };

    }]);
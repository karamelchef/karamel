/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */


'use strict';

angular.module('main.module')
  .controller('group-editor.controller', ['$scope', '$modalInstance', 'groupInfo', function($scope, $modalInstance, groupInfo) {

      function initScope(scope) {

        if (groupInfo !== null) {
          $scope.name = groupInfo.name;
          $scope.size = groupInfo.size;
          $scope.autoScalingEnabled = groupInfo.autoScalingEnabled;
        }
        else {
          $scope.size = 0;
        }

      }

      $scope.pushNodeGroup = function() {
        if (!this.nodeGroupForm.$valid) {
          return false;
        }
        $modalInstance.close({name: $scope.name, size: $scope.size, autoScalingEnabled: $scope.autoScalingEnabled});
      };

      $scope.close = function() {
        $modalInstance.close();
      };

      initScope($scope);

    }]);


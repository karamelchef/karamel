/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */


'use strict';

angular.module('karamel.main')
  .controller('GroupsContoller', ['$scope', '$modalInstance', 'groupInfo', function($scope, $modalInstance, groupInfo) {

      function initScope(scope) {

        if (groupInfo !== null) {
          $scope.name = groupInfo.name;
          $scope.size = groupInfo.size;
        }
        else {
          $scope.size = 0;
        }

      }

      $scope.pushNodeGroup = function() {
        if (!this.nodeGroupForm.$valid) {
          return false;
        }
        $modalInstance.close({name: $scope.name, size: $scope.size});
      };

      $scope.close = function() {
        $modalInstance.close();
      };

      initScope($scope);

    }]);


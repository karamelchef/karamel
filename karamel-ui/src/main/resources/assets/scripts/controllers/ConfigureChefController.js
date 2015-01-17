/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */


'use strict';

angular.module('demoApp')
    .controller('ConfigureChefController', ['$scope', '$modalInstance', 'board', function ($scope, $modalInstance, board) {

        function initScope(scope) {
            scope.boardName = board.name;
            scope.board = column;
            scope.title = '';
        }

        $scope.updateChef = function () {
            if (!this.updateChefForm.$valid) {
                return false;
            }
            $modalInstance.close({title: this.title, board: board});
        };

        $scope.close = function () {
            $modalInstance.close();
        };

        initScope($scope);

    }])
    .controller('NodeGroupConfigurationController', ['$scope', '$modalInstance', 'nodeGroupInfo', function ($scope, $modalInstance, nodeGroupInfo) {

        // Initialize the scope.
        function initScope(scope) {

            if(nodeGroupInfo !== null){
                // If the call is for updation.
                $scope.name = nodeGroupInfo.name;
                $scope.instances = nodeGroupInfo.instances;
            }
            else{
                // Set default value as 0;
                $scope.instances =0;
            }

        }

        // Simply push the new node group information.
        $scope.pushNodeGroup = function(){
            if (!this.nodeGroupForm.$valid) {
                return false;
            }
            $modalInstance.close({name: $scope.name, size: $scope.instances});
        };

        $scope.close = function () {
            $modalInstance.close();
        };

        initScope($scope);

    }]);


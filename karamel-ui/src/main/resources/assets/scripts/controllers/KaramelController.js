/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */

'use strict';

angular.module('demoApp')
    .controller('KaramelController', ['$rootScope', '$log', '$scope', 'BoardService', 'BoardDataService','$window', function ($rootScope, $log, $scope, BoardService, BoardDataService,$window) {

        function initScope(scope) {

//            if($window['sessionStorage'] !== undefined){
//
//                var boardObj = $window.sessionStorage.getItem('karamel');
//                if(boardObj != null){
//                    $rootScope.karamelBoard = BoardService.copyKaramelBoard(angular.fromJson(boardObj));
//                }
//            }
//            else {
//                $log.error("No Support for session storage.");
//            }
        }

        $scope.removeRecipe = function (nodeGroup, cookbook, recipe) {
            BoardService.removeRecipe($rootScope.karamelBoard,nodeGroup, cookbook, recipe);
        };

        $scope.removeColumn = function (nodeGroup) {
            $log.info("Remove Column Invoked.");
            BoardService.removeNodeGroup($rootScope.karamelBoard, nodeGroup);
        };

        $scope.addNewRecipe = function (nodeGroup) {
            $log.info("Add New Recipe invoked");
            BoardService.addNewRecipe($rootScope.karamelBoard, nodeGroup);
        };

        $scope.updateNodeGroup = function (nodeGroup) {
            $log.info("Update Node Group Invoked.");
            BoardService.updateNodeGroupInfo($rootScope.karamelBoard, nodeGroup);
        };


        $scope.configureCookbook = function (nodeGroup) {
            $log.info("Configure Cookbook Called");
            BoardService.configureGroupLevelCookbookAttributes($rootScope.karamelBoard, nodeGroup);
        };

        initScope($scope);

    }]);


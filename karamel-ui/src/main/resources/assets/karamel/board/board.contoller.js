/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */

'use strict';

angular.module('karamel-main.module')
    .controller('board.controller', ['$rootScope', '$log', '$scope', 'board.service', '$window', 'AlertService',
      '$location',
      function($rootScope, $log, $scope, BoardService, $window, AlertService, $location) {
        var self = this;

        $scope.bs = BoardService;


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
                AlertService.addAlert({type: 'success', msg: 'Model Loaded Successfully.'});
              }
              catch (err) {
                $log.error(err);
                AlertService.addAlert({type: 'danger', msg: 'Unable to parse the json to generate model.'});
              }
            }
            else {
              AlertService.addAlert({type: 'info', msg: 'Loading new Karamel session.'});
            }
          }
          else {
            $log.error("No Support for session storage.");
          }
        }



        $scope.configureGlobalProvider = function() {
          BoardService.configureGlobalProvider();
        };
        $scope.configureGlobalAttributes = function() {
          BoardService.configureGlobalAttributes();
        };

        $scope.editSshKeys = function() {
          BoardService.editSshKeys();
        };

        $scope.launchCluster = function() {
          BoardService.launchCluster();
        };

        $scope.switchToTerminal = function() {
          $location.path('/terminal');
        };

        $scope.switchToExperiment = function() {
          $location.path('/experiment');
        };

        $scope.exitKaramel = function() {
          BoardService.exitKaramel();
        };

        $scope.sudoPassword = function(password) {
          BoardService.sudoPassword(password);
        };

        $scope.githubCredentials = function(email, password) {
          BoardService.githubCredentials(email, password);
        };


        $scope.removeRecipe = function(group, cookbook, recipe) {
          BoardService.removeRecipe(group, cookbook, recipe);
        };

        $scope.removeColumn = function(group) {
          BoardService.removeGroup(group);
        };

        $scope.addNewRecipe = function(group) {
          BoardService.addNewRecipe(group);
        };

        $scope.updateGroup = function(group) {
          BoardService.updateGroupInfo(group);
        };

        $scope.configureGroupAttributes = function(group) {
          BoardService.configureGroupAttributes(group);
        };

        $scope.configureGroupProvider = function(group) {
          BoardService.configureGroupProvider(group);
        };

        $scope.addGroup = function() {
          BoardService.addGroup();
        };

        self.setExperimentActive = function() {
          return BoardService.setExperimentActive();
        };
        self.setExperimentInActive = function() {
          return BoardService.setExperimentInActive();
        };

        initScope();

      }]);


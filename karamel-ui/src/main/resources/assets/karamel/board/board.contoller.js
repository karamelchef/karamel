/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */

'use strict';

angular.module('main.module')
    .controller('board.controller', ['$rootScope', '$log', '$scope', 'active-cluster.service', '$window', 'alert.service',
      '$location',
      function($rootScope, $log, $scope, activeClusterService, $window, alertService, $location) {
        var self = this;

        $scope.bs = activeClusterService;


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



        $scope.configureGlobalProvider = function() {
          activeClusterService.configureGlobalProvider();
        };
        $scope.configureGlobalAttributes = function() {
          activeClusterService.configureGlobalAttributes();
        };

        $scope.editSshKeys = function() {
          activeClusterService.editSshKeys();
        };

        $scope.launchCluster = function() {
          activeClusterService.launchCluster();
        };

        $scope.removeRecipe = function(group, cookbook, recipe) {
          activeClusterService.removeRecipe(group, cookbook, recipe);
        };

        $scope.removeColumn = function(group) {
          activeClusterService.removeGroup(group);
        };

        $scope.addNewRecipe = function(group) {
          activeClusterService.addNewRecipe(group);
        };

        $scope.updateGroup = function(group) {
          activeClusterService.updateGroupInfo(group);
        };

        $scope.configureGroupAttributes = function(group) {
          activeClusterService.configureGroupAttributes(group);
        };

        $scope.configureGroupProvider = function(group) {
          activeClusterService.configureGroupProvider(group);
        };

        $scope.addGroup = function() {
          activeClusterService.addGroup();
        };
        
        initScope();

      }]);


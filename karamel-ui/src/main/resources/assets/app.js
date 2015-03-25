/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */


'use strict';

// Declare app level module which depends on other modules
angular.module('demoApp', [
    'ngRoute'
    ,'ngCookies'
//    ,'ngResource' // REST apis
    , 'ui.sortable' // moving cards on the board
    , 'ui.bootstrap' // UI framework
    , 'lr.upload' // uploading files
    , "xeditable"// edit project name
    , "oitozero.ngSweetAlert"
    ,'yamlApp'
    ,'karamel.terminal'
    ,'blockUI'
    , 'ssh.terminal'
])
    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/', {templateUrl: 'karamel.html', controller: 'KaramelController'});
        $routeProvider.when('/terminal',{templateUrl: 'partials/command-page/karamelTerminal.html', controller: 'karamelTerminalController'});
        $routeProvider.otherwise({redirectTo: '/'});
    }])

    .controller("MainCtrl",['$log','$scope','$timeout','AlertService', function($log,$scope,$timeout,AlertService){

        var _defaultTimeout = 3000;
        $scope.alerts = [];

        // Keep track of incoming alerts.
        $scope.$watch(AlertService.getAlert,function(alert){

            if(!(_.isNull(alert))){
                var length = $scope.alerts.push(alert);

                $timeout(function(){
                    if($scope.alerts.length >0){
                        $scope.alerts.splice(0,1);
                    }
                }, _defaultTimeout);
            }

        });

        $scope.closeAlert = function(index) {
            $scope.alerts.splice(index, 1);
        };

    }])
    .service('AlertService',['$log',function($log){

        var _currAlert = null;

        return {

            addAlert : function(alert){
                _currAlert = alert;
            },

            getAlert : function(){
                return _currAlert;
            }
        }

    }])
    .service('KaramelSyncService',['$log','$rootScope','$window','AlertService', function($log,$rootScope,$window,AlertService){

        // Sync Karamel Board with Cache.
        return {

            syncWithCache : function(karamelBoard){

                if($window['sessionStorage'] !== undefined){
                    $window.sessionStorage.setItem('karamel', karamelBoard === undefined? null : angular.toJson(karamelBoard));
                }
                else {
                    $log.error("No Support for session storage.");
                    AlertService.addAlert({type: 'warning' , msg:'Unable to sync updates with cache.'});
                }
            }

        }

    }]);
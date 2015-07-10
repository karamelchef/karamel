/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */


'use strict';

// Declare app level module which depends on other modules
angular.module('karamel.main', [
              'ngRoute'
            , 'ngCookies'
            , 'ui.sortable' // moving cards on the board
            , 'ui.bootstrap' // UI framework
            , 'lr.upload' // uploading files
            , "xeditable"// edit project name
            , "oitozero.ngSweetAlert"
            , 'karamel.terminal'
            , 'angular-md5'
            , 'blockUI'
            , 'karamel.shell.emulator'
])
        .run(function (editableOptions) {
            editableOptions.theme = 'bs3'; // bootstrap3 theme for xeditable
        })
//
// We add an interceptor to update the UI board name if the karamel-core application is not available.
//
        .config(['$routeProvider', function ($routeProvider) {
                $routeProvider.when('/', {templateUrl: 'board.html', controller: 'BoardController'});
                $routeProvider.when('/terminal', {templateUrl: 'terminal.html', controller: 'karamelTerminalController'});
                $routeProvider.when('/experiment', {templateUrl: 'experiment.html', controller: 'ExperimentCtrl'});
                $routeProvider.otherwise({redirectTo: '/'});
            }], function ($httpProvider, $rootScope) {
            $httpProvider.interceptors.push(function ($q) {
                return {
                    responseError: function (rejection) {
                        if (rejection.status === 0) {
                            $rootScope.activeCluster.name = "Karamel Application has Crashed!!";
                            return;
                        }
                        return $q.reject(rejection);
                    }
                }
            });
        }
        )
        .controller("MainCtrl", ['$log', '$scope', '$timeout', 'AlertService', function ($log, $scope, $timeout, AlertService) {

                var _defaultTimeout = 3000;
                $scope.alerts = [];

                // Keep track of incoming alerts.
                $scope.$watch(AlertService.getAlert, function (alert) {

                    if (!(_.isNull(alert))) {
                        var length = $scope.alerts.push(alert);

                        $timeout(function () {
                            if ($scope.alerts.length > 0) {
                                $scope.alerts.splice(0, 1);
                            }
                        }, _defaultTimeout);
                    }

                });

                $scope.closeAlert = function (index) {
                    $scope.alerts.splice(index, 1);
                };

            }])
        .service('AlertService', ['$log', function ($log) {

                var _currAlert = null;

                return {
                    addAlert: function (alert) {
                        _currAlert = alert;
                    },
                    getAlert: function () {
                        return _currAlert;
                    }
                }

            }])
        .service('BrowserCacheService', ['$log', '$rootScope', '$window', 'AlertService', function ($log, $rootScope, $window, AlertService) {
                return {
                    updateCache: function () {
                        if ($window['sessionStorage'] !== undefined) {
                            $window.sessionStorage.setItem('karamel', $rootScope.activeCluster === undefined ? null : angular.toJson($rootScope.activeCluster));
                        }
                        else {
                            $log.error("No Support for session storage.");
                            AlertService.addAlert({type: 'warning', msg: 'Unable to sync updates with cache.'});
                        }
                    },
                    resetCache: function () {
                        $rootScope.activeCluster = undefined;

                        if ($window['sessionStorage'] !== undefined) {
                            $window.sessionStorage.setItem('karamel', $rootScope.activeCluster === undefined ? null : angular.toJson($rootScope.activeCluster));
                        }
                    }
                }
            }]);

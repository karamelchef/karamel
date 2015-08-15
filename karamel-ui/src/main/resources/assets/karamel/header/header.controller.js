'use strict';

angular.module('main.module')
    .controller('header.controller', ['$timeout', '$scope', '$rootScope', 'board.service', 'core-rest.service',
      function ($timeout, $scope, $rootScope, BoardService, coreService) {

        var self = this;

        $rootScope.connected = true;

        $scope.hasEc2 = function () {
          return BoardService.hasEc2();
        };

        $scope.hasBaremetal = function () {
          return BoardService.hasBaremetal();
        };

        $scope.hasProvider = function () {
          return $scope.hasEc2() || $scope.hasBaremetal() ||
              $scope.hasGce() || $scope.hasOpenStack();
        };

        $scope.hasGce = function () {
          return BoardService.hasGce();
        };

        $scope.hasOpenStack = function () {
          return BoardService.hasOpenStack();
        };


        function restartTimer() {
          self.currentTimeout = $timeout(pingServer, 20000);
        }

        function pingServer() {
          coreService.ping()
              .success(function (data, status, headers, config) {
                $rootScope.connected = true;
                restartTimer();
              })
              .error(function (data, status, headers, config) {
                $rootScope.connected = false;
              });
        }


        function _initScope() {
          restartTimer();
        }
        ;

        _initScope();
      }])
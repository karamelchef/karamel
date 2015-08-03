'use strict';

angular.module('karamel.main')
        .controller('HeaderCtrl', ['$timeout', '$scope', 'BoardService', 'KaramelCoreRestServices',
            function ($timeout, $scope, BoardService, KaramelCoreRestServices) {

                var self = this;

                $scope.connected = true;

                $scope.hasEc2 = function () {
                    return BoardService.hasEc2();
                };

                $scope.hasBaremetal = function () {
                    return BoardService.hasBaremetal();
                };

                $scope.hasProvider = function () {
                    return $scope.hasEc2() || $scope.hasBaremetal();
                };



                function restartTimer() {
                    self.currentTimeout = $timeout(pingServer, 500);
                }

                function pingServer() {
                    KaramelCoreRestServices.ping()
                            .success(function (data, status, headers, config) {
                                $scope.connected = true;
                            })
                            .error(function (data, status, headers, config) {
                                $scope.connected = false;
                            });
                    restartTimer();
                }


                function _initScope() {
                    restartTimer();
                };

                _initScope();
            }])
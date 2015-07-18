'use strict';

angular.module('karamel.main')
        .controller('HeaderCtrl', ['$log', '$rootScope', '$scope', 'BoardService',
            function ($log, $rootScope, $scope, BoardService) {

                $scope.hasEc2 = function () {
                    return BoardService.hasEc2();
                };
                
                $scope.hasBaremetal= function () {
                    return BoardService.hasBaremetal();
                };
                
                $scope.hasProvider= function () {
                    return $scope.hasEc2() || $scope.hasBaremetal();
                };


            }])
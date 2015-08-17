'use strict';

angular.module('main.module')
  .controller('header.controller', ['SweetAlert', '$timeout', '$scope', '$rootScope', 'active-cluster.service', 'core-rest.service', '$location',
    function(SweetAlert, $timeout, $scope, $rootScope, activeClusterService, coreService, $location) {

      $rootScope.connected = true;

      $scope.experimentActive = false;

      $scope.setExperimentActive = function() {
        $scope.experimentActive = true;
      };
      $scope.setExperimentInActive = function() {
        $scope.experimentActive = false;
      };

      $scope.hasEc2 = function() {
        return activeClusterService.hasEc2();
      };

      $scope.hasBaremetal = function() {
        return activeClusterService.hasBaremetal();
      };

      $scope.hasProvider = function() {
        return $scope.hasEc2() || $scope.hasBaremetal() ||
          $scope.hasGce() || $scope.hasOpenStack();
      };

      $scope.hasGce = function() {
        return activeClusterService.hasGce();
      };

      $scope.hasOpenStack = function() {
        return activeClusterService.hasOpenStack();
      };

      $scope.switchToTerminal = function() {
        $location.path('/terminal');
      };

      $scope.switchToExperiment = function() {
        $location.path('/experiment');
      };

      $scope.exitKaramel = function() {
        activeClusterService.exitKaramel();
      };

      $scope.sudoPassword = function(password) {
        activeClusterService.sudoPassword(password);
      };

      $scope.githubCredentials = function(email, password) {
        activeClusterService.githubCredentials(email, password);
      };
      
      $scope.exitKaramel = function() {
        SweetAlert.swal({
          title: "Shutdown Karamel engine?",
          text: "The Karamel Engine will shutdown and ongoing deployments will be lost.",
          type: "warning",
          showCancelButton: true,
          confirmButtonColor: "#DD6B55", confirmButtonText: "Yes, exit Karamel!",
          cancelButtonText: "Cancel",
          closeOnConfirm: false,
          closeOnCancel: false},
        function(isConfirm) {
          if (isConfirm) {
            coreService.exitKaramel()
              .success(function(data, status, headers, config) {
                SweetAlert.swal("Shutdown", "Karamel engine has shutdown. Close your browser window.", "info");
              })
              .error(function(data, status, headers, config) {
                SweetAlert.swal("Error", "There was a problem shutting down the Karamel Engine. Maybe it was already shutdown?", "error");
              });

          } else {
            SweetAlert.swal("Cancelled", "Phew, That was close :)", "error");
          }
        });
      };

      function restartTimer() {
        currentTimeout = $timeout(pingServer, 20000);
      }

      function pingServer() {
        coreService.ping()
          .success(function(data, status, headers, config) {
            $rootScope.connected = true;
            restartTimer();
          })
          .error(function(data, status, headers, config) {
            $rootScope.connected = false;
          });
      }

      function _initScope() {
        restartTimer();
      }
      ;

      _initScope();
    }])
'use strict';

angular.module('main.module')
  .controller('header.controller', ['SweetAlert', '$timeout', '$scope', '$rootScope', 'alert.service',
'active-cluster.service', 'core-rest.service', '$location',
    function(SweetAlert, $timeout, $scope, $rootScope, alertService, clusterService, coreService, $location) {

      var currentTimeout;

      $scope.clusterService = clusterService;

      $rootScope.connected = true;

      $scope.experimentActive = false;

      $scope.setExperimentActive = function() {
        $scope.experimentActive = true;
      };
      $scope.setExperimentInActive = function() {
        $scope.experimentActive = false;
      };

      $scope.switchToTerminal = function() {
        $location.path('/terminal');
      };

      $scope.switchToExperiment = function() {
        $location.path('/experiment');
      };

      $scope.saveYamlToDisk = function(){
          
        coreService.jsonToYaml($scope.clusterService.getJsonForRest())
                .success(function(data, status, headers, config) {
                  var blob = new Blob([data.yml], {type: "text/plain;charset=utf-8"});
                  saveAs(blob, clusterService.name().concat(".yml"));
                })
                .error(function(data, status, headers, config) {
                      alertService.addAlert({type: 'danger', msg: 'Could not save the cluster definition.'});
                });
          
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

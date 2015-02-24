/**
 * Created by babbarshaer on 2014-11-25.
 */
// Controller for different providers.

angular.module("demoApp")
    .controller('SshKeysController', ['$log', '$scope', '$modalInstance', 'CaramelCoreServices', function($log, $scope, $modalInstance, CaramelCoreServices) {

        function initKeys(scope) {
          scope.sshKeyPair = {
            pubKeyPath: null,
            priKeyPath: null
          };
          CaramelCoreServices.loadSshKeys()
              .success(function(data) {
                $log.info("ssh data is:" + data.publicKeyPath + "," + data.privateKeyPath);
                scope.sshKeyPair.pubKeyPath = data.publicKeyPath;
                scope.sshKeyPair.priKeyPath = data.privateKeyPath;
              })
              .error(function(data) {
                $log.warn("No SSh keys is available");
              });

          scope.states = {
            valid: "valid",
            invalid: "invalid",
            initial: "initial"
          };

        }

        $scope.close = function() {
          $modalInstance.close();
        };


        $scope.submitKeys = function() {
          if (this.sshKeyForm.$valid) {
            $modalInstance.close();
          }
        };

        $scope.generateKeys = function() {
          CaramelCoreServices.generateSshKeys()
              .success(function(data) {
                $log.info("ssh data is:" + data.publicKeyPath + "," + data.privateKeyPath);
                $scope.sshKeyPair.pubKeyPath = data.publicKeyPath;
                $scope.sshKeyPair.priKeyPath = data.privateKeyPath;
              })
              .error(function(data) {
                $log.warn("Couldn't generate ssh-keys");
              });
        };


        initKeys($scope);
      }]);
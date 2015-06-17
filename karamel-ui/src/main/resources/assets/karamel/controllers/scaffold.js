

angular.module('karamel.main')

  .controller('ScaffoldController', ['$log', '$scope', '$modalInstance', 'KaramelCoreRestServices', function($log, $scope, $modalInstance, KaramelCoreRestServices) {

      function initCb(scope) {
        scope.cbName = "";
        scope.states = {
          valid: "valid",
          invalid: "invalid",
          initial: "initial"
        };

      }

      $scope.close = function() {
        $modalInstance.close();
      };

      $scope.scaffoldFormSubmit = function() {
//          if (this.sshKeyForm.$valid) {
//            $modalInstance.close();
//          }
        KaramelCoreRestServices.scaffoldCookbook($scope.cbName)
          .success(function(data) {
//                scope.sshKeyPair.privKeyPath = data.privateKeyPath;
          })
          .error(function(data) {
            $log.warn("Scaffolding a cookbook failed.");
          });

      };

      initCb($scope);
    }]);
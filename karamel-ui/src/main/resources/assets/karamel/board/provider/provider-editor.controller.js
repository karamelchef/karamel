angular.module('main.module')
    .controller('provider-editor.controller', ['$log', '$scope', '$modalInstance', 'info', function($log, $scope, $modalInstance, info) {

        function _initScope(scope) {
          scope.ec2 = new Ec2();
          scope.baremetal = new Baremetal();
          if (info.baremetal != null) {
            scope.baremetal.copy(info.baremetal);
            if (info.baremetal.ips != null)
              scope.baremetal.ips = info.baremetal.ips.join("\n");
            scope.activeProvider = 'baremetal';
          }
          if (info.ec2 != null) {
            scope.ec2.copy(info.ec2);
            scope.activeProvider = 'ec2';
          }
        }

        $scope.close = function() {
          $modalInstance.close();
        };

        $scope.submit = function() {
          var values = {ec2: null, baremetal: null};
          if ($scope.activeProvider === 'baremetal') {
            var bm = new Baremetal();
            bm.copy($scope.baremetal);
            values.baremetal = bm;
          }
          if ($scope.activeProvider === 'ec2') {
            var ec2 = new Ec2();
            ec2.copy($scope.ec2);
            values.ec2 = ec2;
          }
          $modalInstance.close(values);
        };

        _initScope($scope);

      }]);
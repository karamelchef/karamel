

angular.module('demoApp')

    .controller('ViewClusterController',['$log','$scope','$interval','$modalInstance','KaramelCoreRestServices','info', function($log,$scope,$interval,$modalInstance,KaramelCoreRestServices, info){

        function initScope(scope) {

            // Initialize the instance of cluster.
            scope.cluster = "Fetching the data ..... ";

            //STEP 1: Set the interval value.
            scope.interval = 5000;

            $log.info(info);

            //STEP 2: Create an instance of timeout function.
            scope.intervalInstance = $interval(getClusterStatus, scope.interval);

        }

        // Invoke the Cluster Function.
        function getClusterStatus(){

            // Create internal object.
            var _obj = {
                clusterName : info.cluster.clusterName
            };

            KaramelCoreRestServices.viewCluster(_obj)

                .success(function (data, status, headers, config) {
                    $scope.cluster = angular.toJson(data);
                })

                .error(function(data,status,headers,config){
                    $log.info("Received cluster error.");
                })
        }


        // Call before window close order to prevent memory leaks.
        function destroyIntervalInstance (){

            if (angular.isDefined($scope.intervalInstance)) {
                $interval.cancel($scope.intervalInstance);
                $scope.intervalInstance = undefined;
            }
        }

        $scope.close = function () {

            // Destroy Interval Instance.
            destroyIntervalInstance();

            // Close the instance.
            $modalInstance.close();
        };

        // Initialize the scope.
        initScope($scope);

    }]);
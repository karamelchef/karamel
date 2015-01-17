/**
 * Created by babbarshaer on 2014-11-25.
 */
// Controller for different providers.

angular.module("demoApp")
    .controller('AmazonProviderController', ['$log','$scope','$modalInstance','info','CaramelCoreServices', function ($log, $scope, $modalInstance,info,CaramelCoreServices) {

        function initScope(scope){

            scope.account = {
                accountId: null,
                accountKey: null,
                pubKey: null
            };

            scope.states ={
                valid: "valid",
                invalid: "invalid",
                initial: "initial"
            };


            scope.connectionState = scope.states.initial;

            var container = null;

            if(! (_.isNull(info.group) || _.isUndefined(info.group))){
                container = info.group;
            }
            else{
                container = info.board;
            }

            // In case we have the credentials
            if(!(_.isNull(container.getEC2provider()) || _.isUndefined(container.getEC2provider()) || _.isEmpty(container.getEC2provider()))){

                scope.account = container.getEC2provider();

                validate(scope.account)
                    .success(function(data,status,header,config){
                        $scope.connectionState = $scope.states.valid;
                    })
                    .error(function(data,status,header,config){
                        $scope.connectionState = $scope.states.invalid;
                    })
            }
        }

        $scope.close = function () {
            $modalInstance.close();
        };

        $scope.addOrUpdateProvider = function(){
            if(this.ec2ProviderForm.$valid){
                $modalInstance.close({ec2Provider : $scope.account});
            }
        };


        $scope.testConnection = function(){

            // Reset the Connection State.
            $scope.connectionState = $scope.states.initial;

            // If basic validations passed, then move forward.
            if(this.ec2ProviderForm.accountId.$valid && this.ec2ProviderForm.accountKey.$valid){

                validate($scope.account)
                    .success(function(data,status,header,config){
                        $scope.connectionState = $scope.states.valid;
                    })
                    .error(function(data,status,header,config){
                        $scope.connectionState = $scope.states.invalid;
                    })
            }

        };


        function validate(account){
            return CaramelCoreServices.validateAccount(account);
        }


        initScope($scope);
    }])
    .directive('accountCredentialsValidator',['$log','$scope','CaramelCoreServices',function($log,$scope,CaramelCoreServices){

        // ===== WIP.
        return {
        }


    }]);
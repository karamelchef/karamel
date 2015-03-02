angular.module('demoApp')
    .controller('CredentialsController', ['$log', '$scope' , '$modalInstance', 'info', function ($log, $scope, $modalInstance, info) {

        function _initScope(scope) {
            scope.credentialsHolderMap = {};
            
            scope.mapKeys = {
                ssh : 'ssh',
                provider: 'provider',
                summary : 'summary'
            };

        }

        $scope.close = function () {
            $modalInstance.close();
        };

        _initScope($scope);
    }])
    
    // Credentials Tab Directive.
    .directive('credentialsTab', ['$log', function ($log) {

        return {

            restrict: 'E',
            transclude: true,
            controller: function ($scope) {

                var panes = $scope.panes = [];

                $scope.select = function (pane) {
                    angular.forEach(panes, function (pane) {
                        pane.selected = false;
                    });
                    pane.selected = true;
                };

                this.addPane = function (pane) {
                    if (panes.length == 0) {
                        $scope.select(pane);
                    }
                    panes.push(pane);
                }
            },
            templateUrl: "partials/my-tabs.html"
        }

    }])
    
    // Directive for the Providers.
    .directive('providerPane', ['$log','CaramelCoreServices', function ($log, CaramelCoreServices) {
            
        return{
            restrict: 'E',
            require: "^credentialsTab",
            scope:{
                title: '@',
                mapKey: '@',
                credentialsHolderMap: '='
            },
            link : function(scope,elem,attrs,tabsCtrl){
                
                
                function _initScope(scp){
                    
                    tabsCtrl.addPane(scp);
                    scp.mapKey = 'provider';
                    
                    scp.bootUp = true;
                    scp.account = {
                        accountId: null,
                        accountKey: null
                    };
                    
                    
                    scp.states = {
                        initial: 'initial',
                        valid: 'valid',
                        invalid: 'invalid'
                    };
                    scp.currentState = scp.states.initial;
                }

                scope.$watch('selected', function(){
                    
                    if(scope.selected){
                        
                        $log.info('provider pane selected.');
                        
                        if(scope.bootUp){
                            
                            CaramelCoreServices.loadCredentials()
                                
                                .success(function(data) {
                                    
                                    scope.account.accountId = data.accountId;
                                    scope.account.accountKey = data.accountKey;
                                    scope.currentState = scope.states.valid;
                                    
                                })
                                
                                .error(function(data) {
                                    $log.warn("No Ec2 credentials is available");
                                });
                            scope.bootUp = false;
                            
                        }
                        
                        else{
                            $log.info('Won\'t try now');
                        }
                    }
                    
                });
                
                
                scope.validateCredentials = function() {

                    if (scope.account.accountId != null && scope.account.accountKey != null) {
                        _resetParameters(scope);
                        
                        CaramelCoreServices.validateCredentials(scope.account)
                            
                            .success(function(data) {
                                scope.currentState = scope.states.valid;
                            })
                            
                            .error(function(data) {
                                $log.warn("No Ec2 credentials is available");
                            });
                    }
                    else{
                        $log.info("Seems to work provider pane");
                    }
                };

                function _resetParameters(scp){
                    scp.currentState = scp.states.initial;
                }
                
                _initScope(scope);

            },
            templateUrl: 'partials/provider-pane.html'
            
        }
    }])
    
    // Directive for the ssh key pane.
    .directive('sshKeyPane', ['$log','CaramelCoreServices',  function ($log, CaramelCoreServices) {
        
        return{
            restrict: 'E',
//            transclude: true,
            require: "^credentialsTab",
            scope:{
                title: '@',
                mapKey: '@',
                credentialsHolderMap: '='
            },
            link : function(scope, elem, attrs, tabsCtrl){
                
                function _initScope(scp){
                    
                    tabsCtrl.addPane(scp);
                    scp.bootUp = true;
                    
                    scp.sshKeyPair = {
                        pubKeyPath: null,
                        priKeyPath: null
                    };
                }
                
                scope.$watch('selected', function(){
                    
                    if(scope.selected){
                        
                        $log.info('ssh-key-pane selected');
                        $log.info('Map-Key:' + scope.mapKey);
                        
                        if(scope.bootUp){
                            
                            $log.info("ssh - first time try.");
                            CaramelCoreServices.loadSshKeys()
                                
                                .success(function(data) {
                                    $log.info("ssh data is:" + data.publicKeyPath + "," + data.privateKeyPath);
                                    scope.sshKeyPair.pubKeyPath = data.publicKeyPath;
                                    scope.sshKeyPair.priKeyPath = data.privateKeyPath;
                                })
                                
                                .error(function(data) {
                                    $log.warn("No SSh keys is available");
                                });
                            
                            scope.bootUp = false;
                        }
                        
                        else{
                            $log.info("Won't try now.");
                        }
                    }
                });
                
                
                _initScope(scope);
            },
            templateUrl: 'partials/ssh-pane.html'
        }
    }])

    .directive('summaryPane', ['$log', function($log){

        return{
            restrict: 'E',
            require: "^credentialsTab",
            scope:{
                title: '@',
                mapKey: '@',
                credentialsHolderMap: "="
            },
            link : function(scope, elem, attrs, tabsCtrl){

                function _initScope(scp){
                    tabsCtrl.addPane(scp);
                }

                scope.$watch('selected', function(){
                    if(scope.selected){
                        $log.info('summary pane selected');
                    }
                });


                _initScope(scope);
            },
            templateUrl: 'partials/summary-pane.html'
        }
        
    }]);
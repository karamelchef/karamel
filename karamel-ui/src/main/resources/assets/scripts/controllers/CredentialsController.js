angular.module('demoApp')
    .controller('CredentialsController', ['$log', '$scope' , '$modalInstance', 'info', function ($log, $scope, $modalInstance, info) {

        function _initScope(scope) {
            scope.credentialsHolderMap = {};
            
            scope.mapKeys = {
                ssh : 'ssh',
                provider: 'provider',
                summary : 'summary'
            };
            _setUpHolderMap(scope.credentialsHolderMap);
        }
        
        function _setUpHolderMap(map){
            map[$scope.mapKeys.provider] = info.board.getEC2provider();
            map[$scope.mapKeys.ssh] = info.board.getSshKeyPair();
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
                    scp.provider = scp.credentialsHolderMap[scp.mapKey];
                    
                    if(scp.provider.getIsValid()){
                        scp.currentState = scp.states.valid;
                    }
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

                    scp.availableStates = {
                        success: 'success',
                        failure: 'failure',
                        warn: 'warn'
                    };
                    
                    scp.currentState = scp.availableStates.warn;
                    scp.sshKeyObj = scp.credentialsHolderMap[scp.mapKey];
                }
                
                
                function _updateState(result){
                    
                    if (result === 'success'){
                        scope.sshKeyObj.setIsValid(true);
                        scope.currentState = scope.availableStates.success;
                    }
                    
                    else if (result === 'failure'){
                        scope.sshKeyObj.setIsValid(false);
                        scope.currentState = scope.availableStates.failure;
                    }
                    
                    else if(result === 'warn'){
                        scope.sshKeyObj.setIsValid(false);    
                        scope.currentState = scope.availableStates.warn;
                    }
                    
                }
                
                scope.$watch('selected', function(){
                    
                    if(scope.selected){
                        
                        $log.info('ssh-key-pane selected');
                        
                        if(scope.bootUp){
                            
                            $log.info("ssh - first time try.");
                            CaramelCoreServices.loadSshKeys()
                                
                                .success(function(data) {
                                    $log.info("ssh data is:" + data.publicKeyPath + "," + data.privateKeyPath);
                                    scope.sshKeyPair.pubKeyPath = data.publicKeyPath;
                                    scope.sshKeyPair.priKeyPath = data.privateKeyPath;
                                    _updateState('success');
                                })
                                
                                .error(function(data) {
                                    $log.warn("No default ssh key available");
                                    _updateState('warn');
                                });
                            
                            scope.bootUp = false;
                        }
                        
                        else{
                            $log.info("Won't try now.");
                        }
                    }
                });
                
                
                scope.generateKeys = function(){
                    
                
                    CaramelCoreServices.generateSshKeys()
                        .success(function(data) {
                            
                            $log.info("ssh data is:" + data.publicKeyPath + "," + data.privateKeyPath);
                            scope.sshKeyPair.pubKeyPath = data.publicKeyPath;
                            scope.sshKeyPair.priKeyPath = data.privateKeyPath;
                            _updateState('success');
                            
                            
                    })
                        .error(function(data) {
                            
                            $log.warn("Couldn't generate ssh-keys");
                            _updateState('failure');
                            
                    });
                    
                };
                
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
                allKeys: '=',
                credentialsHolderMap: "="
            },
            link : function(scope, elem, attrs, tabsCtrl){

                function _initScope(scp){
                    
                    tabsCtrl.addPane(scp);
                    scp.summary = {
                        provider : false,
                        sshKey: false
                    }
                }

                function _checkAndUpdateSummary(summary, map){
                    
                    $log.info("Updating summary.");
                    
                    for(var name in scope.allKeys){

                        if(name !== scope.mapKey){
                            
                            var obj = map[name];
                            $log.info(angular.toJson(obj));
                            
                            if(obj !== null){
                                
                                if(obj instanceof Provider){
                                    summary.provider = obj.getIsValid() ? true : false;
                                }

                                else if(obj instanceof SshKeyPair){
                                    summary.sshKey = obj.getIsValid() ? true : false;
                                }
                            }

                        }
                    }
                }
                
                scope.$watch('selected', function(){
                    
                    if(scope.selected){
                        
                        $log.info("Scope is selected and recalculating summary.");
                        _checkAndUpdateSummary(scope.summary, scope.credentialsHolderMap);
                        
                    }
                    
                });

                _initScope(scope);
            },
            templateUrl: 'partials/summary-pane.html'
        }
        
    }]);
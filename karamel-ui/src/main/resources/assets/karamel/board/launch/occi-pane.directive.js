/**
 * Created by Mamut on 2016-01-18.
 */
angular.module('main.module')
    .directive('launchOcciPane', ['$log', 'core-rest.service', function($log, coreService) {
        return{
            restrict: 'E',
            require: "^launchTabs",
            scope: {
                title: '@',
                mapKey: '@',
                credentialsHolderMap: '='
            },
            link: function(scope, elem, attrs, tabsCtrl) {
                function _initScope(scp) {
                    tabsCtrl.addPane(scp);
                    scp.mapKey = 'occi';
                    scp.bootUp = true;
                    scp.account = {
                          userCertificatePath: null,
                          systemCertDir: null
                    };
                    scp.availableStates = {
                        success: 'success',
                        failure: 'failure',
                        initialWarn: 'initialWarn',
                        userWarn: 'userWarn',
                        specialWarn: 'specialWarn'
                    };
                    scp.stateMessage = {
                        success: 'Valid OCCI Provider Details',
                        failure: 'Provider OCCI Details Invalid.',
                        initialWarn: 'No OCCI Provider Details Found.',
                        userWarn: 'Please Re-Validate OCCI details',
                        specialWarn: 'OCCI Credentials must be validated. Click on the \'Validate\' button.'
                    };
                    scp.currentState = scp.availableStates.initialWarn;
                    scp.occi = scp.credentialsHolderMap[scp.mapKey];
                }

                function _updateState(result, credentialObj) {

                    if (result === 'success') {
                        credentialObj.setIsValid(true);
                        scope.currentState = scope.availableStates.success;
                    }

                    else if (result === 'failure') {
                        credentialObj.setIsValid(false);
                        scope.currentState = scope.availableStates.failure;
                    }

                    else if (result === 'initialWarn') {
                        credentialObj.setIsValid(false);
                        scope.currentState = scope.availableStates.initialWarn;
                    }

                    else if (result === 'userWarn') {
                        credentialObj.setIsValid(false);
                        scope.currentState = scope.availableStates.userWarn;
                    }
                    else if (result === 'specialWarn') {
                        credentialObj.setIsValid(false);
                        scope.currentState = scope.availableStates.specialWarn;
                    }
                }

                scope.warnUser = function() {
                    _updateState('userWarn', scope.occi);
                };

                scope.$watch('selected', function() {

                    if (scope.selected) {
                        $log.info('provider pane selected.');
                        
                        if (scope.bootUp) {
                            coreService.loadOcciCredentials()
                                .success(function(data) {
                                    scope.account.userCertificatePath = data.userCertificatePath;
                                    scope.account.systemCertDir = data.systemCertDir;
                                    _updateState('specialWarn', scope.occi);
                                    scope.validateOcciCredentials();
                                })
                                .error(function(data) {
                                    $log.warn("No Occi credentials is available");
                                    _updateState('initialWarn', scope.occi);
                                });
                            scope.bootUp = false;
                        }
                        else {
                            $log.info('Won\'t try now');
                        }
                    }
                });

                scope.validateOcciCredentials = function() {                  

                    if (scope.account.userCertificatePath !== "" && scope.account.systemCertDir !== "") {
                        //Validate and STORE credentials ...
                        coreService.validateOcciCredentials(scope.account)
                            .success(function(data) {
                                _updateState('success', scope.occi);
                            })
                            .error(function(data) {
                                _updateState('failure', scope.occi);
                            });
                    }
                    else {
                        _updateState('failure', scope.occi);
                    }
                };

                if (scope.credentialsHolderMap['occi'])
                    _initScope(scope);

            },
            templateUrl: 'karamel/board/launch/occi-pane.html'
        };
    }]);
/**
 * Created by alberto on 12/6/15.
 */
angular.module('main.module')
    .directive('launchNovaPane', ['$log', 'core-rest.service', function($log, coreService) {
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
                    scp.mapKey = 'nova';
                    scp.bootUp = true;
                    scp.account = {
			version: "v2",
                        accountName: null,
                        accountPass: null,
                        region: null,
                        endpoint: null
                    };
                    scp.availableStates = {
                        success: 'success',
                        failure: 'failure',
                        initialWarn: 'initialWarn',
                        userWarn: 'userWarn',
                        specialWarn: 'specialWarn'
                    };
                    scp.stateMessage = {
                        success: 'Valid Provider Details',
                        failure: 'Provider Details Invalid.',
                        initialWarn: 'No Provider Details Found.',
                        userWarn: 'Please Re-Validate',
                        specialWarn: 'Credentials must be validated. Click on the \'Validate\' button.'
                    };
                    scp.currentState = scp.availableStates.initialWarn;
                    scp.nova = scp.credentialsHolderMap[scp.mapKey];
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
                    _updateState('userWarn', scope.nova);
                };

                scope.$watch('selected', function() {

                    if (scope.selected) {
                        $log.info('provider pane selected.');

                        if (scope.bootUp) {
                            coreService.loadNovaCredentials()
                                .success(function(data) {
                                    scope.account.accountName = data.accountName;
                                    scope.account.accountPass = data.accountPass;
                                    scope.account.version = data.version;
                                    scope.account.region = data.region;
                                    scope.account.endpoint = data.endpoint;
                                    scope.account.networkId = data.networkId;
                                    _updateState('specialWarn', scope.nova);
                                    scope.validateNovaCredentials();
                                })
                                .error(function(data) {
                                    $log.warn("No Openstack nova credentials is available");
                                    _updateState('initialWarn', scope.nova);
                                });
                            scope.bootUp = false;
                        }
                        else {
                            $log.info('Won\'t try now');
                        }
                    }
                });

                scope.validateNovaCredentials = function() {

                    if (scope.account.accountName != null && scope.account.accountPass != null
                    && scope.account.endpoint != null && scope.account.region != null
                    && scope.account.networkId != null) {
                        coreService.validateNovaCredentials(scope.account)
                            .success(function(data) {
                                _updateState('success', scope.nova);
                            })
                            .error(function(data) {
                                _updateState('failure', scope.nova);
                            });
                    }
                    else {
                        _updateState('failure', scope.nova);
                    }
                };

                if (scope.credentialsHolderMap['nova'])
                    _initScope(scope);

            },
            templateUrl: 'karamel/board/launch/nova-pane.html'
        }
    }]);

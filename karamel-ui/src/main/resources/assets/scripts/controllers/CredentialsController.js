angular.module('demoApp')
        .controller('CredentialsController', ['$log', '$scope', '$modalInstance', 'info', function ($log, $scope, $modalInstance, info) {

                function _initScope(scope) {
                    scope.credentialsHolderMap = {};

                    scope.mapKeys = {
                        ssh: 'ssh',
                        provider: 'provider',
                        summary: 'summary'
                    };
                    scope.summaryValid = true;
                    _setUpHolderMap(scope.credentialsHolderMap);
                }

                function _setUpHolderMap(map) {
                    map[$scope.mapKeys.provider] = info.board.getEC2provider();
                    map[$scope.mapKeys.ssh] = info.board.getSshKeyPair();
                }

                $scope.close = function () {
                    $modalInstance.close();
                };

                function _checkSummaryValid(map) {
                    var result = true;
                    for (var name in map) {

                        var obj = map[name];
                        if (obj instanceof Credentials) {
                            $log.info(obj.getIsValid());
                            result = result && obj.getIsValid();
                        }
                    }
                    return result;
                }


                $scope.credentialsFormSubmit = function () {


                    if (_checkSummaryValid($scope.credentialsHolderMap)) {
                        $log.info('All checks passed, capturing details.');
                        $modalInstance.close(info.board);
                    }
                    else {
                        $scope.summaryValid = false;
                    }

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
        .directive('providerPane', ['$log', 'KaramelCoreRestServices', function ($log, KaramelCoreRestServices) {

                return{
                    restrict: 'E',
                    require: "^credentialsTab",
                    scope: {
                        title: '@',
                        mapKey: '@',
                        credentialsHolderMap: '='
                    },
                    link: function (scope, elem, attrs, tabsCtrl) {


                        function _initScope(scp) {

                            tabsCtrl.addPane(scp);
                            scp.mapKey = 'provider';

                            scp.bootUp = true;

                            scp.account = {
                                accessKey: null,
                                secretKey: null
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
                            scp.provider = scp.credentialsHolderMap[scp.mapKey];

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

                        scope.warnUser = function () {
                            _updateState('userWarn', scope.provider);
                        };


                        scope.$watch('selected', function () {

                            if (scope.selected) {

                                $log.info('provider pane selected.');

                                if (scope.bootUp) {

                                    KaramelCoreRestServices.loadCredentials()

                                            .success(function (data) {
                                                scope.account.accessKey = data.accessKey;
                                                scope.account.secretKey = data.secretKey;
                                                _updateState('specialWarn', scope.provider);
                                                validateCredentials();
                                            })

                                            .error(function (data) {
                                                $log.warn("No Ec2 credentials is available");
                                                _updateState('initialWarn', scope.provider);
                                            });

                                    scope.bootUp = false;
                                }

                                else {
                                    $log.info('Won\'t try now');
                                }
                            }

                        });


                        scope.validateCredentials = function () {

                            if (scope.account.accessKey != null && scope.account.secretKey != null) {

                                KaramelCoreRestServices.validateCredentials(scope.account)
                                        .success(function (data) {
                                            _updateState('success', scope.provider);
                                        })

                                        .error(function (data) {
                                            _updateState('failure', scope.provider);
                                        });
                            }
                            else {
                                _updateState('failure', scope.provider);
                            }
                        };

                        _initScope(scope);

                    },
                    templateUrl: 'partials/provider-pane.html'

                }
            }])

        // Directive for the ssh key pane.
        .directive('sshKeyPane', ['$log', 'KaramelCoreRestServices', function ($log, KaramelCoreRestServices) {

                return{
                    restrict: 'E',
//            transclude: true,
                    require: "^credentialsTab",
                    scope: {
                        title: '@',
                        mapKey: '@',
                        credentialsHolderMap: '='
                    },
                    link: function (scope, elem, attrs, tabsCtrl) {

                        function _initScope(scp) {

                            tabsCtrl.addPane(scp);
                            scp.bootUp = true;

                            scp.sshKeyPair = {
                                pubKeyPath: null,
                                privKeyPath: null,
                                passphrase: null
                            };

                            scp.availableStates = {
                                success: 'success',
                                failure: 'failure',
                                initialWarn: 'initialWarn'
                                , userWarn: 'userWarn'
                            };

                            scp.stateMessage = {
                                success: 'Valid SSH Key Pair',
                                failure: 'Unable to generate valid SSH Key Pair',
                                initialWarn: 'Enter SSH Key Pair Paths (and password). Then click Validate.'
                                , userWarn: 'Please generate a new one, instead of manual changing.'
                            };

                            scp.currentState = scp.availableStates.initialWarn;
                            scp.sshKeyObj = scp.credentialsHolderMap[scp.mapKey];
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

                        }

                        scope.warnUser = function () {
                            _updateState('userWarn', scope.sshKeyObj);
                        };

                        scope.usingPassword = function () {
                            _updateState('initialWarn', scope.sshKeyObj);
                        };

                        scope.$watch('selected', function () {

                            if (scope.selected) {

                                $log.info('ssh-key-pane selected');

                                if (scope.bootUp) {

                                    $log.info("ssh - first time try.");
                                    KaramelCoreRestServices.loadSshKeys(scope.sshKeyPair.passphrase)

                                            .success(function (data) {
                                                $log.info("ssh data is:" + data.publicKeyPath + "," + data.privateKeyPath);
                                                scope.sshKeyPair.pubKeyPath = data.publicKeyPath;
                                                scope.sshKeyPair.privKeyPath = data.privateKeyPath;
                                                scope.sshKeyPair.passphrase = data.passphrase;
                                                _updateState('success', scope.sshKeyObj);
                                            })

                                            .error(function (data) {
                                                $log.warn("No default ssh key available");
                                                _updateState('initialWarn', scope.sshKeyObj);
                                            });

                                    scope.bootUp = false;
                                }

                                else {
                                    $log.info("Won't try now.");
                                }
                            }
                        });


                        scope.generateKeys = function () {


                            KaramelCoreRestServices.generateSshKeys()
                                    .success(function (data) {

                                        $log.info("ssh data is:" + data.publicKeyPath + "," + data.privateKeyPath);
                                        scope.sshKeyPair.pubKeyPath = data.publicKeyPath;
                                        scope.sshKeyPair.privKeyPath = data.privateKeyPath;
                                        scope.sshKeyPair.passphrase = data.passphrase;
                                        _updateState('success', scope.sshKeyObj);


                                    })
                                    .error(function (data) {

                                        $log.warn("Couldn't generate ssh-keys");
                                        _updateState('failure', scope.sshKeyObj);

                                    });

                        };

                        scope.registerKeys = function () {
                            KaramelCoreRestServices.registerSshKeys(scope.sshKeyPair)
                                    .success(function (data) {
                                        $log.info("ssh data is:" + data.publicKeyPath + "," + data.privateKeyPath);
                                        scope.sshKeyPair.pubKeyPath = data.publicKeyPath;
                                        scope.sshKeyPair.privKeyPath = data.privateKeyPath;
                                        _updateState('success', scope.sshKeyObj);
                                    })
                                    .error(function (data) {
                                        $log.warn("Couldn't generate ssh-keys");
                                        _updateState('failure', scope.sshKeyObj);

                                    });

                        };



                        _initScope(scope);
                    },
                    templateUrl: 'partials/ssh-pane.html'
                }
            }])

        .directive('summaryPane', ['$log', function ($log) {

                return{
                    restrict: 'E',
                    require: "^credentialsTab",
                    scope: {
                        title: '@',
                        mapKey: '@',
                        allKeys: '=',
                        credentialsHolderMap: "="
                    },
                    link: function (scope, elem, attrs, tabsCtrl) {

                        function _initScope(scp) {

                            tabsCtrl.addPane(scp);
                            scp.summary = {
                                provider: false,
                                sshKey: false
                            }
                        }

                        function _checkAndUpdateSummary(summary, map) {

                            $log.info("Updating summary.");

                            for (var name in scope.allKeys) {

                                if (name !== scope.mapKey) {

                                    var obj = map[name];
                                    $log.info(angular.toJson(obj));

                                    if (obj !== null) {

                                        if (obj instanceof Provider) {
                                            summary.provider = obj.getIsValid() ? true : false;
                                        }

                                        else if (obj instanceof SshKeyPair) {
                                            summary.sshKey = obj.getIsValid() ? true : false;
                                        }
                                    }

                                }
                            }
                        }

                        scope.$watch('selected', function () {

                            if (scope.selected) {

                                $log.info("Scope is selected and recalculating summary.");
                                _checkAndUpdateSummary(scope.summary, scope.credentialsHolderMap);

                            }

                        });

                        _initScope(scope);
                    },
                    templateUrl: 'partials/summary-pane.html'
                }

            }]);
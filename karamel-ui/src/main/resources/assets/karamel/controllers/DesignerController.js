angular.module('demoApp')
        .controller('DesignerController', ['$log', '$scope', '$modalInstance', 'info', function ($log, $scope, $modalInstance, info) {

                function _initScope(scope) {
                    scope.designerHolderMap = {};

                    scope.mapKeys = {
                        ssh: 'ssh',
                        provider: 'provider',
                        summary: 'summary'
                    };
                    scope.summaryValid = true;
                    _setUpHolderMap(scope.designerHolderMap);
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
                        if (obj instanceof Designer) {
                            $log.info(obj.getIsValid());
                            result = result && obj.getIsValid();
                        }
                    }
                    return result;
                }

          $scope.githubCredentials = function (board) {
            $log.info("Set Credentials function called.");

            var modalInstance = $modal.open({
              templateUrl: "partials/designer.html",
              controller: "DesignerController",
              backdrop: "static",
              resolve: {
                info: function() {
                  return {
                    board: angular.copy(board)
                  }
                }
              }
            });

            modalInstance.result.then(function(updatedBoard) {
              if (updatedBoard) {

                board.setEC2Provider(updatedBoard.getEC2provider());
                board.setSshKeyPair(updatedBoard.getSshKeyPair());

                _syncBoardWithCache(updatedBoard);

              }

              else {
                if (!_areCredentialsSet(board)) {
                  AlertService.addAlert({type: 'warning', msg: 'Credentials Invalid.'});
                }

              }

            });
          }

                $scope.designerFormSubmit = function () {


                    if (_checkSummaryValid($scope.designerHolderMap)) {
                        $log.info('All checks passed, capturing details.');
                        $modalInstance.close(info.board);
                    }
                    else {
                        $scope.summaryValid = false;
                    }

                };

                _initScope($scope);
            }])

        // Designer Tab Directive.
        .directive('designerTab', ['$log', function ($log) {

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
                    templateUrl: "partials/github-tabs.html"
                }

            }])

        .directive('experimentView', ['$log', 'KaramelCoreRestServices', function ($log, KaramelCoreRestServices) {

                return{
                    restrict: 'E',
                    require: "^designerTab",
                    scope: {
                        title: '@',
                        mapKey: '@',
                        designerHolderMap: '='
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
                            scp.sshKeyObj = scp.designerHolderMap[scp.mapKey];
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
                    templateUrl: 'partials/designer.html'
                }
            }])            
            
        .directive('sshKeyPane', ['$log', 'KaramelCoreRestServices', function ($log, KaramelCoreRestServices) {

                return{
                    restrict: 'E',
//            transclude: true,
                    require: "^designerTab",
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
                    require: "^designerTab",
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
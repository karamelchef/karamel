angular.module('main.module')
        .directive('launchSshPane', ['$log', 'core-rest.service', 'alert.service',
          function ($log, coreService, alertService) {
            return{
              restrict: 'E',
              require: "^launchTabs",
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
                    passwordNeeded: 'passwordNeeded',
                    initialWarn: 'initialWarn',
                    sudoWarn: 'sudoWarn'
                    , userWarn: 'userWarn'
                  };
                  scp.stateMessage = {
                    success: 'Valid SSH Key Pair',
                    passwordNeeded: 'Enter passphrase for SSH Key Pair',
                    failure: 'Unable to generate valid SSH Key Pair',
                    initialWarn: 'Enter SSH Key Pair Paths (and password). Then click Validate.',
                    sudoWarn: 'Enter the sudo account password. Then click Validate.',
                    userWarn: 'Please generate a new one, instead of manual changing.'
                  };
                  scp.sudoAccount = {
                    password: null
                  };
                  scp.repeatSshPwd = "";
                  scp.currentState = scp.availableStates.initialWarn;
                  scp.sshKeyObj = scp.credentialsHolderMap[scp.mapKey];
                  scp.needsPassword = true;
                  scp.repeatSudoPassword = "";
                  scp.hasEc2 = scp.credentialsHolderMap["ec2"] === undefined ? false : true;
                  scp.hasGce = scp.credentialsHolderMap["gce"] === undefined ? false : true;
                  scp.hasOcci = scp.credentialsHolderMap["occi"] === undefined ? false : true;
                  scp.hasNova = scp.credentialsHolderMap["nova"] === undefined ? false : true;
                  scp.isBaremetal = !(scp.hasEc2 || scp.hasGce || scp.hasOcci || scp.hasNova)
                }

                function _updateState(result, credentialObj) {

                  if (result === 'success') {
                    credentialObj.setIsValid(true);
                    scope.currentState = scope.availableStates.success;
                  } else if (result === 'passwordNeeded') {
                    credentialObj.setIsValid(false);
                    scope.currentState = scope.availableStates.passwordNeeded;
                  } else if (result === 'failure') {
                    credentialObj.setIsValid(false);
                    scope.currentState = scope.availableStates.failure;
                  } else if (result === 'initialWarn') {
                    credentialObj.setIsValid(false);
                    scope.currentState = scope.availableStates.initialWarn;
                  } else if (result === 'userWarn') {
                    credentialObj.setIsValid(false);
                    scope.currentState = scope.availableStates.userWarn;
                  }

                }

                scope.baremetal = function () {
                  return scope.isBaremetal;
                };

                scope.warnUser = function () {
                  _updateState('userWarn', scope.sshKeyObj);
                };

                scope.usingSudoPassword = function () {
                  return sudoAccount === null;
                };

                scope.$watch('selected', function () {

                  if (scope.selected) {
                    $log.info('ssh-key-pane selected');

                    if (scope.bootUp) {
                      $log.info("ssh - first time try.");
                      coreService.loadSshKeys(scope.sshKeyPair.passphrase)
                              .success(function (data) {
                                $log.info("ssh data is:" + data.publicKeyPath + "," + data.privateKeyPath);
                                scope.sshKeyPair.pubKeyPath = data.publicKeyPath;
                                scope.sshKeyPair.privKeyPath = data.privateKeyPath;
                                if (data.passphrase !== null) {
                                  scope.sshKeyPair.passphrase = data.passphrase;
                                  scope.repeatSshPwd = data.passphrase;
                                }
                                scope.needsPassword = data.needsPassword;
                                if (scope.needsPassword === true &&
                                        (scope.sshKeyPair.passphrase === null ||
                                                scope.sshKeyPair.passphrase.length < 2)) {
                                  _updateState('initialWarn', scope.sshKeyObj);
                                } else {
                                  _updateState('success', scope.sshKeyObj);
                                }
                              })
                              .error(function (data) {
                                $log.warn("No default ssh key available");
                                _updateState('initialWarn', scope.sshKeyObj);
                                alertService.addAlert({type: 'danger', msg: data.reason});
                              });
                      scope.bootUp = false;
                    } else {
                      $log.info("Won't try now.");
                    }
                  }
                });


                scope.generateKeys = function () {
                  coreService.generateSshKeys()
                          .success(function (data) {
                            $log.info("ssh data is:" + data.publicKeyPath + "," + data.privateKeyPath);
                            scope.sshKeyPair.pubKeyPath = data.publicKeyPath;
                            scope.sshKeyPair.privKeyPath = data.privateKeyPath;
                            scope.sshKeyPair.passphrase = data.passphrase;
                            scope.repeatSshPwd = data.passphrase;
                            _updateState('success', scope.sshKeyObj);
                          })
                          .error(function (data) {
                            $log.warn("Couldn't generate ssh-keys");
                            _updateState('failure', scope.sshKeyObj);
                            alertService.addAlert({type: 'danger', msg: data.reason});
                          });
                };

                scope.setRepeatSshpassphrase = function (inputTxt) {
                  scope.repeatSshPwd = inputTxt;
                  scope.matchSshKeyPasswords();
                };
                scope.matchSshKeyPasswords = function () {
                  if (scope.needsPassword) {
                    if (scope.sshKeyPair.passphrase === null || scope.sshKeyPair.passphrase === "") {
                      scope.currentState = scope.availableStates.success;
                      return true;
                    }
                    if (scope.repeatSshPwd !== scope.sshKeyPair.passphrase) {
                      scope.currentState = scope.availableStates.passwordNeeded;
                      return false;
                    }
                  }
                  scope.registerKeys();
                  return true;
                };

                scope.registerKeys = function () {
                    coreService.registerSshKeys(scope.sshKeyPair)
                            .success(function (data) {
                              $log.info("ssh data is:" + data.publicKeyPath + "," + data.privateKeyPath);
                              scope.sshKeyPair.pubKeyPath = data.publicKeyPath;
                              scope.sshKeyPair.privKeyPath = data.privateKeyPath;
                              _updateState('success', scope.sshKeyObj);
                            })
                            .error(function (data) {
                              $log.info("Couldn't register ssh-keys yet");
                              _updateState('userWarn', scope.sshKeyObj);
                            });
                };

                scope.sudoChange = function () {
                  if (scope.usingSudoPasswd) {
                    if (!scope.matchSudoPasswords()) {
                      _updateState('sudoWarn', scope.sshKeyObj);
                    } else {
                      _updateState('success', scope.sshKeyObj);
                    }
                  } else {
                    scope.repeatSudoPassword = "";
                    scope.sudoAccount.password = "";
                    scope.registerSudoPassword();
                  }
                };

                scope.setRepeatSudoPassword = function (inputTxt) {
                  scope.repeatSudoPassword = inputTxt;
                  scope.matchSudoPasswords();
                };

                scope.matchSudoPasswords = function () {
                  if (scope.usingSudoPasswd) {
                    if (scope.sudoAccount.password === null || scope.sudoAccount.password === "") {
                      scope.currentState = scope.availableStates.sudoWarn;
                      return false;
                    }
                    if (scope.repeatSudoPassword !== scope.sudoAccount.password) {
                      scope.currentState = scope.availableStates.sudoWarn;
                      return false;
                    }
                  }
                  scope.registerSudoPassword();
                  return true;
                };

                scope.registerSudoPassword = function () {


                  if (scope.repeatSudoPassword !== scope.sudoAccount.password) {
                    return;
                  }

                  coreService.sudoPassword(scope.sudoAccount)
                          .success(function (data) {
                            _updateState('success', scope.sshKeyObj);
                          })
                          .error(function (data) {
                            $log.warn("Couldn't register sudo account password");
                            _updateState('failure', scope.sshKeyObj);
                          });
                };


                _initScope(scope);
              },
              templateUrl: 'karamel/board/launch/ssh-pane.html'
            }
          }]);

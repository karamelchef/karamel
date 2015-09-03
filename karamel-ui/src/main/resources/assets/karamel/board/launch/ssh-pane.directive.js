angular.module('main.module')
  .directive('launchSshPane', ['$log', 'core-rest.service', 'alert.service', function($log, coreService, alertService) {
      return{
        restrict: 'E',
//            transclude: true,
        require: "^launchTabs",
        scope: {
          title: '@',
          mapKey: '@',
          credentialsHolderMap: '='
        },
        link: function(scope, elem, attrs, tabsCtrl) {

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
            scp.sudoAccount = {
              password: null
            };
            scp.sudoPasswordEnabled = false;
            scp.sshPassphraseEnabled = false;
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

          scope.warnUser = function() {
            _updateState('userWarn', scope.sshKeyObj);
          };

          scope.usingPassword = function() {
            _updateState('initialWarn', scope.sshKeyObj);
          };

          scope.usingSudoPassword = function() {
            return sudoAccount === null;
          };

          scope.$watch('selected', function() {

            if (scope.selected) {
              $log.info('ssh-key-pane selected');

              if (scope.bootUp) {
                $log.info("ssh - first time try.");
                coreService.loadSshKeys(scope.sshKeyPair.passphrase)
                  .success(function(data) {
                    $log.info("ssh data is:" + data.publicKeyPath + "," + data.privateKeyPath);
                    scope.sshKeyPair.pubKeyPath = data.publicKeyPath;
                    scope.sshKeyPair.privKeyPath = data.privateKeyPath;
                    scope.sshKeyPair.passphrase = data.passphrase;
                    _updateState('success', scope.sshKeyObj);
                  })
                  .error(function(data) {
                    $log.warn("No default ssh key available");
                    _updateState('initialWarn', scope.sshKeyObj);
                    alertService.addAlert({type: 'danger', msg: data.reason});
                  });
                scope.bootUp = false;
              }
              else {
                $log.info("Won't try now.");
              }
            }
          });


          scope.generateKeys = function() {
            coreService.generateSshKeys()
              .success(function(data) {
                $log.info("ssh data is:" + data.publicKeyPath + "," + data.privateKeyPath);
                scope.sshKeyPair.pubKeyPath = data.publicKeyPath;
                scope.sshKeyPair.privKeyPath = data.privateKeyPath;
                scope.sshKeyPair.passphrase = data.passphrase;
                _updateState('success', scope.sshKeyObj);
              })
              .error(function(data) {
                $log.warn("Couldn't generate ssh-keys");
                _updateState('failure', scope.sshKeyObj);
                alertService.addAlert({type: 'danger', msg: data.reason});
              });
          };

          scope.registerKeys = function() {
            coreService.registerSshKeys(scope.sshKeyPair)
              .success(function(data) {
                $log.info("ssh data is:" + data.publicKeyPath + "," + data.privateKeyPath);
                scope.sshKeyPair.pubKeyPath = data.publicKeyPath;
                scope.sshKeyPair.privKeyPath = data.privateKeyPath;
                _updateState('success', scope.sshKeyObj);
                scope.sshPassphraseEnabled = true;
                v
              })
              .error(function(data) {
                $log.warn("Couldn't generate ssh-keys");
                _updateState('failure', scope.sshKeyObj);
                scope.sshPassphraseEnabled = false;
                alertService.addAlert({type: 'danger', msg: data.reason});
              });
          };

          scope.registerSudoPassword = function() {
            coreService.sudoPassword(scope.sudoAccount)
              .success(function(data) {
                _updateState('success', scope.sshKeyObj);
                scope.sudoPasswordEnabled = true;
              })
              .error(function(data) {
                $log.warn("Couldn't register sudo account password");
                _updateState('failure', scope.sshKeyObj);
                scope.sudoPasswordEnabled = false;
              });
          };


          _initScope(scope);
        },
        templateUrl: 'karamel/board/launch/ssh-pane.html'
      }
    }]);
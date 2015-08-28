angular.module('main.module')
  .directive('launchGcePane', ['$log', 'core-rest.service', function($log, coreService) {
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
            scp.mapKey = 'gce';
            scp.bootUp = true;
            scp.account = {
              jsonKeyPath: null
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
            scp.gce = scp.credentialsHolderMap[scp.mapKey];
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
            _updateState('userWarn', scope.gce);
          };

          scope.$watch('selected', function() {

            if (scope.selected) {
              $log.info('provider pane selected.');

              if (scope.bootUp) {
                coreService.loadGceCredentials()
                  .success(function(data) {
                    scope.account.jsonKeyPath = data.jsonKeyPath;
                    _updateState('specialWarn', scope.gce);
                    scope.validateGceCredentials();
                  })
                  .error(function(data) {
                    $log.warn("No GCE credentials is available");
                    _updateState('initialWarn', scope.gce);
                  });
                scope.bootUp = false;
              }
              else {
                $log.info('Won\'t try now');
              }
            }
          });

          scope.validateGceCredentials = function() {

            if (scope.account.jsonKeyPath !== null) {
              coreService.validateGceCredentials(scope.account)
                .success(function(data) {
                  _updateState('success', scope.gce);
                })
                .error(function(data) {
                  _updateState('failure', scope.gce);
                });
            }
            else {
              _updateState('failure', scope.gce);
            }
          };

          if (scope.credentialsHolderMap['gce'])
            _initScope(scope);

        },
        templateUrl: 'karamel/board/launch/gce-pane.html'
      };
    }]);
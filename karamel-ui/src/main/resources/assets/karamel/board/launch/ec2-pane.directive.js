angular.module('main.module')
  .directive('launchEc2Pane', ['$log', 'core-rest.service', function($log, coreService) {
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
            scp.mapKey = 'ec2';
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
            scp.ec2 = scp.credentialsHolderMap[scp.mapKey];
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
            _updateState('userWarn', scope.ec2);
          };

          scope.$watch('selected', function() {

            if (scope.selected) {
              $log.info('provider pane selected.');

              if (scope.bootUp) {
                coreService.loadEc2Credentials()
                  .success(function(data) {
                    scope.account.accessKey = data.accessKey;
                    scope.account.secretKey = data.secretKey;
                    _updateState('specialWarn', scope.ec2);
                    scope.validateEc2Credentials();
                  })
                  .error(function(data) {
                    $log.warn("No Ec2 credentials is available");
                    _updateState('initialWarn', scope.ec2);
                  });
                scope.bootUp = false;
              }
              else {
                $log.info('Won\'t try now');
              }
            }
          });

          scope.validateEc2Credentials = function() {

            if (scope.account.accessKey != null && scope.account.secretKey != null) {
              coreService.validateEc2Credentials(scope.account)
                .success(function(data) {
                  _updateState('success', scope.ec2);
                })
                .error(function(data) {
                  _updateState('failure', scope.ec2);
                });
            }
            else {
              _updateState('failure', scope.ec2);
            }
          };

          if (scope.credentialsHolderMap['ec2'])
            _initScope(scope);

        },
        templateUrl: 'karamel/board/launch/ec2-pane.html'
      }
    }]);
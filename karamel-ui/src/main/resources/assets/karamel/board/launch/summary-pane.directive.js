angular.module('karamel-main.module')
  .directive('launchSummaryPane', ['$log', function($log) {
      return{
        restrict: 'E',
        require: "^launchTabs",
        scope: {
          title: '@',
          mapKey: '@',
          credentialsHolderMap: "="
        },
        link: function(scope, elem, attrs, tabsCtrl) {

          function _initScope(scp) {
            tabsCtrl.addPane(scp);
            scp.summary = {}
          }

          function _checkAndUpdateSummary(summary, map) {
            $log.info("Updating summary.");
            for (var name in map) {
              if (name !== "summary") {
                var obj = map[name];
                $log.info(angular.toJson(obj));

                if (obj !== null) {
                  summary[obj.getMapKey()] = obj.getIsValid() ? true : false;
                }

              }
            }
          }

          scope.$watch('selected', function() {
            if (scope.selected) {
              $log.info("Scope is selected and recalculating summary.");
              _checkAndUpdateSummary(scope.summary, scope.credentialsHolderMap);
            }
          });

          _initScope(scope);
        },
        templateUrl: 'karamel/board/launch/summary-pane.html'
      }

    }]);
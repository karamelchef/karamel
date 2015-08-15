angular.module('main.module')
    .directive('launchTabs', ['$log', function($log) {
        return {
          restrict: 'E',
          transclude: true,
          controller: function($scope) {
            var panes = $scope.panes = [];
            $scope.select = function(pane) {
              angular.forEach(panes, function(pane) {
                pane.selected = false;
              });
              pane.selected = true;
            };

            this.addPane = function(pane) {
              if (panes.length == 0) {
                $scope.select(pane);
              }
              panes.push(pane);
            }
          },
          templateUrl: "karamel/board/tabs/common-tabs.html"
        }
      }]);
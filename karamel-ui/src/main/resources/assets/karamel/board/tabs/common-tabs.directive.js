/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */
'use strict';
angular.module('main.module')
    .directive('commonTabs', ['$log', function($log) {
        // Help tabs toggle between hide and show.
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




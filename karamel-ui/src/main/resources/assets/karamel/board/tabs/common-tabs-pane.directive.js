/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */
'use strict';
angular.module('karamel-main.module')
    .directive('commonTabsPane', ['$log', 'core-rest.service', 'cookbook-manipulation.service', 
    function($log, coreService, cbService) {

        return{
          restrict: 'E',
          scope: {
            title: "@",
            cookbook: "=",
            cookbooksFilteredData: "=",
            urlInfo: "="
          },
          transclude: true,
          require: "^commonTabs",
          link: function(scope, elem, attrs, tabsCtrl) {

            function initScope(scope) {
              tabsCtrl.addPane(scope);
            }

            // Keep a watch on the selected value.
            scope.$watch('selected', function() {

              if (scope.selected) {

                if (scope.cookbook && scope.cookbook.github) {
                  scope.urlInfo = scope.cookbook.github;
                }

                if (scope.urlInfo) {
                  scope.filteredData = scope.cookbooksFilteredData[scope.urlInfo];
                  if (scope.filteredData == null) {
                    var data = {
                      "url": (scope.urlInfo),
                      "refresh": false
                    };

                    coreService.getCookBookInfo(data)

                        .success(function(data, status, headers, config) {

                          $log.info("Cookbook Details Fetched Successfully.");
                          scope.filteredData = cbService.prepareCookbookMetaData(scope.cookbook, data);
                          scope.cookbooksFilteredData[scope.urlInfo] = scope.filteredData;
                        })
                        .error(function(data, status, headers, config) {
                          $log.info("Cookbook Details can't be Fetched.");
                        });
                  }
                }
              }
            });

            initScope(scope);
          },
          templateUrl: "karamel/board/tabs/common-tabs-pane.html"
        }
      }]);




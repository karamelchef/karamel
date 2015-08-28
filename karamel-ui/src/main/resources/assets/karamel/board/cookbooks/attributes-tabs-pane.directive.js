/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */
'use strict';
angular.module('main.module')
  .directive('attributesTabsPane', ['$log', 'core-rest.service',
    function($log, coreService) {

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

              if (scope.cookbook) {
                scope.urlInfo = scope.cookbook.id;
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
                      var globalCookbook = null;
                      if (scope.cookbook["alias"] == data["name"]) {
                        globalCookbook = scope.cookbook;
                      }
                      // If the attributes section is present.
                      if (globalCookbook != null && globalCookbook["attributes"] != null) {
                        angular.forEach(data["attributes"], function(attribute) {

                          var storedAttributes = globalCookbook["attributes"];

                          // If same property present replace the value from the cookbook.
                          if (storedAttributes[attribute["name"]] != null) {
                            attribute["value"] = storedAttributes[attribute["name"]];
                          }
                          else {
                            attribute["value"] = attribute["default"];
                          }
                        });
                      }
                      scope.cookbooksFilteredData[scope.urlInfo] = data;
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
        templateUrl: "karamel/board/cookbooks/attributes-tabs-pane.html"
      }
    }]);




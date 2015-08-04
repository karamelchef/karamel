/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */


'use strict';

// ========= TRY TO MERGE WITH THE CONFIGURE CHEF CONTROLLER .
angular.module('karamel.main')

  .filter('requiredFilter', ['$log', function($log) {


      return function(attributes, requiredCheck) {


        if (requiredCheck) {
          var requiredAttributes = [];

          angular.forEach(attributes, function(attribute) {
            if (attribute["required"] === "required") {
              requiredAttributes.push(attribute);
            }
          });
          return requiredAttributes;

        }

        else {

          var optionalAttributes = [];
          angular.forEach(attributes, function(attribute) {
            if (attribute["required"] !== "required") {
              optionalAttributes.push(attribute);
            }
          });

          return optionalAttributes;
        }
      }
    }])

  .service('CookbookManipulationService', ['$log', function($log) {


      return {
        prepareCookbookMetaData: function(scopeCookbook, defaultCookbook) {
          var globalCookbook = null;
          if (scopeCookbook["name"] == defaultCookbook["name"]) {
            globalCookbook = scopeCookbook;
          }

          // If the attributes section is present.
          if (globalCookbook != null && globalCookbook["attributes"] != null) {
            angular.forEach(defaultCookbook["attributes"], function(attribute) {

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
          return defaultCookbook;
        },
        persistDataChangesInLocalCookbook: function(storedCookbooks, updatedCookbooksMetaData) {

          angular.forEach(updatedCookbooksMetaData, function(cookbookMetaData) {

            var requiredCookbook = null;

            for (var i = 0; i < storedCookbooks.length; i++) {
              if (cookbookMetaData["name"] === storedCookbooks[i]["name"]) {
                requiredCookbook = storedCookbooks[i];
                break;
              }
            }

            // cookbook found.
            if (requiredCookbook != null) {

              var attributes = cookbookMetaData["attributes"];
              angular.forEach(attributes, function(attribute) {

                // If variable is set and not empty string or full of spaces, then append the property value.
                if (attribute["type"] === 'array' && attribute["value"] != null && attribute["value"].length > 0) {
                  var diff = (attribute["value"].length !== attribute["default"].length);
                  
                  if (attribute["value"].length === attribute["default"].length) {
                    for (i in attribute["value"]) {
                      if (attribute["value"][i] !== attribute["default"][i])
                        diff = true;
                    }
                  }
                  
                  if (diff)
                    requiredCookbook.addPropertyToAttributes(attribute["name"], attribute["value"]);
                } else if (attribute["value"] != null && !!(attribute["value"].replace(/\s/g, '').length) && attribute["default"] !== attribute["value"]) {
                  requiredCookbook.addPropertyToAttributes(attribute["name"], attribute["value"]);
                }
              });
            }
          });
          return storedCookbooks;
        }
      }

    }])
  .controller('CookbookAttributeController', ['$log', '$scope', '$modalInstance', 'info', 'CookbookManipulationService', function($log, $scope, $modalInstance, info, CookbookManipulationService) {

      function initScope(scope) {
        scope.cookbooks = info.cookbooks;
        scope.cookbooksFilteredData = {};
        scope.title = "Configure " + info.title + " Cookbook Attributes";
        scope.optionalCollapsed = true;
        scope.info = {
          url: "default"
        }
      }

      $scope.close = function() {
        $modalInstance.close();
      };

      $scope.toggleCollapse = function() {
        $scope.optionalCollapsed = !$scope.optionalCollapsed;
      };

      $scope.updateAttributes = function() {
        CookbookManipulationService.persistDataChangesInLocalCookbook($scope.cookbooks, $scope.cookbooksFilteredData);
        $modalInstance.close({cookbooks: $scope.cookbooks});
      };

      // Initialize the scope.
      initScope($scope);
    }])

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
        templateUrl: "karamel/partials/common-tabs.html"
      }
    }])

  .directive('commonTabsPane', ['$log', 'KaramelCoreRestServices', 'CookbookManipulationService', function($log, KaramelCoreRestServices, CookbookManipulationService) {

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

                  KaramelCoreRestServices.getCookBookInfo(data)

                    .success(function(data, status, headers, config) {

                      $log.info("Cookbook Details Fetched Successfully.");
                      scope.filteredData = CookbookManipulationService.prepareCookbookMetaData(scope.cookbook, data);
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
        templateUrl: "karamel/partials/common-tabs-pane.html"
      }
    }]);




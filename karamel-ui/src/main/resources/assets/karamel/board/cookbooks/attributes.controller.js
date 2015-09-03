/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */
'use strict';
angular.module('main.module')
  .controller('attributes.controller', ['$scope', '$modalInstance', 'info'
      , function($scope, $modalInstance, info) {

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
          angular.forEach($scope.cookbooksFilteredData, function(cookbookMetaData) {

            var foundCookbook = null;

            for (var i = 0; i < $scope.cookbooks.length; i++) {
              if (cookbookMetaData["name"] === $scope.cookbooks[i]["alias"]) {
                foundCookbook = $scope.cookbooks[i];
                break;
              }
            }

            if (foundCookbook != null) {

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
                    foundCookbook.addPropertyToAttributes(attribute["name"], attribute["value"]);
                } else if (attribute["value"] != null && !!(attribute["value"].replace(/\s/g, '').length) && attribute["default"] !== attribute["value"]) {
                  foundCookbook.addPropertyToAttributes(attribute["name"], attribute["value"]);
                }
              });
            }
          });
          $modalInstance.close({cookbooks: $scope.cookbooks});
        };

        // Initialize the scope.
        initScope($scope);
      }])
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

        } else {
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
  ;




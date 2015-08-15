/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */
'use strict';
angular.module('main.module')
  .controller('attributes-editor.controller', ['$scope', '$modalInstance', 'info', 'cookbook-manipulation.service'
      , function($scope, $modalInstance, info, cbService) {

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
          cbService.persistDataChangesInLocalCookbook($scope.cookbooks, $scope.cookbooksFilteredData);
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




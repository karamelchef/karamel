'use strict';
angular.module('karamel.main')
  .controller('NewRecipeController', ['$scope', '$log', '$modalInstance', 'group', 'KaramelCoreRestServices', 
      function($scope, $log, $modalInstance, group, KaramelCoreRestServices) {

      function initScope(scope) {
        scope.groupName = group.name;
        scope.group = group;
        scope.recipe = null;
        scope.cookbook = null;
        scope.url = null;
      }

      // ========== Based on the url entered fetch the cookbook info.
      $scope.fetchCookbookInfo = function() {

        if (this.addRecipeForm.cookBookUrl.$valid) {
          $log.info("Fetching cookbook information ... ");
          var data = {
            "url": $scope.url,
            "refresh": true                          
          };
          KaramelCoreRestServices.getCookBookInfo(data)
            .success(function(data, status, headers, config) {
              $scope.cookbook = data;
            })
            .error(function(data, status, headers, config) {
              $log.info("Cookbook Details can't be fetched.");
            });
        }
      };

      $scope.addNewRecipe = function() {

        if (this.addRecipeForm.$valid) {
          $log.info(" Add new recipe invoked.");
          $log.info($scope.recipe.name);
          $modalInstance.close({url: $scope.url, cookbook: $scope.cookbook, group: group, recipe: $scope.recipe});
        }
      };
      $scope.close = function() {
        $modalInstance.close();
      };
      initScope($scope);
    }])
  .directive('duplicateRecipe', ['$log', function($log) {
      return{
        require: "ngModel",
        restrict: 'A',
        transclude: true,
        link: function(scope, elem, attrs, ctrl) {

          ctrl.$validators.duplicateRecipe = function(modelValue, viewValue) {

            if (ctrl.$isEmpty(viewValue)) {
              return true;
            }

            var recipe = new Recipe(viewValue.name);
            var cookbooksPresent = scope.group.cookbooks;
            for (var i = 0; i < cookbooksPresent.length; i++) {
              if (cookbooksPresent[i].containsRecipe(recipe)) {
                return false;
              }
            }
            return true;
          }
        }
      }

    }]);


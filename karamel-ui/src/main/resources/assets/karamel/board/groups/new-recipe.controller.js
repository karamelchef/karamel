'use strict';
angular.module('karamel-main.module')
  .controller('new-recipe.controller', ['$scope', '$log', '$modalInstance', 'group', 'KaramelCoreRestServices',
    function($scope, $log, $modalInstance, group, coreService) {

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
          coreService.getCookBookInfo(data)
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
          $modalInstance.close({cookbook: $scope.cookbook, group: group, recipe: $scope.recipe});
        }
      };
      $scope.close = function() {
        $modalInstance.close();
      };
      initScope($scope);
    }]);

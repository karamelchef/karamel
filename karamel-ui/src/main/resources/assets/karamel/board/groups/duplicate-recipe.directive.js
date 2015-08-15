'use strict';
angular.module('karamel-main.module')
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

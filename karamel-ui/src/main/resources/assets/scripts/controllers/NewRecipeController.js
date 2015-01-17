/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */


'use strict';

angular.module('demoApp')
    .controller('NewRecipeController',['$scope','$log','$modalInstance','nodeGroup','CaramelCoreServices', function($scope,$log,$modalInstance,nodeGroup,CaramelCoreServices){

        function initScope(scope){
            scope.groupName = nodeGroup.name;
            scope.nodeGroup = nodeGroup;
            scope.recipe=null;
            scope.cookbook=null;
            scope.url=null;
        }

        // ========== Based on the url entered fetch the cookbook info.
        $scope.fetchCookbookInfo = function(){

            // ========== First check the validity of the url object and then invoke the call. ()
            if(this.addRecipeForm.cookBookUrl.$valid){

                $log.info("Fetching cookbook information ... ");
                var data ={
                    "url": $scope.url,
                    "refresh": true                             // ======= OPTION OF explicit refreshing needs to be allowed.
                };

                CaramelCoreServices.getCookBookInfo(data)

                    .success(function(data,status,headers,config){
                        $scope.cookbook = data;
                    })

                    .error(function(data,status,headers,config){
                        $log.info("Cookbook Details can't be fetched.");
                    });
            }

        };

        // ========== Add a new recipe to the node group.
        $scope.addNewRecipe = function(){

            if(this.addRecipeForm.$valid){
                $log.info(" Add new recipe invoked.");
                $log.info($scope.recipe.name);
                $modalInstance.close({cookbook: $scope.cookbook , nodeGroup: nodeGroup , recipe: $scope.recipe});
            }
        };
        // =========== Close the modal instance
        $scope.close = function(){
            $modalInstance.close();
        };
        initScope($scope);
    }])
    .directive('duplicateRecipe',['$log',function($log){

        return{

            require: "ngModel",
            restrict: 'A',
            transclude: true,
            link: function(scope,elem,attrs,ctrl){

                ctrl.$validators.duplicateRecipe  = function(modelValue, viewValue) {

                    if(ctrl.$isEmpty(viewValue)){
                        return true;
                    }

                    // As the options represent the object, the view value is a Recipe object contained inside cookbook.
                    var recipe = new Recipe(viewValue.name);

                    var cookbooksPresent = scope.nodeGroup.getCookbooks();
                    for(var i=0 ; i< cookbooksPresent.length; i++){
                        if(cookbooksPresent[i].containsRecipe(recipe)){
                            return false;
                        }
                    }

                    return true;
                }
            }
        }

    }]);


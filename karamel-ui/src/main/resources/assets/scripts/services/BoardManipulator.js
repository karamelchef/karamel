/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */


'use strict';

angular.module('demoApp')
    .service('BoardManipulator', ['$log','BoardDataService', function ($log,BoardDataService) {

        return {

            addNodeGroup: function (board, nodeGroupName) {
                board.nodeGroups.push(new NodeGroup(nodeGroupName));
            },

            removeNodeGroup: function (board, nodeGroupName) {
                var id = -1;
                for (var i = 0; i < board.nodeGroups.length; i++) {
                    if (board.nodeGroups[i].name === nodeGroupName) {
                        id = i;
                        break;
                    }
                }

                if (id != -1) {
                    board.nodeGroups.splice(id, 1);
                }


            },

            addRecipeToNodeGroup: function (board, nodeGroup, recipe) {
                angular.forEach(board.nodeGroups, function (currNodeGroup) {
                    if (currNodeGroup.name === nodeGroup.name) {
                        currNodeGroup.addRecipe(recipe);
                    }
                });
            },

            removeRecipeFromNodeGroup: function (nodeGroup,cookbook,recipe) {

                $log.info(" Board Manipulator Service, Remove Recipe Invoked.");

                var cookbooks = nodeGroup.getCookbooks();
                var requiredCookbook = null;
                for(var i =0; i < cookbooks.length; i++){
                    if(cookbook.equals(cookbooks[i])){
                        requiredCookbook = cookbooks[i];
                    }
                }

                if(requiredCookbook != null){
                    requiredCookbook.removeRecipe(recipe);
                }
            },

            addCookbook: function(board, cookbook){
                // check if the cookbook if already present and then maybe replace it.
                board.addCookbook(cookbook);
            }
        }

    }]);
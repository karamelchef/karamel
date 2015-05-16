/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */

'use strict';

angular.module('demoApp')

    .service('BoardService', ['BoardDataService', 'SweetAlert', '$log', '$modal', '$location', 
'BoardManipulator', '$rootScope', '$window', 'KaramelCoreRestServices', 'KaramelSyncService', 
'AlertService', 
function(BoardDataService, SweetAlert, $log, $modal, $location, BoardManipulator, $rootScope, $window, 
KaramelCoreRestServices, KaramelSyncService, AlertService) {

        // ============  Private Functions
        var _createAndPopulateBoard = function(boardJSON) {
          var karamelBoard = new Cluster();
          karamelBoard.load(boardJSON);
          return karamelBoard;
        };


        function _syncBoardWithCache(board) {
          KaramelSyncService.syncWithCache(board);
        }


        function _normalizeUrl(originalUrl, pattern, replaceStr) {
          // Remove the pattern from the url.

          if (originalUrl.indexOf(pattern) != -1) {
            originalUrl = originalUrl.replace(pattern, replaceStr);
          }

          return originalUrl;
        }


        function _launchCluster(board) {

          var restObj = getRestObjBuilder().buildKaramelForRest(board);
          var data = {
            json: angular.toJson(restObj)
          };
          KaramelCoreRestServices.startCluster(data)
              .success(function(data, status, headers, config) {
                $log.info("Connection Successful.");
                AlertService.addAlert({type: 'success', msg: 'Cluster Launch Successful.'});
                $location.path('/terminal');
              })
              .error(function(data, status, headers, config) {
                $log.info("Error Received.");
                $log.info(data.message);
                AlertService.addAlert({type: 'warning', msg: data.message || 'Unable to launch service'});
              });
        }


        function _areCredentialsSet(board) {

          var result = true;

          result = result && board.getEC2provider().getIsValid();
          result = result && board.getSshKeyPair().getIsValid();

          return result;
        }

        // --------------------------------------------------------------------------------------------------------------------

        return {
          addNewRecipe: function(board, nodeGroup) {

            var modalInstance = $modal.open({
              templateUrl: 'partials/newRecipe.html',
              controller: 'NewRecipeController',
              backdrop: 'static',
              resolve: {
                nodeGroup: function() {
                  return nodeGroup;
                }
              }
            });

            modalInstance.result.then(function(info) {

              if (info) {
                $log.info("Adding Recipe with name:" + info.recipe.name);


                //Normalize the cookbook object url.
                info.cookbook.github = _normalizeUrl(info.cookbook.url, "https://github.com/", "");

                var tempCookbook = {
                  name: info.cookbook.name,
                  github: info.cookbook.github
                };

                // ====== check addition of cookbook at the board level.
                if (board.containsCookbook(tempCookbook) == null) {

                  // Board level cookbook object.
                  var cookbook_board = new Cookbook();
                  cookbook_board.load(info.cookbook);

                  board.addCookbook(cookbook_board);
                }

                // ====== Check addition of cookbook at the node group level.
                var localCookbook = nodeGroup.containsCookbook(tempCookbook);
                if (localCookbook == null) {

                  localCookbook = new Cookbook();
                  localCookbook.load(info.cookbook);
                  nodeGroup.addCookbook(localCookbook);

                }

                $log.info(board);
                // Add Recipe to the cookbook,now
                localCookbook.addRecipe(new Recipe(info.recipe.name));

                _syncBoardWithCache(board);
              }
            });


          },
          // Remove the recipe.
          removeRecipe: function(board, nodeGroup, cookbook, recipe) {

            SweetAlert.swal({
              title: "Are you sure?",
              text: "The Recipe will be deleted from the Node Group.",
              type: "warning",
              showCancelButton: true,
              confirmButtonColor: "#DD6B55", confirmButtonText: "Yes, delete it!",
              cancelButtonText: "Cancel",
              closeOnConfirm: false,
              closeOnCancel: false},
            function(isConfirm) {
              if (isConfirm) {
                SweetAlert.swal("Deleted!", "Recipe Nuked. \\\m/", "success");
                $log.info("Remove Recipe Confirmed.");
                BoardManipulator.removeRecipeFromNodeGroup(nodeGroup, cookbook, recipe);

                _syncBoardWithCache(board);
              } else {
                SweetAlert.swal("Cancelled", "Recipe Lives :)", "error");
              }
            });


          },
          // Remove the node group.
          removeNodeGroup: function(board, nodeGroup) {

            SweetAlert.swal({
              title: "Are you sure?",
              text: "The Node Group will be permanently deleted.",
              type: "warning",
              showCancelButton: true,
              confirmButtonColor: "#DD6B55", confirmButtonText: "Yes, delete it!",
              cancelButtonText: "Cancel",
              closeOnConfirm: false,
              closeOnCancel: false},
            function(isConfirm) {
              if (isConfirm) {
                SweetAlert.swal("Deleted!", "Node Group Deleted.", "success");
                BoardManipulator.removeNodeGroup(board, nodeGroup.name);    // FIX ME: Remove the BoardManipulator Functionality as duplicate functionality.
                _syncBoardWithCache(board);
              } else {
                SweetAlert.swal("Cancelled", "Phew, That was close :)", "error");
              }
            });
          },
          exitKaramel: function() {

            SweetAlert.swal({
              title: "Are you sure?",
              text: "Karamel will exit and ongoing deployments will be lost.",
              type: "warning",
              showCancelButton: true,
              confirmButtonColor: "#DD6B55", confirmButtonText: "Yes, exit Karamel!",
              cancelButtonText: "Cancel",
              closeOnConfirm: false,
              closeOnCancel: false},
            function(isConfirm) {
              if (isConfirm) {
                   KaramelCoreRestServices.exitKaramel()
                .success(function(data, status, headers, config) {
                    $window.close();
                 })
                .error(function(data, status, headers, config) {
                  $log.info("Error Received.");
                });                     
                
              } else {
                SweetAlert.swal("Cancelled", "Phew, That was close :)", "error");
              }
            });
          },
          // Create the karamel board.
          karamelBoard: function(board) {
            var karamelBoard = new Cluster(board.name);

            angular.forEach(board.nodeGroups, function(nodeGroup) {
              BoardManipulator.addNodeGroup(karamelBoard, nodeGroup.name);

              angular.forEach(nodeGroup.recipes, function(recipe) {
                BoardManipulator.addRecipeToNodeGroup(karamelBoard, nodeGroup, new Recipe(recipe.title, 'status', recipe.details));
              });

            });

            return karamelBoard;
          },
          // Edit the cluster configuration.
          editClusterConfiguration: function(board) {

            var modalInstance = $modal.open({
              templateUrl: "partials/editYaml.html",
              controller: "yamlEditorController",
              backdrop: 'static'
            });

            var self = this;

            modalInstance.result.then(function(updatedClusterJson) {
//                    if (updatedClusterJson) {
//
//                        board = self.createKaramelBoard(updatedClusterJson);
//                        $log.info("Cluster Information Updated Successfully.");
//
//                        _syncBoardWithCache(board);
//                    }
            });
          },
          //Add a new node group.
          addNewNodeGroup: function(board) {

            var modalInstance = $modal.open({
              templateUrl: "partials/nodeGroup.html",
              controller: "NodeGroupConfigurationController",
              backdrop: 'static',
              resolve: {
                // No current node group information.
                nodeGroupInfo: function() {
                  return null;
                }
              }
            });

            modalInstance.result.then(function(newNodeGroupInfo) {
              if (newNodeGroupInfo) {

                var nodeGroup = new NodeGroup();
                nodeGroup.load(newNodeGroupInfo);
                // new Node group information received.
                board.addNodeGroup(nodeGroup);

                _syncBoardWithCache(board);
              }
            });
          },
          updateNodeGroupInfo: function(board, existingNodeGroupInfo) {

            var modalInstance = $modal.open({
              templateUrl: "partials/nodeGroup.html",
              controller: "NodeGroupConfigurationController",
              backdrop: 'static',
              resolve: {
                nodeGroupInfo: function() {
                  return existingNodeGroupInfo;
                }
              }
            });

            // Update the existing information of the node group.
            modalInstance.result.then(function(updatedNodeGroupInfo) {
              if (updatedNodeGroupInfo) {

                var id = -1;
                for (var i = 0; i < board.nodeGroups.length; i++) {
                  if (board.nodeGroups[i].name === existingNodeGroupInfo.name) {
                    id = i;
                  }
                }

                if (id != -1) {
                  board.nodeGroups[id].name = updatedNodeGroupInfo.name;
                  board.nodeGroups[id].instances = updatedNodeGroupInfo.size;
                }

                _syncBoardWithCache(board);
              }
            });
          },
          // =========== Part of Fix: Model Overhaul.
          createKaramelBoard: function(boardJSON) {
            return _createAndPopulateBoard(boardJSON);
          },
          copyKaramelBoard: function(boardJSON) {
            var board = new Cluster();
            board.copy(boardJSON);

            return board;
          },
          // ========== Allow users to configure the global attributes.
          configureGlobalAttributes: function(board) {

            var modalInstance = $modal.open({
              templateUrl: "partials/cookbookAttributesConfigure.html",
              controller: "CookbookAttributeController",
              backdrop: "static",
              resolve: {
                info: function() {
                  return {
                    title: "Global",
                    cookbooks: angular.copy(board.getCookbooks())
                  }
                }
              }
            });

            modalInstance.result.then(function(result) {
              if (result) {
                // Update the board with the cookbook object.
                board.setCookbooks(result.cookbooks);
                _syncBoardWithCache(board);
              }
            });

          },
          // ========= Allow users to configure the node group level attributes.
          configureGroupLevelCookbookAttributes: function(board, nodeGroup) {

            var modalInstance = $modal.open({
              templateUrl: "partials/cookbookAttributesConfigure.html",
              controller: "CookbookAttributeController",
              backdrop: "static",
              resolve: {
                info: function() {
                  return {
                    title: nodeGroup.name,
                    cookbooks: angular.copy(nodeGroup.getCookbooks())
                  }
                }
              }
            });

            modalInstance.result.then(function(result) {
              if (result) {
                //Update the node group on the board with the updated cookbooks.
                nodeGroup.setCookbooks(result.cookbooks);

                _syncBoardWithCache(board);
              }
            });

          },
          saveYaml: function(board) {

            $log.info("Invoked Function to save yml to disk.");
            // Populate the rest object that needs to be sent to the REST Service to get back the yaml.
            var restObj = null;

            if (board == null) {
              $log.info("No Board Object Present.");
              AlertService.addAlert({type: 'warning', msg: 'No Karamel Model Found.'});
              return;
            }
            else {
              restObj = getRestObjBuilder().buildKaramelForRest(board);
            }

            var data = {
              json: angular.toJson(restObj)
            };
            KaramelCoreRestServices.getCompleteYaml(data)
                .success(function(data, status, headers, config) {
                  var blob = new Blob([data.yml], {type: "text/plain;charset=utf-8"});
                  saveAs(blob, board["name"].concat(".yml"));
                })
                .error(function(data, status, headers, config) {
                  $log.info("Error Received.");
                });

          },
          editAmazonProvider: function(board, group, isLaunch) {

            $log.info("Edit Amazon Provider.");

            var modalInstance = $modal.open({
              templateUrl: "partials/amazonProvider.html",
              controller: "AmazonProviderController",
              backdrop: "static",
              resolve: {
                info: function() {
                  return {
                    board: angular.copy(board),
                    group: angular.copy(group)
                  }
                }
              }
            });

            var externalObj = this;
            modalInstance.result.then(function(provider) {

              if (provider) {

                var ec2Provider = board.getEC2provider() != null ? board.getEC2provider() : new EC2Provider();
                ec2Provider.addAccountDetails(provider.ec2Provider);


                if (_.isNull(group)) {
                  $log.info("Save the provider details at global level.");
                  board.setEC2Provider(ec2Provider);
                }

                else {
                  $log.info("Save the provider details at group level.");
                  group.setEC2Provider(ec2Provider);
                }

                _syncBoardWithCache(board);

                if (isLaunch) {
                  // If call came as part of start cluster, launch cluster.
                  externalObj.startCluster(board);
                }

              }
              else {
                // If provider is still not set.
                if (board.getEC2provider() == null) {
                  $log.info("Ec2 provider details still not set.");
                  AlertService.addAlert({type: 'danger', msg: 'Provider details not set.'});
                }
              }
            });

          },
          startCluster: function(board) {

            $log.info("Invoked Function to start cluster.");
            // Populate the rest object that needs to be sent to the REST Service to get back the yaml.

            if (board == null) {
              $log.info("No Board Object Present.");
              AlertService.addAlert({type: 'warning', msg: 'No Karamel Model Found.'});
              return;
            }

            if (!(_areCredentialsSet(board))) {
              this.setCredentials(board, true);
            }

            else {
              _launchCluster(board);
            }

          },
          viewCluster: function(board) {

            $log.info("View cluster function invoked.");

            var modalInstance = $modal.open({
              templateUrl: "partials/viewCluster.html",
              controller: "ViewClusterController",
              backdrop: "static",
              resolve: {
                info: function() {
                  return {
                    cluster: {
                      clusterName: board.name
                    }
                  }
                }
              }
            });

          },
          scaffoldCookbook: function(board) {

            $log.info("Scaffold Cookbook function invoked.");
            var modalInstance = $modal.open({
              templateUrl: "partials/scaffold.html",
              controller: "ScaffoldController",
              backdrop: "static",
              resolve: {
                info: function() {
                  return {
                    cluster: {
                      clusterName: board.name
                    }
                  }
                }
              }
            });
            var restObj = getRestObjBuilder().buildKaramelForRest(board);
            var data = {
              json: angular.toJson(restObj)
            };
            KaramelCoreServices.scaffoldCookbook(data)
                .success(function(data, status, headers, config) {
                  $log.info("Connection Successful.");
                  AlertService.addAlert({type: 'success', msg: 'Cookbook created successfully.'});
                })
                .error(function(data, status, headers, config) {
                  $log.info("Error Received.");
                  $log.info(data.message);
                  AlertService.addAlert({type: 'warning', msg: data.message || 'Unable to create cookbook'});
                });


          },
          setCredentials: function(board, isLaunch) {
            $log.info("Set Credentials function called.");

            var modalInstance = $modal.open({
              templateUrl: "partials/credentials.html",
              controller: "CredentialsController",
              backdrop: "static",
              resolve: {
                info: function() {
                  return {
                    board: angular.copy(board)
                  }
                }
              }
            });

            modalInstance.result.then(function(updatedBoard) {
              if (updatedBoard) {

                board.setEC2Provider(updatedBoard.getEC2provider());
                board.setSshKeyPair(updatedBoard.getSshKeyPair());

                _syncBoardWithCache(updatedBoard);

                if (isLaunch) {
                  _launchCluster(board);
                }
              }

              else {
                if (!_areCredentialsSet(board)) {
                  AlertService.addAlert({type: 'warning', msg: 'Credentials Invalid.'});
                }

              }

            });
          }
        }

      }]);

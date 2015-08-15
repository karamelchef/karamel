/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */

'use strict';

angular.module('main.module')
    .service('board.service', ['SweetAlert', '$log', '$modal', '$location', '$rootScope', 'core-rest.service', 
  'browser-cache.service', 'alert.service',
      function(SweetAlert, $log, $modal, $location, $rootScope, coreService, 
      cacheService, alertService) {

        var self = this;
        self.experimentActive = false;

        function _normalizeUrl(originalUrl, pattern, replaceStr) {

          if (originalUrl.indexOf(pattern) !== -1) {
            originalUrl = originalUrl.replace(pattern, replaceStr);
          }
          return originalUrl;
        }

        function _isNullCluster() {
          if ($rootScope.activeCluster === angular.undefined) {
            return true;
          }
          if ($rootScope.activeCluster === null) {
            return true;
          }
          return false;
        }


        function _launchCluster() {
          var coreFormatCluster = $rootScope.activeCluster.toCoreApiFormat();
          var data = {
            json: angular.toJson(coreFormatCluster)
          };
          coreService.startCluster(data)
              .success(function(data, status, headers, config) {
                $log.info("Connection Successful.");
                alertService.addAlert({type: 'success', msg: 'Cluster Launch Successful.'});
                $location.path('/terminal');
              })
              .error(function(data, status, headers, config) {
                $log.info("Error Received.");
                $log.info(data.message);
                alertService.addAlert({type: 'warning', msg: data.message || 'Unable to launch service'});
              });
        }

        return {
          isExperimentActive: function() {
            return self.experimentActive;
          },
          setExperimentActive: function() {
            self.experimentActive = true;
          },
          setExperimentInActive: function() {
            self.experimentActive = false;
          },
          addNewRecipe: function(group) {

            var modalInstance = $modal.open({
              templateUrl: 'karamel/board/groups/new-recipe.html',
              controller: 'new-recipe.controller as ctrl',
              backdrop: 'static',
              resolve: {
                group: function() {
                  return group;
                }
              }
            });

            modalInstance.result.then(function(info) {
              var cluster = $rootScope.activeCluster;
              if (info) {
                $log.info("Adding Recipe with name:" + info.recipe.name);

                var tempCookbook = {
                  id: info.cookbook.id
                };

                if (cluster.containsCookbook(tempCookbook) === null) {
                  var cookbook_cluster = new Cookbook();
                  cookbook_cluster.load(info.cookbook);
                  cluster.addCookbook(cookbook_cluster);
                }

                var localCookbook = group.containsCookbook(tempCookbook);

                if (localCookbook === null) {
                  localCookbook = new Cookbook();
                  localCookbook.load(info.cookbook);
                  group.addCookbook(localCookbook);
                }

                $log.info(cluster);
                localCookbook.addRecipe(new Recipe(info.recipe.name));
                cacheService.updateCache();
              }
            });
          },
          removeRecipe: function(group, cookbook, recipe) {
            SweetAlert.swal({
              title: "Remove this recipe?",
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
                var cookbooks = group.cookbooks;
                var requiredCookbook = null;

                for (var i = 0; i < cookbooks.length; i++) {
                  if (cookbook.equals(cookbooks[i])) {
                    requiredCookbook = cookbooks[i];
                  }
                }

                if (requiredCookbook !== null) {
                  requiredCookbook.removeRecipe(recipe);
                }

                cacheService.updateCache();
              } else {
                SweetAlert.swal("Cancelled", "Recipe Lives :)", "error");
              }
            });
          },
          removeGroup: function(group) {
            SweetAlert.swal({
              title: "Remove this group?",
              text: "The Node Group will be permanently deleted.",
              type: "warning",
              showCancelButton: true,
              confirmButtonColor: "#DD6B55", confirmButtonText: "Yes, delete it!",
              cancelButtonText: "Cancel",
              closeOnConfirm: false,
              closeOnCancel: false},
            function(isConfirm) {
              var cluster = $rootScope.activeCluster;

              if (isConfirm) {
                SweetAlert.swal("Deleted!", "Node Group Deleted.", "success");
                var id = -1;

                for (var i = 0; i < cluster.groups.length; i++) {
                  if (cluster.groups[i].name === group.name) {
                    id = i;
                    break;
                  }
                }

                if (id !== -1) {
                  cluster.groups.splice(id, 1);
                }

                cacheService.updateCache();
              } else {
                SweetAlert.swal("Cancelled", "Phew, That was close :)", "error");
              }
            });
          },
          exitKaramel: function() {
            SweetAlert.swal({
              title: "Shutdown Karamel engine?",
              text: "The Karamel Engine will shutdown and ongoing deployments will be lost.",
              type: "warning",
              showCancelButton: true,
              confirmButtonColor: "#DD6B55", confirmButtonText: "Yes, exit Karamel!",
              cancelButtonText: "Cancel",
              closeOnConfirm: false,
              closeOnCancel: false},
            function(isConfirm) {
              if (isConfirm) {
                coreService.exitKaramel()
                    .success(function(data, status, headers, config) {
                      SweetAlert.swal("Shutdown", "Karamel engine has shutdown. Close your browser window.", "info");
                    })
                    .error(function(data, status, headers, config) {
                      SweetAlert.swal("Error", "There was a problem shutting down the Karamel Engine. Maybe it was already shutdown?", "error");
                    });

              } else {
                SweetAlert.swal("Cancelled", "Phew, That was close :)", "error");
              }
            });
          },
          sudoPassword: function(password) {

            coreService.sudoPassword(password)
                .success(function(data, status, headers, config) {
                  $log.info("Sudo password updated.");
                })
                .error(function(data, status, headers, config) {
                  $log.info("Error Received.");
                });

          },
          addGroup: function() {
            var modalInstance = $modal.open({
              templateUrl: "karamel/board/groups/group-editor.html",
              controller: "group-editor.controller as ctrl",
              backdrop: 'static',
              resolve: {
                groupInfo: function() {
                  return null;
                }
              }
            });

            modalInstance.result.then(function(newGroupInfo) {
              if (newGroupInfo) {
                var group = new Group();
                group.load(newGroupInfo);
                $rootScope.activeCluster.addGroup(group);
                cacheService.updateCache();
              }
            });
          },
          updateGroupInfo: function(existingGroupInfo) {
            var modalInstance = $modal.open({
              templateUrl: "karamel/board/groups/group-editor.html",
              controller: "group-editor.controller as ctrl",
              backdrop: 'static',
              resolve: {
                groupInfo: function() {
                  return existingGroupInfo;
                }
              }
            });

            modalInstance.result.then(function(updatedGroupInfo) {
              if (updatedGroupInfo) {
                var cluster = $rootScope.activeCluster;
                var id = -1;
                for (var i = 0; i < cluster.groups.length; i++) {
                  if (cluster.groups[i].name === existingGroupInfo.name) {
                    id = i;
                  }
                }

                if (id !== -1) {
                  cluster.groups[id].name = updatedGroupInfo.name;
                  cluster.groups[id].size = updatedGroupInfo.size;
                }

                cacheService.updateCache();
              }
            });
          },
          configureGlobalAttributes: function() {
            var modalInstance = $modal.open({
              templateUrl: "karamel/board/cookbooks/attributes.editor.html",
              controller: "attributes-editor.controller as ctrl",
              backdrop: "static",
              resolve: {
                info: function() {
                  return {
                    title: "Global",
                    cookbooks: angular.copy($rootScope.activeCluster.cookbooks)
                  }
                }
              }
            });

            modalInstance.result.then(function(result) {
              if (result) {
                $rootScope.activeCluster.cookbooks = result.cookbooks;
                cacheService.updateCache();
              }
            });
          },
          configureGlobalProvider: function() {
            var modalInstance = $modal.open({
              templateUrl: "karamel/partials/provider-editor.html",
              controller: "provider-editor.controller as ctrl",
              backdrop: "static",
              resolve: {
                info: function() {
                  return {
                    ec2: $rootScope.activeCluster.ec2,
                    baremetal: $rootScope.activeCluster.baremetal
                  }
                }
              }
            });

            modalInstance.result.then(function(result) {
              if (result) {
                $rootScope.activeCluster.ec2 = result.ec2;
                $rootScope.activeCluster.baremetal = result.baremetal;
                cacheService.updateCache();
              }
            });

          },
          configureGroupProvider: function(group) {
            var modalInstance = $modal.open({
              templateUrl: "karamel/partials/provider-editor.html",
              controller: "provider-editor.controller as ctrl",
              backdrop: "static",
              resolve: {
                info: function() {
                  return {
                    ec2: group.ec2,
                    baremetal: group.baremetal
                  }
                }
              }
            });
            modalInstance.result.then(function(result) {
              if (result) {
                group.ec2 = result.ec2;
                group.baremetal = result.baremetal;
                cacheService.updateCache();
              }
            });
          },
          configureGroupAttributes: function(group) {
            var modalInstance = $modal.open({
              templateUrl: "karamel/partials/editor-attributes.html",
              controller: "attributes-editor.controller as ctrl",
              backdrop: "static",
              resolve: {
                info: function() {
                  return {
                    title: group.name,
                    cookbooks: angular.copy(group.cookbooks)
                  }
                }
              }
            });
            modalInstance.result.then(function(result) {
              if (result) {
                group.cookbooks = result.cookbooks;
                cacheService.updateCache();
              }
            });
          },
          hasEc2: function() {
            if (_isNullCluster()) {
              return false;
            }
            return $rootScope.activeCluster.hasEc2();
          },
          hasBaremetal: function() {
            if (_isNullCluster()) {
              return false;
            }
            return $rootScope.activeCluster.hasBaremetal();
          },
          hasGce: function() {
            if (_isNullCluster()) {
              return false;
            }
            return $rootScope.activeCluster.hasGce();
          },
          hasOpenStack: function() {
            if (_isNullCluster()) {
              return false;
            }
            return $rootScope.activeCluster.hasOpenStack();
          },
          launchCluster: function() {
            var cluster = $rootScope.activeCluster;
            if (cluster === null) {
              $log.info("No Active Cluster Object Present.");
              alertService.addAlert({type: 'warning', msg: 'No Active Cluster Found.'});
              return;
            }
            if (!$rootScope.activeCluster.areCredentialsSet()) {
              this.setCredentials(true);
            }
            else {
              _launchCluster();
            }
          },
          setCredentials: function(isLaunch) {
            var modalInstance = $modal.open({
              templateUrl: "karamel/board/launch/launch.html",
              controller: "launch.controller as ctrl",
              backdrop: "static",
              resolve: {
                info: function() {
                  return {
                    cluster: angular.copy($rootScope.activeCluster)
                  }
                }
              }
            });
            modalInstance.result.then(function(updatedCluster) {
              if (updatedCluster) {
                $rootScope.activeCluster.ec2 = updatedCluster.ec2;
                $rootScope.activeCluster.sshKeyPair = updatedCluster.sshKeyPair;
                cacheService.updateCache();

                if (isLaunch) {
                  _launchCluster();
                }
              } else if (!$rootScope.activeCluster.areCredentialsSet()) {
                alertService.addAlert({type: 'warning', msg: 'Credentials Invalid.'});
              }
            });
          }
        }

      }]);


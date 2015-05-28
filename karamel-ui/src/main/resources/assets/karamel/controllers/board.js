/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */

'use strict';

angular.module('karamel.main')
  .controller('BoardController', ['$rootScope', '$log', '$scope', 'BoardService', '$window', 'AlertService',
    '$location', function($rootScope, $log, $scope, BoardService, $window, AlertService, $location) {

      function initScope() {
        if ($window['sessionStorage'] !== undefined) {
          var clusterObj = $window.sessionStorage.getItem('karamel');
          clusterObj = angular.fromJson(clusterObj);

          if (clusterObj !== null) {
            try {
              var cluster = new Cluster();
              cluster.copy(clusterObj);
              $rootScope.activeCluster = cluster;
              AlertService.addAlert({type: 'success', msg: 'Model Loaded Successfully.'});
            }
            catch (err) {
              $log.error(err);
              AlertService.addAlert({type: 'danger', msg: 'Unable to parse the json to generate model.'});
            }
          }
          else {
            AlertService.addAlert({type: 'info', msg: 'Couldn\'t find any model in cache.'});
          }
        }
        else {
          $log.error("No Support for session storage.");
        }
      }

      $scope.configureGlobalProvider = function() {
        BoardService.configureGlobalProvider();
      };
      $scope.configureGlobalAttributes = function() {
        BoardService.configureGlobalAttributes();
      };

      $scope.editSshKeys = function() {
        BoardService.editSshKeys();
      };

      $scope.launchCluster = function() {
        BoardService.launchCluster();
      };

      $scope.switchToTerminal = function() {
        $location.path('/terminal');
      };

      $scope.exitKaramel = function() {
        BoardService.exitKaramel();
      };

      $scope.removeRecipe = function(group, cookbook, recipe) {
        BoardService.removeRecipe(group, cookbook, recipe);
      };

      $scope.removeColumn = function(group) {
        BoardService.removeGroup(group);
      };

      $scope.addNewRecipe = function(group) {
        BoardService.addNewRecipe(group);
      };

      $scope.updateGroup = function(group) {
        BoardService.updateGroupInfo(group);
      };

      $scope.configureGroupAttributes = function(group) {
        BoardService.configureGroupAttributes(group);
      };

      $scope.configureGroupProvider = function(group) {
        BoardService.configureGroupProvider(group);
      };

      $scope.addGroup = function() {
        BoardService.addGroup();
      };

      initScope();

    }])
  .service('BoardService', ['SweetAlert', '$log', '$modal', '$location'
      , '$rootScope', '$window', 'KaramelCoreRestServices', 'BrowserCacheService', 'AlertService',
    function(SweetAlert, $log, $modal, $location, $rootScope, $window,
      KaramelCoreRestServices, BrowserCacheService, AlertService) {

      function _normalizeUrl(originalUrl, pattern, replaceStr) {

        if (originalUrl.indexOf(pattern) !== -1) {
          originalUrl = originalUrl.replace(pattern, replaceStr);
        }
        return originalUrl;
      }
      
      function _launchCluster() {
        var coreFormatCluster = $rootScope.activeCluster.toCoreApiFormat();
        var data = {
          json: angular.toJson(coreFormatCluster)
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

      return {
        addNewRecipe: function(group) {

          var modalInstance = $modal.open({
            templateUrl: 'karamel/partials/column-new-recipe.html',
            controller: 'NewRecipeController',
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

              info.cookbook.github = _normalizeUrl(info.url, "https://github.com/", "");

              var tempCookbook = {
                name: info.cookbook.name,
                github: info.cookbook.github
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
              BrowserCacheService.updateCache();
            }
          });
        },
        removeRecipe: function(group, cookbook, recipe) {
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

              BrowserCacheService.updateCache();
            } else {
              SweetAlert.swal("Cancelled", "Recipe Lives :)", "error");
            }
          });
        },
        removeGroup: function(group) {
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

              BrowserCacheService.updateCache();
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
        addGroup: function() {
          var modalInstance = $modal.open({
            templateUrl: "karamel/partials/editor-group.html",
            controller: "GroupsContoller",
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
              BrowserCacheService.updateCache();
            }
          });
        },
        updateGroupInfo: function(existingGroupInfo) {
          var modalInstance = $modal.open({
            templateUrl: "karamel/partials/editor-group.html",
            controller: "GroupsContoller",
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

              BrowserCacheService.updateCache();
            }
          });
        },
        configureGlobalAttributes: function() {
          var modalInstance = $modal.open({
            templateUrl: "karamel/partials/editor-attributes.html",
            controller: "CookbookAttributeController",
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
              BrowserCacheService.updateCache();
            }
          });
        },
        configureGlobalProvider: function() {
          var modalInstance = $modal.open({
            templateUrl: "karamel/partials/editor-provider.html",
            controller: "provider.editor",
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
              BrowserCacheService.updateCache();
            }
          });

        },
        configureGroupProvider: function(group) {
          var modalInstance = $modal.open({
            templateUrl: "karamel/partials/editor-provider.html",
            controller: "provider.editor",
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
              BrowserCacheService.updateCache();
            }
          });
        },
        configureGroupAttributes: function(group) {
          var modalInstance = $modal.open({
            templateUrl: "karamel/partials/editor-attributes.html",
            controller: "CookbookAttributeController",
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
              BrowserCacheService.updateCache();
            }
          });
        },
        launchCluster: function() {
          var cluster = $rootScope.activeCluster;
          if (cluster === null) {
            $log.info("No Active Cluster Object Present.");
            AlertService.addAlert({type: 'warning', msg: 'No Active Cluster Found.'});
            return;
          }
          if (!$rootScope.activeCluster.areCredentialsSet()) {
            this.setCredentials(true);
          }
          else {
            _launchCluster();
          }
        },
        scaffoldCookbook: function(cluster) {
          $log.info("Scaffold Cookbook function invoked.");
          var modalInstance = $modal.open({
            templateUrl: "karamel/partials/scaffold.html",
            controller: "ScaffoldController",
            backdrop: "static",
            resolve: {
              info: function() {
                return {
                  cluster: {
                    clusterName: cluster.name
                  }
                }
              }
            }
          });
          var data = {
            json: angular.toJson(cluster)
          };
          KaramelCoreRestServices.scaffoldCookbook(data)
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
        setCredentials: function(isLaunch) {
          var modalInstance = $modal.open({
            templateUrl: "karamel/partials/launch.html",
            controller: "LaunchController",
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
              BrowserCacheService.updateCache();

              if (isLaunch) {
                _launchCluster();
              }
            } else if (!$rootScope.activeCluster.areCredentialsSet()) {
              AlertService.addAlert({type: 'warning', msg: 'Credentials Invalid.'});
            }
          });
        }
      }

    }])
  .directive('fileUploader', ['$log', '$rootScope', 'BoardService', 'KaramelCoreRestServices', '$window',
    'AlertService', 'BrowserCacheService',
    function($log, $rootScope, BoardService, KaramelCoreRestServices, $window, AlertService, BrowserCacheService) {
      return{
        restrict: 'A',
        link: function(scope, element, attributes) {

          element.bind('change', function(changeEvent) {
            var reader = new FileReader();
            reader.onload = function(loadEvent) {
              var ymlJson = {
                yml: loadEvent.target.result
              };
              $log.info("Requesting Karamel Core Services for JSON. ");
              BrowserCacheService.resetCache();
              KaramelCoreRestServices.getJsonFromYaml(ymlJson)
                .success(function(data, status, headers, config) {
                  $log.info("Success");

                  try {
                    var cluster = new Cluster();
                    cluster.load(data);
                    $rootScope.activeCluster = cluster;
                    AlertService.addAlert({type: 'success', msg: 'Model Created Successfully.'});
                  }
                  catch (err) {
                    $log.error(err);
                    AlertService.addAlert({type: 'danger', msg: 'Unable to parse json from core.'});
                  }

                  $log.info($rootScope.activeCluster);
                })
                .error(function(data, status, headers, config) {
                  $log.info("Fetch Call Failed.");
                  AlertService.addAlert({type: 'danger', msg: 'Core: ' + data.message});
                });

              element.val("");
            };
            reader.readAsText(changeEvent.target.files[0]);
          });

        }
      }
    }])
  .directive('clickDirective', ['$log', function($log) {

      return {
        restrict: 'A',
        link: function(scope, element, attributes) {

          element.bind('click', function(clickEvent) {
            var uploaderElement = angular.element(document.querySelector("#fileUploader"));
            uploaderElement.trigger('click');
          });

        }
      }

    }]);


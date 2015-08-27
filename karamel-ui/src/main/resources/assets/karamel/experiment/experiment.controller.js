'use strict';

angular.module('main.module')
  .controller('experiment.controller', ['$log', '$scope', '$rootScope', '$timeout', 'SweetAlert', 'core-rest.service',
    'github.service', 'modal.factory', 'experiment.factory',
    function($log, $scope, $rootScope, $timeout, SweetAlert, coreService, githubService, ModalFactory,
      expFactory) {

      var self = this;

      $scope.gs = githubService;

      $scope.status = {
        isOpen: true,
        isModified: true,
        experimentOpen: true,
        userGroup: false,
        sourceOpen: false,
        binaryOpen: false,
        configOpen: false,
        orchestrationOpen: false,
        depdenciesOpen: false,
        parametersOpen: false, 
        chefOpen: false,
        advanced: false,
        expanded: false,
        experiment: [
          {
            name: '',
            status: false
          }
        ]
      };
      $scope.items = ['push', 'clear', 'delete local', 'delete local and remote'];
      $scope.selected = $scope.items[0];

      $scope.options = [{
          name: 'Bash',
          value: 'bash'
        }, {
          name: 'Python',
          value: 'python'
        }];


      $scope.select = function(i, $event) {
        $event.preventDefault();
        if (angular.isDefined(i))
          $scope.selected = $scope.items[i];
      }; // end select


      self.currentTimeout = null;

      $scope.toggle = function() {
        $scope.status.advanced = true;
        $scope.status.userGroup = !$scope.status.userGroup;
        $scope.status.experimentOpen = !$scope.status.experimentOpen;
        $scope.status.sourceOpen = !$scope.status.sourceOpen;
        $scope.status.binaryOpen = !$scope.status.binaryOpen;
        $scope.status.configOpen = !$scope.status.configOpen;
        $scope.status.depdenciesOpen = !$scope.status.depdenciesOpen;
        $scope.status.orchestrationOpen = !$scope.status.orchestrationOpen;
        $scope.status.parametersOpen = !$scope.status.parametersOpen;
        $scope.status.chefOpen = !$scope.status.chefOpen;
        $scope.status.expanded = !$scope.status.expanded;
        for (var i = 0; i < $scope.status.experiment.length; i++) {
          $scope.status.experiment[i].status = !$scope.status.experiment[i].status;
        }
      }


      $scope.isExperimentOpen = function(name) {
        for (var i = 0; i < $scope.status.experiment.length; i++) {
          if ($scope.status.experiment[i].name === name) {
            return $scope.status.experiment[i].status;
          }
        }
        return false;
      }

      $scope.setExperimentOpenTrue = function(name) {
        for (var i = 0; i < $scope.status.experiment.length; i++) {
          if ($scope.status.experiment[i].name === name) {
            $scope.status.experiment[i].status = true;
          }
        }
      }
      $scope.setExperimentOpenFalse = function(name) {
        for (var i = 0; i < $scope.status.experiment.length; i++) {
          if ($scope.status.experiment[i].name === name) {
            $scope.status.experiment[i].status = false;
          }
        }
      }


      $scope.experiment = {
        user: '',
        group: '',
        githubRepo: '',
        description: '',
        githubOwner: '',
        localDependencies: '',
        globalDependencies: '',
        berksfile: '',
        urlBinary: '',
        urlGitClone: '',
        mavenCommand: '',
        experimentSetupCode: '',
        defaultAttributes: '',
        clusterDefinition: '',
        code: [
          {
            name: 'experiment',
            scriptContents: '',
            configFileName: '',
            configFileContents: '',
            scriptType: 'bash'
          }
        ]
      };

      $scope.getUrl = function() {
        if ($scope.status.isModified) {
          return "";
        }
        var len = $scope.experiment.urlGitClone.length;
        return $scope.experiment.urlGitClone.substring(0, len - 4).replace(":", "/");
      }

      $scope.landing = true;

      $scope.experimentNameInvalid = false;
      $scope.githubOwnerInvalid = false;
      $scope.userInvalid = false;
      $scope.groupInvalid = false;
      $scope.scriptTypeInvalid = false;
      $scope.sourceTypeInvalid = false;
      $scope.configInvalid = false;
      $scope.preScriptInvalid = false;
      $scope.scriptInvalid = false;


      $scope.sourceType = {};

      $scope.sourceTypes = [
        {value: "maven", label: "Git-Maven-Jave"},
        {value: "python", label: "Python-Eggs"},
        {value: "ruby", label: "Ruby-Gems"}
      ];


      $scope.configFileName = "";
      $scope.newExperimentName = "";
      $scope.newExperimentErr = false;
      $scope.newExperimentErMsg = "";

      $scope.newExperiment = function($event) {
        $event.preventDefault();
        $scope.newExperimentErr = false;
        for (var i = 0; i < $scope.experiment.code.length; i++) {
          if ($scope.experiment.code[i].name === $scope.newExperimentName) {
            $scope.newExperimentErr = true;
            $scope.newExperimentErrMsg = "Experiment name already exists";
            return;
          }
        }
        $scope.status.isModified = true;

        var newEntry = {
          name: $scope.newExperimentName,
          scriptContents: '',
          configFileName: '',
          configFileContents: '',
          scriptType: 'bash'
        };

        $scope.experiment.code.push(newEntry);
        $scope.newExperimentName = "";
        $scope.newExperimentErrMsg = "";

        var statusEntry = {
          name: $scope.newExperimentName,
          status: true
        };
        $scope.status.experiment.push(statusEntry);
      }

      $scope.newConfigFile = function(experimentName) {
        for (var i = 0; i < $scope.experiment.code.length; i++) {
          if ($scope.experiment.code[i].name === experimentName) {
            $scope.experiment.code[i].configFileName = $scope.configFileName;
            return;
          }
        }
      }

      $scope.removeConfigFile = function(experimentName) {
        for (var i = 0; i < $scope.experiment.code.length; i++) {
          if ($scope.experiment.code[i].name === experimentName) {
            $scope.experiment.code[i].configFileName = "";
            $scope.experiment.code[i].configFileContents = "";
            return;
          }
        }
      }

      $scope.removeExperiment = function(experimentName) {

        SweetAlert.swal({
          title: "Remove " + experimentName + "?",
          text: "This will remove the Chef experiment recipe " + experimentName + ".rb from the local GitHub repository.",
          type: "info",
          showCancelButton: true,
          confirmButtonColor: "#DD6B55", confirmButtonText: "Yes, remove it.",
          cancelButtonText: "Cancel",
          closeOnConfirm: true,
          closeOnCancel: false},
        function(isConfirm) {

          if (isConfirm) {

            for (var i = 0; i < $scope.experiment.code.length; i++) {
              if ($scope.experiment.code[i].name === experimentName) {
                coreService.removeFileFromExperiment($scope.gs.org.name, $scope.gs.repo.name, experimentName)
                  .success(function(data, status, headers, config) {
                    $scope.experiment.code.splice(i, 1);
                    SweetAlert.swal("Deleted", experimentName + " has been deleted.", "success");
                  })
                  .error(function(data, status, headers, config) {
                    $log.info(experimentName + " could not be deleted.");
                  });
                return;
              }
            }

          } else {
            SweetAlert.swal("Cancelled", "Experiment hasn't been deleted.", "error");
          }
        });



      }

      $scope.toggleDropdown = function($event) {
        $event.preventDefault();
        $event.stopPropagation();
        $scope.status.isopen = !$scope.status.isopen;
      };

      $scope.experimentFactoryModal = function() {
        $log.info("New experiment...");
        ModalFactory.experimentFactory('lg').then(
          function(result) {
            if (angular.isDefined(result)) {
              $log.info("new experiment modal experiment results ...");
              $scope.experiment.user = result.githubRepo;
              $scope.experiment.group = result.githubRepo;
              $scope.experiment.githubRepo = result.githubRepo;
              $scope.experiment.githubOwner = result.githubOwner;
              $scope.experiment.urlGitClone = "https://github.com:" + result.githubOwner
                + "/" + result.githubRepo + ".git";
              $scope.experiment.description = result.description;
              $scope.landing = false;
            }
          });
      };

      $scope.loadExperimentProfile = function() {
        $log.info("Load experiment...");
        $scope.gs.org.name = "";
        $scope.gs.repo.name = "";

        ModalFactory.loadExperiment('lg').then(
          function(result) {
            if (angular.isDefined(result)) {
              $log.info("load experiment modal experiment at " + result);
              $scope.experiment.urlGitClone = result;
              if (result !== null && result !== "") {
                $scope.loadExperiment();
              }
            }
          });
      };

      $scope.profileModal = function() {
        $log.info("Loading profile by launching modal dialog.");
        ModalFactory.profile('lg').then(
          function(result) {
            if (angular.isDefined(result)) {
              if (result !== null && result === true) {
                $log.info("Credentials entered");
                githubService.getCredentials();
              }
            }
          });
      };


      function _initScope() {
        $log.log("Looking for cached GitHub Credentials...");
        githubService.getCredentials();
        var exp = expFactory.recover();
        if (exp !== false && exp !== null && typeof exp !== 'undefined') {
          self.deepCopyExperiment(exp);
        }
        restartTimer();
      }

      $scope.deleteRepo = function($event) {
        $event.preventDefault();

        SweetAlert.swal({
          title: "Delete?",
          text: "This removes the repository from both GitHub and local storage. You will not be able to recover it.",
          type: "info",
          showCancelButton: true,
          confirmButtonColor: "#DD6B55", confirmButtonText: "Yes, delete it completely.",
          cancelButtonText: "Do not delete.",
          closeOnConfirm: true,
          closeOnCancel: false},
        function(isConfirm) {
          if (isConfirm) {
            // delete local clone of Repo
            coreService.removeRepo($scope.experiment.githubOwner, $scope.experiment.githubRepo, true, false)
              .success(function(data, status, headers, config) {
                // delete Repo on GitHub
                coreService.removeRepo($scope.experiment.githubOwner, $scope.experiment.githubRepo, true, true)
                  .success(function(data, status, headers, config) {
                    // Core Rest Services
                    // delete Browser LocalStorage
                    clearExperiment();
                    SweetAlert.swal("Deleted", "The Experiment is now removed and cannot be recovered.", "info");
                  })
                  .error(function(data, status, headers, config) {
                    SweetAlert.swal("Problem deleting Repo on GitHub", "You will need to remove the repository from GitHub's website.", "error");
                  });
              })
              .error(function(data, status, headers, config) {
                SweetAlert.swal("Problem deleting local clone", "An error occured when trying to delete the experiment locally", "error");
              });
          } else {
            cancelTimer();
            SweetAlert.swal("Not Deleted", "The Experiment has not been deleted", "error");
          }
        });

      }
      $scope.deleteLocal = function($event) {
        $event.preventDefault();
        SweetAlert.swal({
          title: "Close the experiment and delete the local clone from ${HOME}/.karamel/cookbook_designer?",
          text: "This deletes the copy of the Experiment on your local harddisk, but not the copy on GitHub.",
          type: "info",
          showCancelButton: true,
          confirmButtonColor: "#DD6B55", confirmButtonText: "Yes, delete the local copy only.",
          cancelButtonText: "Do not delete",
          closeOnConfirm: true,
          closeOnCancel: false},
        function(isConfirm) {
          if (isConfirm) {
            coreService.removeRepo($scope.experiment.githubOwner, $scope.experiment.githubRepo, true, false)
              .success(function(data, status, headers, config) {
                // Core Rest Services
                clearExperiment();
                SweetAlert.swal("Deleted", "The experiment is now deleted locally", "info");
              })
              .error(function(data, status, headers, config) {
                $log.info("An error occured when trying to delete the experiment locally.");
              });
          } else {
            cancelTimer();
            SweetAlert.swal("Deleted", "The Experiment has not been deleted", "error");
          }
        });

      }
      $scope.closeBrowser = function($event) {
        $event.preventDefault();

        if ($scope.status.isModified) {
          SweetAlert.swal({
            title: "Close the Experiment and lose unsaved changes?",
            text: "You have unsaved changes that will be lost!",
            type: "info",
            showCancelButton: true,
            confirmButtonColor: "#DD6B55", confirmButtonText: "Yes, close it.",
            cancelButtonText: "Do not close",
            closeOnConfirm: true,
            closeOnCancel: false},
          function(isConfirm) {
            if (isConfirm) {
              clearExperiment();
            } else {
              cancelTimer();
            }
          });
        } else {
          clearExperiment();
        }

      }


      function clearExperiment() {
        cancelTimer();
        $scope.landing = true;
        expFactory.clear();
        $scope.experiment = {
          user: '',
          group: '',
          githubRepo: '',
          description: '',
          githubOwner: '',
          localDependencies: '',
          globalDependencies: '',
          urlBinary: '',
          urlGitClone: '',
          mavenCommand: '',
          experimentSetupCode: '',
          defaultAttributes: '',
          clusterDefinition: '',
          code: [
            {
              name: 'experiment',
              scriptContents: '',
              configFileName: '',
              configFileContents: '',
              scriptType: 'bash'
            }
          ]
        };
        restartTimer();
      }


      function restartTimer() {
        self.currentTimeout = $timeout(saveExperimentTimer, 2000);

        if ($scope.experiment.githubRepo !== "" && $scope.experiment.githubOwner !== ""
          && $scope.gs.githubCredentials.user !== ""
          && $scope.experiment.githubRepo !== ""
          && $scope.experiment.githubOwner !== ""
          && $scope.gs.githubCredentials.email !== "") {
          $scope.landing = false;
        }

      }

      function cancelTimer() {
        if (self.currentTimeout) {
          $timeout.cancel(self.currentTimeout);
          self.currentTimeout = null;
        }
      }

      function saveExperimentTimer() {
        if (!$scope.landing && $scope.experiment !== {} && $scope.experiment.githubRepo !== ""
          && $scope.experiment.name !== "") {
          expFactory.store($scope.experiment);
        }
        if ($rootScope.connected) {
          restartTimer();
        }
      }


      self.deepCopyExperiment = function(data) {
        $scope.experiment.urlBinary = data.urlBinary;
        $scope.experiment.urlGitClone = data.urlGitClone;
        $scope.experiment.mavenCommand = data.mavenCommand;
        $scope.experiment.localDependencies = data.localDependencies;
        $scope.experiment.globalDependencies = data.globalDependencies;
        $scope.experiment.berksfile = data.berksfile;
        $scope.experiment.user = data.user;
        $scope.experiment.group = data.group;
        $scope.experiment.githubRepo = data.githubRepo;
        $scope.experiment.githubOwner = data.githubOwner;
        $scope.experiment.experimentSetupCode = data.experimentSetupCode;
        $scope.experiment.defaultAttributes = data.defaultAttributes;
        $scope.experiment.code = [];
        for (var i = 0; i < data.code.length; i++) {
          var newEntry = {
            name: data.code[i].name,
            scriptContents: data.code[i].scriptContents,
            configFileName: data.code[i].configFileName,
            configFileContents: data.code[i].configFileContents,
            scriptType: data.code[i].scriptType
          };
          $scope.experiment.code.push(newEntry);

          $scope.status.experiment = [];
          var statusEntry = {
            name: data.code[i].name,
            status: true
          };
          $scope.status.experiment.push(statusEntry);

        }
      }


      $scope.loadExperiment = function() {

        coreService.loadExperiment($scope.experiment.urlGitClone)
          .success(function(data, status, headers, config) {

            if (angular.isDefined(data)) {
              $log.info("Experiment Loaded Successfully.");
              self.deepCopyExperiment(data);
              $scope.landing = false;
              $scope.status.isModified = false;
            }
          })
          .error(function(data, status, headers, config) {
            SweetAlert.swal("Problem loading from GitHub", data.reason, "error");
          });
      }

      $scope.pushExperiment = function($event) {
        $event.preventDefault();

        coreService.pushExperiment($scope.experiment)
          .success(function(data, status, headers, config) {
            $scope.status.isModified = false;
          })
          .error(function(data, status, headers, config) {
            SweetAlert.swal("Problem saving to GitHub", data.reason, "error");
          });


      }

      _initScope();
    }])
'use strict';

angular.module('karamel.main')
        .controller('ExperimentCtrl', ['$log', '$scope', '$timeout', 'SweetAlert', 'KaramelCoreRestServices',
            'GithubService', 'ModalService', 'ExperimentsService', 'BoardService',
            function ($log, $scope, $timeout, SweetAlert, KaramelCoreRestServices, GithubService, ModalService,
                    ExperimentsService, BoardService) {

                var self = this;

                $scope.gs = GithubService;

                $scope.status = {
                    isOpen: true,
                    experimentOpen: true,
                    userGroup: false,
                    sourceOpen: false,
                    binaryOpen: false,
                    configOpen: false,
                    depdenciesOpen: false,
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


                $scope.select = function (i, $event) {
                    $event.preventDefault();
                    if (angular.isDefined(i))
                        $scope.selected = $scope.items[i];
                }; // end select


                self.currentTimeout = null;

                $scope.toggle = function () {
                    $scope.status.advanced = true;
                    $scope.status.userGroup = !$scope.status.userGroup;
                    $scope.status.experimentOpen = !$scope.status.experimentOpen;
                    $scope.status.sourceOpen = !$scope.status.sourceOpen;
                    $scope.status.binaryOpen = !$scope.status.binaryOpen;
                    $scope.status.configOpen = !$scope.status.configOpen;
                    $scope.status.depdenciesOpen = !$scope.status.depdenciesOpen;
                    $scope.status.chefOpen = !$scope.status.chefOpen;
                    $scope.status.expanded = !$scope.status.expanded;
                    for (var i = 0; i < $scope.status.experiment.length; i++) {
                        $scope.status.experiment[i].status = !$scope.status.experiment[i].status;
                    }
                }


                $scope.isExperimentOpen = function (name) {
                    for (var i = 0; i < $scope.status.experiment.length; i++) {
                        if ($scope.status.experiment[i].name === name) {
                            return $scope.status.experiment[i].status;
                        }
                    }
                    return false;
                }

                $scope.setExperimentOpenTrue = function (name) {
                    for (var i = 0; i < $scope.status.experiment.length; i++) {
                        if ($scope.status.experiment[i].name === name) {
                            $scope.status.experiment[i].status = true;
                        }
                    }
                }
                $scope.setExperimentOpenFalse = function (name) {
                    for (var i = 0; i < $scope.status.experiment.length; i++) {
                        if ($scope.status.experiment[i].name === name) {
                            $scope.status.experiment[i].status = false;
                        }
                    }
                }


                $scope.loading = false;

                $scope.experiment = {
                    user: '',
                    group: '',
                    githubRepo: '',
                    description: '',
                    githubOwner: '',
                    dependencies: '',
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

                $scope.newExperiment = function ($event) {
                    $event.preventDefault();
                    $scope.newExperimentErr = false;
                    for (var i = 0; i < $scope.experiment.code.length; i++) {
                        if ($scope.experiment.code[i].name === $scope.newExperimentName) {
                            $scope.newExperimentErr = true;
                            $scope.newExperimentErrMsg = "Experiment name already exists";
                            return;
                        }
                    }

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
                }

                $scope.newConfigFile = function (experimentName) {
                    for (var i = 0; i < $scope.experiment.code.length; i++) {
                        if ($scope.experiment.code[i].name === experimentName) {
                            $scope.experiment.code[i].configFileName = $scope.configFileName;
                            return;
                        }
                    }
                }

                $scope.removeConfigFile = function (experimentName) {
                    for (var i = 0; i < $scope.experiment.code.length; i++) {
                        if ($scope.experiment.code[i].name === experimentName) {
                            $scope.experiment.code[i].configFileName = "";
                            $scope.experiment.code[i].configFileContents = "";
                            return;
                        }
                    }
                }

                $scope.removeExperiment = function (experimentName) {

                    SweetAlert.swal({
                        title: "Remove " + experimentName + "?",
                        text: "This will remove the Chef experiment recipe " + experimentName + ".rb from the local GitHub repository.",
                        type: "info",
                        showCancelButton: true,
                        confirmButtonColor: "#DD6B55", confirmButtonText: "Yes, remove it.",
                        cancelButtonText: "Cancel",
                        closeOnConfirm: true,
                        closeOnCancel: false},
                    function (isConfirm) {

                        if (isConfirm) {

                            for (var i = 0; i < $scope.experiment.code.length; i++) {
                                if ($scope.experiment.code[i].name === experimentName) {
                                    KaramelCoreRestServices.removeFileFromExperiment($scope.gs.org.name, $scope.gs.repo.name, experimentName)
                                            .success(function (data, status, headers, config) {
                                                $scope.experiment.code.splice(i, 1);
                                                SweetAlert.swal("Deleted", experimentName + " has been deleted.", "success");
                                            })
                                            .error(function (data, status, headers, config) {
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

                $scope.toggleDropdown = function ($event) {
                    $event.preventDefault();
                    $event.stopPropagation();
                    $scope.status.isopen = !$scope.status.isopen;
                };

                $scope.experimentFactoryModal = function () {
                    $log.info("New experiment...");
                    ModalService.experimentFactory('lg').then(
                            function (result) {
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

                $scope.loadExperimentProfile = function () {
                    $log.info("Load experiment...");
                    ModalService.loadExperiment('lg').then(
                            function (result) {
                                if (angular.isDefined(result)) {
                                    $log.info("load experiment modal experiment results ...");
                                    $scope.experiment.urlGitClone = result.githubUrl;
                                    loadExperiment(result.githubUrl);
                                    $scope.landing = false;
                                }
                            });
                };

                $scope.profileModal = function () {
                    $log.info("Loading profile by launching modal dialog.");
                    ModalService.profile('lg');
                };


                function _initScope() {
                    $log.log("Looking for cached GitHub Credentials...");
                    GithubService.getCredentials();

                    var exp = ExperimentsService.recover();
                    if (exp !== false && exp !== null && typeof exp !== 'undefined') {
                        self.deepCopyExperiment(exp);
                        $scope.landing = false;
                    }
                    restartTimer();
                }

                $scope.deleteRepo = function($event) {
                    $event.preventDefault();

                    SweetAlert.swal({
                        title: "Delete the experiment completely (not recoverable)?",
                        text: "This removes the experiment from both GitHub and local storage. You will not be able to recover it.",
                        type: "info",
                        showCancelButton: true,
                        confirmButtonColor: "#DD6B55", confirmButtonText: "Yes, delete it completely.",
                        cancelButtonText: "Do not delete.",
                        closeOnConfirm: true,
                        closeOnCancel: false},
                    function (isConfirm) {
                        if (isConfirm) {
                            KaramelCoreRestServices.removeRepo($scope.experiment.githubOwner, $scope.experiment.githubRepo, true, true)
                                    .success(function (data, status, headers, config) {
                                        // Core Rest Services
                                        SweetAlert.swal("Deleted", "The Experiment is now removed and cannot be recovered.", "info");
                                    })
                                    .error(function (data, status, headers, config) {
                                        $log.info("There was an error when trying to delete the experiment.");
                                    });
                            clearExperiment(false);
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
                    function (isConfirm) {
                        if (isConfirm) {
                            clearExperiment();
                            KaramelCoreRestServices.removeRepo($scope.experiment.githubOwner, $scope.experiment.githubRepo, true, false)
                                    .then(function (data, status, headers, config) {
                                        // Core Rest Services
                                        SweetAlert.swal("Deleted", "The experiment is now deleted locally", "info");
                                    })
                                    .error(function (data, status, headers, config) {
                                        $log.info("An error occured when trying to delete the experiment locally.");
                                    });

                        } else {
                            cancelTimer();
                            SweetAlert.swal("Deleted", "The Experiment has not been deleted", "error");
                        }
                    });

                }
                $scope.deleteBrowser = function($event) {
                    $event.preventDefault();

                    SweetAlert.swal({
                        title: "Close the Experiment?",
                        text: "If you have already saved it to GitHub, you will be able load the experiment again.",
                        type: "info",
                        showCancelButton: true,
                        confirmButtonColor: "#DD6B55", confirmButtonText: "Yes, close it.",
                        cancelButtonText: "Do not close",
                        closeOnConfirm: true,
                        closeOnCancel: false},
                    function (isConfirm) {
                        if (isConfirm) {
                            // Set landing true first to prevent a race-condition with the timer
                            clearExperiment();
                            // Core Rest Services
                            SweetAlert.swal("Closed", "The experiment is closed.", "info");
                        } else {
                            cancelTimer();
                            SweetAlert.swal("Cancelled", "The experiment has not been closed", "error");
                        }
                    });

                }


                function clearExperiment() {
                    cancelTimer();
                    $scope.landing = true;
                    ExperimentsService.clear();
                    $scope.experiment = {
                        user: '',
                        group: '',
                        githubRepo: '',
                        description: '',
                        githubOwner: '',
                        dependencies: '',
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
                                scriptType: ''
                            }
                        ]
                    };
                    restartTimer();

                }


                function restartTimer() {
                    self.currentTimeout = $timeout(saveExperimentTimer, 2000);
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
                        ExperimentsService.store($scope.experiment);
                    }
                    restartTimer();
                }


                self.deepCopyExperiment = function (data) {
                    $scope.experiment.urlBinary = data.urlBinary;
                    $scope.experiment.urlGitClone = data.urlGitClone;
                    $scope.experiment.mavenCommand = data.mavenCommand;
                    $scope.experiment.dependencies = data.dependencies;
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
                    }

                }


                $scope.loadExperiment = function() {

                    SweetAlert.swal({
                        title: "Load experiment: " + $scope.experiment.urlGitClone.substring(19) + "?",
                        text: "This will load the experiment from the master branch in the GitHub repository and overwrite any local copy of the experiment.",
                        type: "warning",
                        showCancelButton: true,
                        confirmButtonColor: "#DD6B55", confirmButtonText: "Yes, load it.",
                        cancelButtonText: "Cancel",
                        closeOnConfirm: true,
                        closeOnCancel: false},
                    function (isConfirm) {

                        if (isConfirm) {

                            KaramelCoreRestServices.loadExperiment($scope.experiment.urlGitClone)
                                    .success(function (data, status, headers, config) {

                                        if (angular.isDefined(data)) {
                                            $log.info("Experiment Loaded Successfully.");
                                            self.deepCopyExperiment(data);
                                            $scope.landing = false;
                                        }
                                    })
                                    .error(function (data, status, headers, config) {
                                        $log.info("Experiment can't be Loaded.");
                                    });
                        } else {
                            cancelTimer();
                            SweetAlert.swal("Deleted", "The Browser has not been cleared", "error");
                        }
                    });
                }

                $scope.pushExperiment = function($event) {
                    $event.preventDefault();
                
//                    $scope.experimentNameInvalid = false;
//                    $scope.githubOwnerInvalid = false;
//                    $scope.userInvalid = false;
//                    $scope.groupInvalid = false;
//
//                    if (!$scope.uploadExperiment.experimentName.$valid) {
//                        $scope.experimentNameInvalid = true;
//                    }
//
//                    if (!$scope.uploadExperiment.experimentName.$valid) {
//                        $scope.experimentNameInvalid = true;
//                    }
//
//                    if (!$scope.uploadExperiment.user.$valid) {
//                        $scope.userInvalid = true;
//                    }
//
//                    if (!$scope.uploadExperiment.group.$valid) {
//                        $scope.groupInvalid = true;
//                    }



                    SweetAlert.swal({
                        title: "Commit and Push Experiment to GitHub?",
                        text: "This requires a functioning Internet connection. The Experiment will generate a commit and push to the master branch of your GitHub repository.",
                        type: "info",
                        showCancelButton: true,
                        confirmButtonColor: "#DD6B55", confirmButtonText: "Yes, push it!",
                        cancelButtonText: "Cancel",
                        closeOnConfirm: true,
                        closeOnCancel: false},
                    function (isConfirm) {

                        if (isConfirm) {
                            KaramelCoreRestServices.pushExperiment($scope.experiment)
                                    .success(function (data, status, headers, config) {
                                        SweetAlert.swal("Pushed!", "Experiment Pushed to GitHub. \\\m/", "success");
                                        $log.info("Experiment Pushed Successfully.");
                                    })
                                    .error(function (data, status, headers, config) {
                                        $log.info("Experiment can't be Pushed.");
                                        SweetAlert.swal("Problem pushing", "The Experiment could not pushed to GitHub. Is your Internet connection working?", "error");
                                    });
                            return;
                        } else {
                            SweetAlert.swal("Cancelled", "Experiment hasn't been pushed to GitHub", "error");
                        }
                    });




                }

                _initScope();
            }])
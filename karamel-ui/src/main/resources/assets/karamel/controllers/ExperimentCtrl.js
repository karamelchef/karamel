'use strict';

angular.module('karamel.main')
        .controller('ExperimentCtrl', ['$log', '$scope', '$timeout', 'SweetAlert', 'KaramelCoreRestServices',
            'GithubService', 'ModalService', 'ExperimentsService',
            function ($log, $scope, $timeout, SweetAlert, KaramelCoreRestServices, GithubService, ModalService, ExperimentsService) {

                var self = this;

                $scope.gs = GithubService;

                $scope.status = {
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
                    mavenCommand: 'mvn install -DskipTests',
//                    resultsDir: '/tmp/results',
                    experimentSetupCode: '',
                    defaultAttributes: '',
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


                $scope.landing = true;
                $scope.load = true;

                $scope.experimentNameInvalid = false;
                $scope.githubOwnerInvalid = false;
                $scope.userInvalid = false;
                $scope.groupInvalid = false;
                $scope.scriptTypeInvalid = false;
                $scope.sourceTypeInvalid = false;
                $scope.configInvalid = false;
                $scope.preScriptInvalid = false;
                $scope.scriptInvalid = false;


                $scope.scriptTypes = [
                    {value: "bash", label: "Bash"},
                    {value: "python", label: "Python"},
                    {value: "ruby", label: "Ruby"},
                    {value: "perl", label: "Perl"}
                ];
                $scope.scriptType = {};
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

                $scope.newExperiment = function () {
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
                                    $scope.isExpLoaded = true;
                                    $log.info("new experiment modal experiment results ...");
                                    $scope.experiment.user = result.githubRepo;
                                    $scope.experiment.group = result.githubRepo;
                                    $scope.experiment.githubRepo = result.githubRepo;
                                    $scope.experiment.githubOwner = result.githubOwner;
                                    $scope.experiment.description = result.description;
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

                $scope.deleteRepository = function deleteRepository($event) {
                    $event.preventDefault();

                    SweetAlert.swal({
                        title: "Delete the Experiment completely (not recoverable)?",
                        text: "This removes the experiment from both GitHub and local storage. You will not be able to recover it.",
                        type: "info",
                        showCancelButton: true,
                        confirmButtonColor: "#DD6B55", confirmButtonText: "Yes, wipe it.",
                        cancelButtonText: "Do not delete",
                        closeOnConfirm: true,
                        closeOnCancel: false},
                    function (isConfirm) {
                        if (isConfirm) {
                            // Set landing true first to prevent a race-condition with the timer
                            clearExperiment();
                            // Core Rest Services
                            SweetAlert.swal("Deleted", "The Experiment is now gone completely.", "info");
                        } else {
                            cancelTimer();
                            SweetAlert.swal("Deleted", "The Experiment has not been deleted", "error");
                        }
                    });

                }
                $scope.deleteBrowser = function deleteLocal($event) {
                    $event.preventDefault();

                    SweetAlert.swal({
                        title: "Delete the local clone of the Experiment and from HTML local storage?",
                        text: "This deletes the experiment clone on your local harddisk, but not the copy on GitHub.",
                        type: "info",
                        showCancelButton: true,
                        confirmButtonColor: "#DD6B55", confirmButtonText: "Yes, delete the local copy only.",
                        cancelButtonText: "Do not delete",
                        closeOnConfirm: true,
                        closeOnCancel: false},
                    function (isConfirm) {
                        if (isConfirm) {
                            // Set landing true first to prevent a race-condition with the timer
                            clearExperiment();
                            // Core Rest Services
                            SweetAlert.swal("Deleted", "The Experiment is now deleted locally", "info");
                        } else {
                            cancelTimer();
                            SweetAlert.swal("Deleted", "The Experiment has not been deleted", "error");
                        }
                    });

                }
                $scope.deleteBrowser = function clearBrowser($event) {
                    $event.preventDefault();

                    SweetAlert.swal({
                        title: "Clear the Experiment from your browser's local storage?",
                        text: "This clear the experiment you had been editing from your browser (not GitHub).",
                        type: "info",
                        showCancelButton: true,
                        confirmButtonColor: "#DD6B55", confirmButtonText: "Yes, clear it.",
                        cancelButtonText: "Do not clear",
                        closeOnConfirm: true,
                        closeOnCancel: false},
                    function (isConfirm) {
                        if (isConfirm) {
                            // Set landing true first to prevent a race-condition with the timer
                            clearExperiment();
                            // Core Rest Services
                            SweetAlert.swal("Deleted", "The Browser is now cleared", "info");
                        } else {
                            cancelTimer();
                            SweetAlert.swal("Deleted", "The Browser has not been cleared", "error");
                        }
                    });

                }
                
                
                function clearExperiment() {
                            $scope.landing = true;
                            ExperimentsService.store(null);
                            $scope.experiment = {
                                user: '',
                                group: '',
                                githubRepo: '',
                                description: '',
                                githubOwner: '',
                                dependencies: '',
                                urlBinary: '',
                                urlGitClone: '',
                                mavenCommand: 'mvn install -DskipTests',
                                experimentSetupCode: '',
                                defaultAttributes: '',
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
                    
                }


                function restartTimer() {
                    self.currentTimeout = $timeout(saveExperimentTimer, 500);
                }

                function cancelTimer() {
                    if (self.currentTimeout) {
                        $timeout.cancel(self.currentTimeout);
                        self.currentTimeout = null;
                    }
                }

                function saveExperimentTimer() {
                    if (!$scope.landing && $scope.experiment !== {} && $scope.experiment.githubRepo !== "") {
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


                $scope.loadExperiment = function loadExperiment() {
                    KaramelCoreRestServices.loadExperiment(experiment.url)
                            .then(function (data, status, headers, config) {

                                if (angular.isDefined(data)) {
                                    $log.info("Experiment Loaded Successfully.");
                                    $scope.isExpLoaded = true;
                                    self.deepCopyExperiment(data);
                                    $scope.landing = false;
                                }
                            })
                            .error(function (data, status, headers, config) {
                                $log.info("Experiment can't be Loaded.");
                            });

                }

                $scope.pushExperiment = function pushExperiment() {

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
                                    });
//                            ExperimentsService.store($scope.experiment);
                            SweetAlert.swal("Pushed", "The Experiment has been pushed to GitHub and can be loaded using its github URL", "error");
                            return;
                        } else {
                            SweetAlert.swal("Cancelled", "Experiment hasn't been pushed to GitHub", "error");
                        }
                    });




                }

                _initScope();
            }])
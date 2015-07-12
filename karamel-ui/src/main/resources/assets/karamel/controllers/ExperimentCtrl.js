'use strict';

angular.module('karamel.main')
        .controller('ExperimentCtrl', ['$log', '$scope', 'SweetAlert', 'KaramelCoreRestServices', 'GithubService', 'ModalService',
            function ($log, $scope, SweetAlert, KaramelCoreRestServices, GithubService, ModalService) {

                var self = this;

                $scope.gs = GithubService;

                $scope.status = {
                    experimentOpen: false,
                    userGroup: false,
                    sourceOpen: false,
                    binaryOpen: false,
                    configOpen: false,
                    depdenciesOpen: false,
                    chefOpen: false,
                    advanced: false,
                    expanded: false
                };


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
                    resultsDir: '/tmp/results',
                    experimentSetupCode: '',
                    defaultAttributes: '',
                    code: [
                        {
                            name: 'experiment',
                            scriptContents: '',
                            preScriptChefCode: '',
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
                    $scope.experiment.code[$scope.experiment.code.length].name = $scope.newExperimentName;
                    $scope.experiment.code[$scope.experiment.code.length].scriptContents = "";
                    $scope.experiment.code[$scope.experiment.code.length].preScriptChefCode = "";
                    $scope.experiment.code[$scope.experiment.code.length].scriptType = "";
                }


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
                                    $scope.experiment.user = result.user;
                                    $scope.experiment.group = result.group;
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

                self.getEmailHash = function () {
                    return GithubService.getEmailhash();
                }

                function _initScope() {
                    $log.log("Looking for cached GitHub Credentials...");
                    GithubService.getCredentials();
                }

                $scope.loadExperiment = function loadExperiment() {
                    KaramelCoreRestServices.loadExperiment(experiment.url)
                            .then(function (data, status, headers, config) {

                                if (angular.isDefined(data)) {
                                    $log.info("Experiment Loaded Successfully.");
                                    $scope.isExpLoaded = true;
                                    $scope.experiment.urlBinary = data.urlBinary;
                                    $scope.experiment.urlGitClone = data.urlGitClone;
                                    $scope.experiment.mavenCommand = data.mavenCommand;
                                    $scope.experiment.dependencies = data.dependencies;
                                    $scope.experiment.user = data.user;
                                    $scope.experiment.group = data.group;
                                    $scope.experiment.githubRepo = data.githubRepo;
                                    $scope.experiment.githubOwner = data.githubOwner;
                                    $scope.experiment.experimentSetupCode = data.experimentSetupCode;
                                    $scope.experiment.context.resultsDir = data.context.resultsDir;
                                    $scope.experiment.context.scriptContents = data.context.scriptContents;
                                    $scope.experiment.context.preScriptChefCode = data.context.preScriptChefCode;
                                    $scope.experiment.context.defaultAttributes = data.context.defaultAttributes;
                                    $scope.experiment.context.scriptType = data.context.scriptType;
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
                        closeOnConfirm: false,
                        closeOnCancel: false},
                    function (isConfirm) {

                        if (isConfirm) {
                            KaramelCoreRestServices.pushExperiment($scope.experiment)
                                    .success(function (data, status, headers, config) {
//                                $scope.experimentContext = data;
                                        SweetAlert.swal("Pushed!", "Experiment Pushed to GitHub. \\\m/", "success");
                                        $log.info("Experiment Pushed Successfully.");
                                    })
                                    .error(function (data, status, headers, config) {
                                        $log.info("Experiment can't be Pushed.");
                                    });

                        } else {
                            SweetAlert.swal("Cancelled", "Experiment hasn't been pushed to GitHub", "error");
                        }
                    });




                }

                _initScope();
            }])
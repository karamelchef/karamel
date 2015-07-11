'use strict';

angular.module('karamel.main')
        .controller('ExperimentCtrl', ['$log', '$scope', 'SweetAlert', 'KaramelCoreRestServices', 'GithubService', 'ModalService',
            function ($log, $scope, SweetAlert, KaramelCoreRestServices, GithubService, ModalService) {

                var self = this;

                $scope.status = {
                    experimentOpen: true,
                    sourceOpen: false,
                    binaryOpen: false,
                    configOpen: false,
                    depdenciesOpen: false,
                    chefOpen: false,
                    advanced: false,
                    expanded: false
                };
                
                self.expandAll = function() {
                    $scope.status.experimentOpen = !$scope.status.experimentOpen;
                    $scope.status.sourceOpen = !$scope.status.sourceOpen;
                    $scope.status.binaryOpen = !$scope.status.binaryOpen;
                    $scope.status.configOpen = !$scope.status.configOpen;
                    $scope.status.depdenciesOpen = !$scope.status.depdenciesOpen;
                    $scope.status.chefOpen = !$scope.status.chefOpen;
                    $scope.status.advanced = true;
                    $scope.status.expanded = !$scope.status.expanded;
                }

                $scope.gs = GithubService;

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
                                    $scope.experiment.url = "";
                                    $scope.experiment.user = result.user;
                                    $scope.experiment.group = result.group;
                                    $scope.experiment.githubRepo = result.githubRepo;
                                    $scope.experiment.githubOwner = result.githubOwner;
                                    $scope.experiment.resultsDir = result.resultsDir;
                                    $scope.experiment.dependencies = result.dependencies;
                                    $scope.experiment.experimentContext.scriptContents = result.experimentContext.scriptContents;
                                    $scope.experiment.experimentContext.preScriptChefCode = result.experimentContext.preScriptChefCode;
                                    $scope.experiment.experimentContext.defaultAttributes = result.experimentContext.defaultAttributes;
                                    $scope.experiment.experimentContext.scriptType = result.experimentContext.scriptType;
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

                $scope.loading = false;

                $scope.experiment = {
                    binaryUrl: '',
                    sourceUrl: '',
                    mavenCommand: 'mvn install -DskipTests',
                    user: '',
                    group: '',
                    githubRepo: '',
                    githubOwner: '',
                    resultsDir: '/var/karamel/results',
                    dependencies: '',
                    experimentContext: [
                        {
                            scriptContents: '',
                            defaultAttributes: '',
                            preScriptChefCode: '',
                            scriptType: ''
                        }
                    ]
                };

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
                                    $scope.experiment.url = data.url;
                                    $scope.experiment.user = data.user;
                                    $scope.experiment.group = data.group;
                                    $scope.experiment.githubRepo = data.githubRepo;
                                    $scope.experiment.githubOwner = data.githubOwner;
                                    $scope.experiment.resultsDir = data.resultsDir;
                                    $scope.experiment.dependencies = data.dependencies;
                                    $scope.experiment.experimentContext.scriptContents = data.experimentContext.scriptContents;
                                    $scope.experiment.experimentContext.preScriptChefCode = data.experimentContext.preScriptChefCode;
                                    $scope.experiment.experimentContext.defaultAttributes = data.experimentContext.defaultAttributes;
                                    $scope.experiment.experimentContext.scriptType = data.experimentContext.scriptType;
                                    $scope.landing = false;
                                }
                            })
                            .error(function (data, status, headers, config) {
                                $log.info("Experiment can't be Loaded.");
                            });

                }

                $scope.pushExperiment = function pushExperiment() {

                    $scope.experimentNameInvalid = false;
                    $scope.githubOwnerInvalid = false;
                    $scope.userInvalid = false;
                    $scope.groupInvalid = false;

                    if (!$scope.uploadExperiment.experimentName.$valid) {
                        $scope.experimentNameInvalid = true;
                    }

                    if (!$scope.uploadExperiment.experimentName.$valid) {
                        $scope.experimentNameInvalid = true;
                    }

                    if (!$scope.uploadExperiment.user.$valid) {
                        $scope.userInvalid = true;
                    }

                    if (!$scope.uploadExperiment.group.$valid) {
                        $scope.groupInvalid = true;
                    }



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
                            KaramelCoreRestServices.pushExperiment(experimentContext)
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
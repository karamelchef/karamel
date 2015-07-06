'use strict';

angular.module('karamel.main')
        .controller('ExperimentCtrl', ['$log', '$scope', 'SweetAlert', 'KaramelCoreRestServices', 'GithubService', 'ModalService',
            function ($log, $scope, SweetAlert, KaramelCoreRestServices, GithubService, ModalService) {

                var self = this;
                $scope.isExpLoaded = false;
                $scope.isBinary = false;
                $scope.isSource = false;

                $scope.githubStatus = GithubService;

                $scope.load = true;

                $scope.toggleIsBinary = function () {
                    $scope.isBinary = $scope.isBinary === false ? true : false;
                };
                
                $scope.toggleIsSource = function () {
                    $scope.isSource = $scope.isSource === false ? true : false;
                };

                $scope.status = {
                    isopen: false
                };
                $scope.toggleDropdown = function ($event) {
                    $event.preventDefault();
                    $event.stopPropagation();
                    $scope.status.isopen = !$scope.status.isopen;
                };

                $scope.experimentFactoryModal = function () {
                    $log.info("New experiment...");
                    ModalService.experimentFactory('lg').then(
                            function (result) {
                                $scope.isExpLoaded = true;
                                $log.info("new experiment modal experiment results ...");
                                $scope.experiment.url = result.url;
                                $scope.experiment.user = result.user;
                                $scope.experiment.group = result.group;
                                $scope.experiment.githubRepo = result.githubRepo;
                                $scope.experiment.githubOwner = result.githubOwner;
                                $scope.experiment.experimentContext.scriptContents = result.experimentContext.scriptContents;
                                $scope.experiment.experimentContext.preScriptChefCode = result.experimentContext.preScriptChefCode;
                                $scope.experiment.experimentContext.defaultAttributes = result.experimentContext.defaultAttributes;
                                $scope.experiment.experimentContext.scriptType = result.experimentContext.scriptType;
                            });
                };

                $scope.profileModal = function () {
                    $log.info("Loading profile by launching modal dialog.");
                    ModalService.profile('lg');
//                    ModalService.profile('md');
                };

                $scope.getEmailHash = function () {
                    return GithubService.getEmailHash();
                }

                $scope.getEmail = function () {
                    return GithubService.getEmail();
                }

                $scope.getUser = function () {
                    return GithubService.getUser();
                }

                $scope.getPassword = function () {
                    return GithubService.getPassword();
                }

                $scope.loading = false;

                $scope.experiment = {
                    url: '',
                    user: '',
                    group: '',
                    githubRepo: '',
                    githubOwner: '',
                    experimentContext: [
                        {scriptContents: '',
                            defaultAttributes: '',
                            preScriptChefCode: '',
                            scriptType: ''
                        }
                    ]
                };

                function _initScope() {
                    $log.log("Looking for cached GitHub Credentials...");
                    GithubService.getCredentials();
                    GithubService.setOrgName(GithubService.getUser());
                }

                $scope.loadExperiment = function loadExperiment() {
                    KaramelCoreRestServices.loadExperiment(experimentContext.githubUrl)
                            .success(function (data, status, headers, config) {
                                $log.info("Experiment Loaded Successfully.");
                                $scope.isExpLoaded = true;
                                $scope.experiment.url = data.url;
                                $scope.experiment.user = data.user;
                                $scope.experiment.group = data.group;
                                $scope.experiment.githubRepo = data.githubRepo;
                                $scope.experiment.githubOwner = data.githubOwner;
                                $scope.experiment.experimentContext.scriptContents = data.experimentContext.scriptContents;
                                $scope.experiment.experimentContext.preScriptChefCode = data.experimentContext.preScriptChefCode;
                                $scope.experiment.experimentContext.defaultAttributes = data.experimentContext.defaultAttributes;
                                $scope.experiment.experimentContext.scriptType = data.experimentContext.scriptType;


                            })
                            .error(function (data, status, headers, config) {
                                $log.info("Experiment can't be Loaded.");
                            });

                }

                $scope.pushExperiment = function pushExperiment() {

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
//        .directive('modal', function () {
//            return {
//                template: '<div class="modal fade">' +
//                        '<div class="modal-dialog modal-md">' +
//                        '<div class="modal-content">' +
//                        '<div class="modal-header">' +
//                        '<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>' +
//                        '<h2 class="modal-title">{{ title }}</h2>' +
//                        '</div>' +
//                        '<div class="modal-body" ng-transclude></div>' +
//                        '</div>' +
//                        '</div>' +
//                        '</div>',
//                restrict: 'E',
//                transclude: true,
//                replace: true,
//                scope: true,
//                link: function postLink(scope, element, attrs) {
//                    scope.title = attrs.title;
//
//                    scope.$watch(attrs.visible, function (value) {
//                        if (value == true)
//                            $(element).modal('show');
//                        else
//                            $(element).modal('hide');
//                    });
//
//                    $(element).on('shown.bs.modal', function () {
//                        scope.$apply(function () {
//                            scope.$parent[attrs.visible] = true;
//                        });
//                    });
//
//                    $(element).on('hidden.bs.modal', function () {
//                        scope.$apply(function () {
//                            scope.$parent[attrs.visible] = false;
//                        });
//                    });
//                }
//            };
//        });
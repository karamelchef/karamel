'use strict';

angular.module('karamel.main')
        .controller('ExperimentCtrl', ['$log', '$scope', 'SweetAlert', 'KaramelCoreRestServices', 'GithubService', 'ModalService',
            function ($log, $scope, SweetAlert, KaramelCoreRestServices, GithubService, ModalService) {

                var self = this;
                self.isExpLoaded = false;

                $scope.githubStatus = GithubService;

                self.load = true;

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
                            function (success) {
                                $log.info("new experiment modal success ...");
                                self.isExpLoaded = true;
                            }).then(
                            function (experiment) {
                                $log.info("new experiment modal experiment results ...");
                                self.experiment.url = experiment.url;
                                self.experiment.user = experiment.repoName;
                                self.experiment.group = experiment.repoName;
                                self.experiment.githubRepo = experiment.repoName;
                                self.experiment.githubOwner = experiment.orgName;
                                self.experiment.experimentContext.scriptContents = experiment.experimentContext.scriptContents;
                                self.experiment.experimentContext.preScriptChefCode = experiment.experimentContext.preScriptChefCode;
                                self.experiment.experimentContext.defaultAttributes = experiment.experimentContext.defaultAttributes;
                                self.experiment.experimentContext.scriptType = experiment.experimentContext.scriptType;
                            });

                    // experimentContext = ret.experimentContext;
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
                }

                $scope.loadExperiment = function loadExperiment() {
                    KaramelCoreRestServices.loadExperiment(experimentContext.githubUrl)
                            .success(function (data, status, headers, config) {
//                                $scope.experimentContext = data;
                                $log.info("Experiment Loaded Successfully.");
                                self.isExpLoaded = true;
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
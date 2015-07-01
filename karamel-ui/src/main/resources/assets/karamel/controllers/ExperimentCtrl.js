'use strict';

angular.module('karamel.main')
        .controller('ExperimentCtrl', ['$log', '$scope', 'KaramelCoreRestServices', 'GithubService', 'ModalService',
            function ($log, $scope, KaramelCoreRestServices, GithubService, ModalService) {

                $scope.status = {
                    isopen: false
                };
                $scope.toggleDropdown = function ($event) {
                    $event.preventDefault();
                    $event.stopPropagation();
                    $scope.status.isopen = !$scope.status.isopen;
                };

                $scope.profileModal = function () {
                    $log.info("Loading profile by launching modal dialog.");
                    ModalService.profile('md');
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
                $scope.experimentContext = {
                    githubUrl: "",
                    experimentName: "",
                    experimentUser: "",
                    experimentGroup: "",
                    experimentPreChefCode: "",
                    experimentScript: ""
                };

                function _initScope() {
                    $log.log("Looking for cached GitHub Credentials...");
                    GithubService.getCredentials();
                }

                function loadExperiment(experimentUrl) {
                    KaramelCoreRestServices.loadExperiment(experimentUrl)
                            .success(function (data, status, headers, config) {
                                $scope.experimentContext = data;
                                $log.info("Experiment Details Fetched Successfully.");
                            })
                            .error(function (data, status, headers, config) {
                                $log.info("Experiment Details can't be Fetched.");
                            });

                }

                _initScope();
            }])
        .directive('modal', function () {
            return {
                template: '<div class="modal fade">' +
                        '<div class="modal-dialog modal-md">' +
                        '<div class="modal-content">' +
                        '<div class="modal-header">' +
                        '<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>' +
                        '<h2 class="modal-title">{{ title }}</h2>' +
                        '</div>' +
                        '<div class="modal-body" ng-transclude></div>' +
                        '</div>' +
                        '</div>' +
                        '</div>',
                restrict: 'E',
                transclude: true,
                replace: true,
                scope: true,
                link: function postLink(scope, element, attrs) {
                    scope.title = attrs.title;

                    scope.$watch(attrs.visible, function (value) {
                        if (value == true)
                            $(element).modal('show');
                        else
                            $(element).modal('hide');
                    });

                    $(element).on('shown.bs.modal', function () {
                        scope.$apply(function () {
                            scope.$parent[attrs.visible] = true;
                        });
                    });

                    $(element).on('hidden.bs.modal', function () {
                        scope.$apply(function () {
                            scope.$parent[attrs.visible] = false;
                        });
                    });
                }
            };
        });
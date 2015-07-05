'use strict';

angular.module('karamel.main')
        .factory('ModalService', ['$modal', function ($modal) {
                return {
                    confirm: function (size, title, msg) {
                        var modalInstance = $modal.open({
                            templateUrl: 'karamel/partials/confirmModal.html',
                            controller: 'ModalCtrl as ctrl',
                            size: size,
                            resolve: {
                                title: function () {
                                    return title;
                                },
                                msg: function () {
                                    return msg;
                                }
                            }
                        });
                        return modalInstance.result;
                    },
                    profile: function (size) {
                        var modalInstance = $modal.open({
                            templateUrl: 'karamel/partials/profile.html',
                            controller: 'ProfileCtrl as profileCtrl',
                            size: size,
                            resolve: {
 
                            }
                        });
                        return modalInstance.result;
                    },
                    experimentFactory: function (size, experiment) {
                        var modalInstance = $modal.open({
                            templateUrl: 'karamel/partials/experiment-factory.html',
                            controller: 'NewExperimentCtrl as newExperimentCtrl',
                            size: size,
                            windowClass: 'app-modal-window',
                            resolve: {
                                 // return experiment context details - needed to fill in user/url/etc
//                                repoName: function () {
//                                    return githubService.repo.name;
//                                },
//                                orgName: function () {
//                                    return githubService.org.name;
//                                }
                                experiment: function () {
                                    return experiment;
                                }

                            }
                        });
                        return modalInstance.result;
                    }
                }

            }]);

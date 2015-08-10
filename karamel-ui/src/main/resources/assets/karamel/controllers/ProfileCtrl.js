'use strict'

angular.module('karamel.main')
        .controller('ProfileCtrl', ['$scope', '$log', '$modalInstance', 'KaramelCoreRestServices', 'GithubService',
            function ($scope, $log, $modalInstance, KaramelCoreRestServices, GithubService) {
                var self = this;
                self.isLoggedIn = false;
                self.isOrgSelected = false;
                self.isRepoSelected = false;

                self.user = '';
                self.password = '';
                $scope.githubService = GithubService;

                self.load = function () {
                    GithubService.loadExperiment(self.experimentUrl);
                };

                self.profile = function () {
                    GithubService.getCredentials();
                    self.user = GithubService.getUser();
                    if (self.user !== "") {
                        self.isLoggedIn = true;
                    }
                };

                self.login = function () {
                    var credentials = GithubService.setCredentials(self.user, self.password);
                    if (credentials.email !== "") {
                        self.isLoggedIn = true;
                    }
                };


                self.setRepo = function () {
                    var ret = GithubService.setRepo(self.repo);
                    if (ret !== null) {
                        self.isRepoSelected = true;
                    }
                };

                self.getRepo = function () {
                    $scope.repo = GithubService.getRepo();
                };


                self.getRepos = function () {
                    return GithubService.getRepos();
                };

                $scope.githubUser = function () {
                    return self.user;
                };

                self.close = function () {
                    $modalInstance.dismiss('cancel');
                };

                self.reset = function () {
                    return null;
                };

                self.getEmailHash = function () {
                    return GithubService.getEmailHash();
                };

                self.profile();
            }]);

'use strict'

angular.module('karamel.main')
        .controller('ProfileCtrl', ['$scope', '$log', '$modalInstance', 'GithubService',
            function ($scope, $log, $modalInstance, GithubService) {
                var self = this;
                self.isLoggedIn = false;
                self.isOrgSelected = false;
                self.isRepoSelected = false;
                
                self.user = '';
                self.password = '';
                self.org = '';
                self.repo = '';

                self.profile = function () {
                    GithubService.getCredentials();
                    self.user = GithubService.getUser();
                };
                
                
                self.login = function () {
                    var credentials = GithubService.setCredentials(self.user, self.password);
                    if (credentials.email !== "") {
                        self.isLoggedIn = true;
                    }
                };
                
                self.setOrg = function () {
                    var ret = GithubService.setOrg(self.org);
                    if (ret !== null) {
                        self.isOrgSelected = true;
                    }
                };

                self.getOrg = function () {
                    return GithubService.getOrg();
                };

                self.getOrgs = function () {
                    return GithubService.getOrgs();
                };

                self.setRepo = function () {
                    var ret = GithubService.setRepo(self.repo);
                    if (ret !== null) {
                        self.isRepoSelected = true;
                    }                    
                };
                
                self.getRepo = function () {
                    return GithubService.getRepo();
                };

                self.getRepos = function () {
                    return GithubService.getRepos();
                };

                $scope.githubUser = function() {
                    return self.user;
                }

                self.close = function () {
                    $modalInstance.dismiss('cancel');
                };
                
                self.reset = function () {
                    return null;
                };
                
                self.getEmailHash = function() {
                    return GithubService.getEmailHash();
                }
                
                self.profile();
            }]);

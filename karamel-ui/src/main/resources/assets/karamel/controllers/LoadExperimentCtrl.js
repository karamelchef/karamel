'use strict'

angular.module('karamel.main')
        .controller('LoadExperimentCtrl', ['$scope', '$log', '$modalInstance', 'GithubService',
            function ($scope, $log, $modalInstance, GithubService) {
                var self = this;

                $scope.github = GithubService;

                $scope.githubUrl = ""

                self.selectUserOrg = function () {
                    $scope.github.org.name = $scope.github.githubCredentials.user;
                    $scope.github.repos = [];
                    $scope.github.getRepos();
                }
                self.selectOrg = function (name) {
                    GithubService.setOrg(name);
                };
                
                self.selectRepo = function (name) {
                    GithubService.setRepo(name);
                    $scope.github.repo.sshUrl = "https://github.com/" + $scope.github.org.name + "/" + name + ".git";
                    $scope.githubUrl = "https://github.com/" + $scope.github.org.name + "/" + name + ".git";
                };

                self.close = function () {
                    $modalInstance.close($scope.githubUrl);
                };

                self.cancel = function () {
                    $modalInstance.dismiss('cancel');
                };

                self.reset = function () {
                    return null;
                };

                function _initScope() {
                    GithubService.getOrgs();
                    $scope.github.org.name = $scope.github.githubCredentials.user;
                    self.selectUserOrg();
                }
                
                _initScope();
                
            }]);

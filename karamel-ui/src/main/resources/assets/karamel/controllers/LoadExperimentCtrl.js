'use strict'

angular.module('karamel.main')
        .controller('LoadExperimentCtrl', ['$scope', '$log', '$modalInstance', 'GithubService',
            function ($scope, $log, $modalInstance, GithubService) {
                var self = this;

                $scope.github = GithubService;

                $scope.submitted = true;
                $scope.submittedOngoing = false;
                $scope.submittedMsg = "";

                $scope.repoName = "";

                $scope.githubUrl = ""
                
                $scope.githubDetails = {
                    user: '',
                    group: '',
                    githubRepo: '',
                    githubOwner: '',
                    description: '',
                    urlGitClone: ''
                };
                
                
                self.selectUserOrg = function () {
                    $scope.github.org.name = $scope.github.githubCredentials.user;
                    $scope.github.repos = [];
                    $scope.github.getRepos();
                }
                self.selectOrg = function (name) {
                    GithubService.setOrg(name);
                    $scope.repoName = "";
                    $scope.githubDetails.githubRepo = "";
                    $scope.githubDetails.urlGitClone = "";
                    $scope.githubDetails.githubOwner = GithubService.org.name;
                };
                
                self.selectRepo = function (name) {
                    GithubService.setRepo(name);
                    $scope.repoName = name;
                    $scope.githubDetails.githubRepo = name;
                    $scope.githubDetails.urlGitClone = "";
                    $scope.githubDetails.githubOwner = GithubService.org.name;
                    
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


                self.getOrgs = function () {
                    GithubService.getOrgs();
                }

                function _initScope() {
                    self.getOrgs();
                    $scope.github.org.name = $scope.github.githubCredentials.user;
                    self.selectUserOrg();
                }

                
                
                _initScope();
                
            }]);

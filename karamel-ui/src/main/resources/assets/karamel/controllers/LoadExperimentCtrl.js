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

                $scope.githubDetails = {
                    user: '',
                    group: '',
                    githubRepo: '',
                    githubOwner: '',
                    description: '',
                    urlGitClone: ''
                };
                
                
                self.selectOrg = function (name) {
                    GithubService.setOrg(name);
                    $scope.repoName = "";
                    $scope.githubDetails.githubRepo = "";
                    $scope.githubDetails.urlGitClone = "";
                    $scope.githubDetails.githubOwner = GithubService.org.name;
                };

                self.close = function (feedback) {
                    $modalInstance.close(feedback);
                };

                self.cancel = function () {
                    $modalInstance.dismiss('cancel');
                };

                self.reset = function () {
                    return null;
                };

                self.loadExperiment = function () {
                    $scope.submittedOngoing = true;
                    $scope.submitted = true;
                    $scope.submittedMsg = "";
                    GithubService.getRepos($scope.github.org.name).then(
                            function () {
                                // Check if the repository already exists for this user or organization
                                for (var i = 0; i < $scope.github.repos.length; i++) {
                                    if ($scope.github.repos[i].name.localeCompare($scope.repoName) === 0) {
                                        $scope.submittedMsg = "Repo already exists!";
                                        $scope.submittedOngoing = false;
                                        return;
                                    }
                                }
                                $log.info("new experiment being created...");
                                GithubService.newRepo($scope.repoName, $scope.repoDesc);
                                self.githubDetails.user = $scope.github.repo.name;
                                self.githubDetails.group = $scope.github.repo.name;
                                self.githubDetails.githubRepo = $scope.github.repo.name;
                                self.githubDetails.githubOwner = $scope.github.org.name;
                                self.githubDetails.description = $scope.github.repo.description;
                                self.githubDetails.urlGitClone = $scope.github.repo.url;
                                $scope.submittedMsg = "Repo doesn't exist yet.";
                                $scope.submittedOngoing = false;
                                self.close(self.githubDetails);
                            }
                    );
                };

                self.getOrgs = function () {
                    GithubService.getOrgs();
                }

                self.getOrgs();
            }]);

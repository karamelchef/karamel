'use strict'

angular.module('karamel.main')
        .controller('NewExperimentCtrl', ['$scope', '$log', '$modalInstance', 'GithubService',
            function ($scope, $log, $modalInstance, GithubService) {
                var self = this;

                $scope.github = GithubService;

                $scope.submitted = true;
                $scope.submittedOngoing = false;
                $scope.submittedMsg = "";

                self.selectOrg = function (name) {
                    GithubService.setOrg(name);
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

                self.newExperiment = function () {
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
                                self.experimentContext.url = "git@github.com:" + $scope.orgName
                                        + "/" + $scope.repoName + ".git";
                                self.experimentContext.user = $scope.github.repo.name;
                                self.experimentContext.group = $scope.github.repo.name;
                                self.experimentContext.githubRepo = $scope.github.repo.name;
                                self.experimentContext.githubOwner = $scope.github.org.name;
                                $scope.submittedMsg = "Repo doesn't exist yet.";
                                $scope.submittedOngoing = false;
                                self.close(self.experimentContext);
                            }
                    );
                };

                self.getOrgs = function () {
                    GithubService.getOrgs();
                }

                self.experimentContext = {
                    url: '',
                    user: '',
                    group: '',
                    githubRepo: '',
                    githubOwner: '',
                    experimentContext: [
                        {scriptContents: '',
                            defaultAttributes: '',
                            preScriptChefCode: '',
                            scriptType: 'bash'
                        }
                    ]
                };

                $scope.experiment = self.experimentContext;

                self.getOrgs();
            }]);

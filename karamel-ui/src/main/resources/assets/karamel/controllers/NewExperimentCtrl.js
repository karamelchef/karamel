'use strict'

angular.module('karamel.main')
        .controller('NewExperimentCtrl', ['$scope', '$log', '$modalInstance', 'GithubService', 
            function ($scope, $log, $modalInstance, GithubService) {
                var self = this;

                $scope.orgs = {};

                self.getOrgs = function () {
                    $scope.orgs = GithubService.getOrgs();
                };
                
                $scope.user = GithubService.githubCredentials.user;
                
                $scope.email = GithubService.githubCredentials.email;
                
                $scope.orgName = GithubService.org.name;
                
                $scope.repoName = GithubService.repo.name;
                
                $scope.repoDesc = GithubService.repo.description;
                
                
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

                    $log.info("new experiment executed ...");
                    GithubService.newRepo($scope.repoName, $scope.repoDesc);
                    self.experimentContext.url = "git@github.com:" + $scope.orgName 
                            + "/" + $scope.repoName + ".git";
                    self.experimentContext.user = $scope.repoName;
                    self.experimentContext.group = $scope.repoName;
                    self.experimentContext.githubRepo = $scope.repoName;
                    self.experimentContext.githubOwner = $scope.orgName;
                    self.close(self.experimentContext);
                };

                
                self.experimentContext = {
                    url: '',
                    user: '',
                    group: '',
                    githubRepo: '',
                    githubOwner: '',
                    experimentContext: [
                        {   scriptContents: '',
                            defaultAttributes: '',
                            preScriptChefCode: '',
                            scriptType: 'bash'
                        }
                    ]
                };
                
                $scope.experiment = self.experimentContext;

            }]);

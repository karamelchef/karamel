'use strict'

angular.module('karamel.main')
        .controller('NewExperimentCtrl', ['$scope', '$log', '$modalInstance', 'GithubService', 'experiment',
            function ($scope, $log, $modalInstance, GithubService, experiment) {
                var self = this;

                $scope.githubService = GithubService;
                $scope.orgs = {};
                $scope.githubUser= "";
                $scope.orgName = "";
                $scope.repoName = "";
                $scope.repoDesc = "";

                self.setOrg = function (name) {
                    GithubService.setOrg(name);
                };

                self.getOrgs = function () {
                    $scope.orgs = GithubService.getOrgs();
                };
                
                self.getUser = function () {
                    $scope.githubUser = GithubService.getUser();
                };
                
                self.getOrgName = function () {
                    $scope.orgName = GithubService.getOrgName();
                };
                
                self.getRepoName = function () {
                    $scope.repoName = GithubService.getRepoName();
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

                    $log.info("new experiment executed ...");
                    GithubService.newRepo(self.repoName, self.repoDesc);
                    self.experiment.url = "git@github.com:" + self.orgName 
                            + "/" + $scope.repoName + ".git";
                    self.experiment.user = self.repoName;
                    self.experiment.group = self.repoName;
                    self.experiment.githubRepo = self.repoName;
                    self.experiment.githubOwner = self.orgName;
                    self.close(self.experiment);
                };

                self.selectOrg = function (name) {
                    GithubService.setOrg(name);
                }


                self.getOrgs();
                self.getOrgName();
                self.getRepoName();
                
                self.experiment = {
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

            }]);

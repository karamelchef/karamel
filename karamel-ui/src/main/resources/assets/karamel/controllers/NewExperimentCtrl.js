'use strict'

angular.module('karamel.main')
        .controller('NewExperimentCtrl', ['$scope', '$log', '$modalInstance', 'GithubService',
            function ($scope, $log, $modalInstance, GithubService) {
                var self = this;

                $scope.githubRef = GithubService;
                $scope.orgs = {};
                $scope.githubUser= "";
                $scope.orgName = "";
                $scope.repoName = "";

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

                self.close = function () {
                    $modalInstance.dismiss('cancel');
                };

                self.reset = function () {
                    return null;
                };

                self.newExperiment = function (name, description) {
                    $scope.githubRef.repo.name = name;
                    $scope.githubRef.repo.description = description;
                    self.experiment.url = "git@github.com:" + self.orgName 
                            + "/" + $scope.repoName + ".git";
                    self.experiment.user = self.repoName;
                    self.experiment.group = self.repoName;
                    self.experiment.githubRepo = self.repoName;
                    self.experiment.githubOwner = self.orgName;
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

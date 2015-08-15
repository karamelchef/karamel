'use strict'

angular.module('main.module')
    .controller('load.experiment.controller', ['$scope', '$log', '$modalInstance', 'github.service',
      function ($scope, $log, $modalInstance, githubService) {
        var self = this;

        $scope.github = githubService;

        $scope.githubUrl = ""

        self.selectUserOrg = function () {
          $scope.github.org.name = $scope.github.githubCredentials.user;
          $scope.github.repos = [];
          $scope.github.getRepos();
        }
        self.selectOrg = function (name) {
          githubService.setOrg(name);
        };

        self.selectRepo = function (name) {
          githubService.setRepo(name);
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
          githubService.getOrgs();
          $scope.github.org.name = $scope.github.githubCredentials.user;
          self.selectUserOrg();
        }

        _initScope();

      }]);

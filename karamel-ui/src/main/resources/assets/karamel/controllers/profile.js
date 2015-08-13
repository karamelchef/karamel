'use strict'

angular.module('karamel.main')
    .controller('ProfileCtrl', ['$scope', '$log', '$modalInstance', 'KaramelCoreRestServices', 'GithubService',
      function ($scope, $log, $modalInstance, KaramelCoreRestServices, GithubService) {
        var self = this;
        $scope.isLoggedIn = false;

        $scope.github = GithubService;

        self.profile = function () {
          GithubService.getCredentials();
          if ($scope.github.githubCredentials.email !== "") {
            $scope.isLoggedIn = true;
          }
        };

        self.login = function () {
          KaramelCoreRestServices.setGithubCredentials($scope.github.githubCredentials.user,
              $scope.github.githubCredentials.password)
              .success(function (data, status, headers, config) {
                $scope.github.githubCredentials.user = data.user;
                $scope.github.githubCredentials.password = data.password;
                $scope.github.githubCredentials.email = data.email;
                $scope.github.org.name = data.user;
                $scope.github.generateEmailHash();
                if (data.email !== "") {
                  $scope.isLoggedIn = true;
                  self.close();
                }
              })
              .error(function (data, status, headers, config) {
                self.errorMsg = error.data.errorMsg;
                $log.error("Github Credentials can't be Registered .");
              });


        };

        self.close = function () {
          $modalInstance.close();
        };

        $scope.getEmailHash = function () {
          return GithubService.getEmailHash();
        };

        self.profile();
      }]);

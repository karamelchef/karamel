'use strict'

angular.module('karamel.main')
    .controller('ProfileCtrl', ['$scope', 'md5', '$log', '$modalInstance', 'KaramelCoreRestServices', 'GithubService',
      function($scope, md5, $log, $modalInstance, KaramelCoreRestServices, GithubService) {
        var self = this;
        $scope.isLoggedIn = false;

        $scope.github = GithubService;

        self.profile = function() {
          GithubService.getCredentials();
          if ($scope.github.githubCredentials.email !== "") {
            $scope.isLoggedIn = true;
          }
        };

        self.login = function() {
          KaramelCoreRestServices.setGithubCredentials($scope.github.githubCredentials.user,
              $scope.github.githubCredentials.password)
              .then(function(data, status, headers, config) {
                $scope.github.githubCredentials.user = data.user;
                $scope.github.githubCredentials.password = data.password;
                $scope.github.githubCredentials.email = data.email;
                $scope.github.org.name = data.user;
                $scope.github.emailHash = md5.createHash(data.email || '');
                if (data.email !== "") {
                  $scope.isLoggedIn = true;
                  self.close();
                }
              })
              .error(function(data, status, headers, config) {
                self.errorMsg = error.data.errorMsg;
                $log.error("Github Credentials can't be Registered .");
              });


        };

        self.close = function() {
          $modalInstance.close($scope.isLoggedIn);
        };

        $scope.getEmailHash = function() {
          return GithubService.getEmailHash();
        };

        self.profile();
      }]);

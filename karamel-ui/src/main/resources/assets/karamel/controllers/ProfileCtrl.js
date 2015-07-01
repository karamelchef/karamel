'use strict'

angular.module('karamel.main')
        .controller('ProfileCtrl', ['$scope', '$log', '$modalInstance', 'GithubService',
            function ($scope, $log, $modalInstance, GithubService) {
                var self = this;
                self.user = '';
                self.password = '';

                self.profile = function () {
                    GithubService.getCredentials();
                };

                self.login = function () {
                    GithubService.setCredentials(self.user, self.password);

                };

                self.close = function () {
                    $modalInstance.dismiss('cancel');
                };
                
                self.reset = function () {
                    return null;
                };
                
                self.getEmailHash = function() {
                    return GithubService.getEmailHash();
                }
            }]);

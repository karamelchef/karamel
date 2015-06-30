'use strict'

angular.module('karamel.main')
        .controller('ProfileCtrl', ['$location', '$scope', 'md5', 'growl', '$modalInstance', 'KaramelCoreRestServices',
            function ($location, $scope, md5, growl, $modalInstance, KaramelCoreRestServices) {

                var self = this;

                self.emailHash = '';

                self.master = {};

                self.githubCredentials = {
                    email: '',
                    password: ''
                };


                self.profile = function () {
                    KaramelCoreRestServices.getGithubCredentials().then(
                            function success() {
                                self.githubCredentials = success.data;
                                self.emailHash = md5.createHash(self.user.email || '');
                                self.master = angular.copy(self.githubCredentials);
                            },
                            function (error) {
                                self.errorMsg = error.data.errorMsg;
                            })
                };

                self.profile();

                self.login = function (email, password) {
                    KaramelCoreRestServices.setGibhubCredentials(email, password)
                            .success(function (data, status, headers, config) {
                                self.githubCredentials = success.data;
                                self.master = angular.copy(self.githubCredentials);
                                $log.info("Github Credentials Registered Successfully.");
                            })
                            .error(function (data, status, headers, config) {
                        self.errorMsg = error.data.errorMsg;
                            growl.error("Could not login to github.", {title: 'Error', ttl: 5000});
                                $log.info("Github Credentials can't be Registered .");
                            });

                };


                self.reset = function () {
                    self.githubCredentials = angular.copy(self.master);
                    $scope.githubCredentialsForm.$setPristine();
                };

                self.close = function () {
                    $modalInstance.dismiss('cancel');
                };

            }]);

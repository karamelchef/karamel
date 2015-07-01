'use strict';

angular.module('karamel.terminal')
        .factory('GithubService', ['$log', 'md5', 'KaramelCoreRestServices', function ($log, md5, KaramelCoreRestServices) {

                var self = this;

                self.emailHash = '';

                self.githubCredentials = {
                    user: '',
                    email: '',
                    password: ''
                };

                return {
                    getEmailHash: function () {
                        return self.emailHash;
                    },
                    setCredentials: function (user, password) {
                        $log.info("Github Credentials set: " + user + " - " + email + " - " + password);

                        KaramelCoreRestServices.setGibhubCredentials(user, password)
                                .success(function (data, status, headers, config) {
                                    self.githubCredentials.user = data.user;
                                    self.githubCredentials.password = data.password;
                                    self.githubCredentials.email = data.email;
                                    self.emailHash = md5.createHash(self.githubCredentials.email || '');
                                    $log.info("Github Credentials Registered Successfully.");
                                })
                                .error(function (data, status, headers, config) {
                                    self.errorMsg = error.data.errorMsg;
//                            growl.error("Could not login to github.", {title: 'Error', ttl: 5000});
                                    $log.info("Github Credentials can't be Registered .");
                                });

                        return self.githubCredentials;
                    },
                    getCredentials: function () {
                        KaramelCoreRestServices.getGithubCredentials()
                                .success(function (data, status, headers, config) {
                                    self.githubCredentials.user = data.user;
                                    self.githubCredentials.password = data.password;
                                    self.githubCredentials.email = data.email;
                                    self.emailHash = md5.createHash(self.githubCredentials.email || '');
                                    $log.info("GitHub Credentials found: " + data.user + " - " + data.password + " - " + data.email);
                                })
                                .error(function (data, status, headers, config) {
                                    $log.info("GitHub Credentials not found.");
                                });
                        return self.githubCredentials;
                    },
                    getEmail: function () {
                        return self.githubCredentials.email;
                    },
                    getPassword: function () {
                        return self.githubCredentials.password;
                    },
                    getUser: function () {
                        return self.githubCredentials.user;
                    }
                }

            }]);

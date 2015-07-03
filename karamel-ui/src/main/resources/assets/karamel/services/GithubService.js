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

                self.orgs = [];
//                self.orgs = {};

                self.repos = {};

                self.org = {
                    name: '',
                    gravitar: ''
                };

                self.repo = {
                    name: '',
                    description: '',
                    sshUrl: ''
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
                    getOrgs: function () {
                        KaramelCoreRestServices.getGithubOrgs()
                                .success(function (data, status, headers, config) {
                                    $log.info("GitHub Orgs found: " + data.length);
                            for (var i = 0, len = data.length; i < len; i++) {
//                                self.orgs[i].name = data[i].name;
//                                self.orgs[i].gravitar = data[i].gravitar;

                                $log.info("GitHub Org: " + data[i]);
                                $log.info("GitHub Org name: " + data[i].name);
                                $log.info("GitHub Org name: " + data[i].gravitar);
                                self.orgs[i] = {
                                  name: data[i].name,
                                  gravitar: data[i].gravitar
                                };
                            }
//                                    self.orgs = data.orgs;
                                })
                                .error(function (data, status, headers, config) {
                                    $log.info("GitHub Orgs not found.");
                                });
                        return self.orgs;
                    },
                    getRepos: function (org) {
                        KaramelCoreRestServices.getGithubRepos(self.org)
                                .success(function (data, status, headers, config) {
                                    self.repos = data.repos;
                                })
                                .error(function (data, status, headers, config) {
                                    $log.info("GitHub Orgs not found.");
                                });
                        return self.repos;
                    },
                    setOrg: function (org) {
                        if (self.orgs !== null) {
                            for (var i = 0, len = self.orgs.length; i < len; i++) {
                                if (self.orgs[i].name === org) {
                                    self.org.name = self.orgs[i].name;
                                    self.org.gravitar = self.orgs[i].gravitar;
                                    break;
                                }
                            }
                        }
                        return null;
                    },
                    setRepo: function (repo) {
                        if (self.repos !== null) {
                            for (var i = 0, len = self.repos.length; i < len; i++) {
                                if (self.repos[i].name === repo) {
                                    self.repo.name = self.repos[i].name;
                                    self.repo.description = self.repos[i].description;
                                    self.repo.sshUrl = self.repos[i].sshUrl;
                                    break;
                                }
                            }
                        }
                        return null;
                    },

                    createRepo: function (repo, description) {

                        KaramelCoreRestServices.createGithubRepo(self.org.name, repo, description)
                                .success(function (data, status, headers, config) {
                                    self.repo.name = data.name;
                                    self.repo.description = data.description;
                                    self.repo.sslUrl = data.sslUrl;
                                    $log.info("GitHub Repo created");
                                })
                                .error(function (data, status, headers, config) {
                                    $log.info("GitHub Credentials not found.");
                                });
                        return self.githubCredentials;
                    },
                    getOrg: function () {
                        return self.org;
                    },
                    getRepo: function () {
                        return self.repo;
                    },
                    getOrgName: function () {
                        return self.org.name;
                    },
                    getOrgGravitar: function () {
                        return self.org.name.gravitar;
                    },
                    getRepoName: function () {
                        return self.repo.name;
                    },
                    getRepoDescription: function () {
                        return self.repo.description;
                    },
                    getRepoSshUrl: function () {
                        return self.repo.sshUrl;
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

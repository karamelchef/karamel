'use strict';

angular.module('karamel.terminal')
        .service('GithubService', ['$log', 'md5', 'KaramelCoreRestServices', function ($log, md5, KaramelCoreRestServices) {

                var self = this;

                self.emailHash = '';

                self.githubCredentials = {
                    user: '',
                    email: '',
                    password: ''
                };

                self.orgs = [];

                self.repos = [];

                self.org = {
                    name: '',
                    gravitar: ''
                };

                self.repo = {
                    name: '',
                    description: '',
                    sshUrl: ''
                };



                self.setCredentials = function (user, password) {

                    KaramelCoreRestServices.setGibhubCredentials(user, password)
                            .success(function (data, status, headers, config) {
                                self.githubCredentials.user = data.user;
                                self.githubCredentials.password = data.password;
                                self.githubCredentials.email = data.email;
                                self.org.name = data.user;
                                self.emailHash = md5.createHash(self.githubCredentials.email || '');
                                $log.info("Github Credentials Registered Successfully.");
                            })
                            .error(function (data, status, headers, config) {
                                self.errorMsg = error.data.errorMsg;
//                            growl.error("Could not login to github.", {title: 'Error', ttl: 5000});
                                $log.error("Github Credentials can't be Registered .");
                            });

                    return self.githubCredentials;
                };

                self.getCredentials = function () {
                    KaramelCoreRestServices.getGithubCredentials()
                            .success(function (data, status, headers, config) {
                                self.githubCredentials.user = data.user;
                                self.githubCredentials.password = data.password;
                                self.githubCredentials.email = data.email;
                                self.emailHash = md5.createHash(self.githubCredentials.email || '');
                                self.org.name = data.user;
                            })
                            .error(function (data, status, headers, config) {
                                $log.warn("GitHub Credentials not found.");
                            });
                    return self.githubCredentials;
                };
                self.getOrgs = function () {
                    KaramelCoreRestServices.getGithubOrgs()
                            .success(function (data, status, headers, config) {
                                for (var i = 0, len = data.length; i < len; i++) {
                                    self.orgs[i] = {
                                        name: data[i].name,
                                        gravitar: data[i].gravitar
                                    };
                                }
                            })
                            .error(function (data, status, headers, config) {
                                $log.warn("GitHub Orgs not found.");
                            });
                    return self.orgs;
                };
                self.getRepos = function (org) {
                    return KaramelCoreRestServices.getGithubRepos(self.org.name)
                            .success(function (data, status, headers, config) {
                                $log.info("GitHub Repos found: " + data.length);
                                for (var i = 0, len = data.length; i < len; i++) {
                                    $log.info("GitHub Repo name: " + data[i].name);
                                    $log.info("GitHub Repo url: " + data[i].sshUrl);
                                    self.repos[i] = {
                                        name: data[i].name,
                                        description: "",
                                        sshUrl: data[i].sshUrl
                                    };
                                }                                
                                
                            })
                            .error(function (data, status, headers, config) {
                                $log.info("GitHub Orgs not found.");
                            });
                };
                self.setOrg = function (org) {
                    if (self.orgs !== null) {
                        for (var i = 0, len = self.orgs.length; i < len; i++) {
                            if (self.orgs[i].name === org) {
                                self.org.name = self.orgs[i].name;
                                self.org.gravitar = self.orgs[i].gravitar;
                                break;
                            }
                        }
                    }
                    self.org.name = org;
                    self.org.gravitar = "";
                };
                self.newRepo = function (repoName, description) {
                    self.repo.name = repoName;
                    self.repo.description = description;
                    self.repo.sshUrl = "git@github.com:" + self.org.name + "/" + repoName + ".git";
                };
                self.getOrg = function () {
                    return self.org;
                };
                self.getRepo = function () {
                    return self.repo;
                };
                self.getOrgName = function () {
                    return self.org.name;
                };
                self.getOrgGravitar = function () {
                    return self.org.name.gravitar;
                };
                self.getRepoName = function () {
                    return self.repo.name;
                };
                self.getRepoDescription = function () {
                    return self.repo.description;
                };
                self.getRepoSshUrl = function () {
                    return self.repo.sshUrl;
                };
                self.getEmail = function () {
                    return self.githubCredentials.email;
                };
                self.getEmailHash = function () {
                    return self.emailHash;
                };
                self.getPassword = function () {
                    return self.githubCredentials.password;
                };
                self.getUser = function () {
                    return self.githubCredentials.user;
                };
                self.setOrgName = function (name) {
                    self.org.name = name;
                };


            }]);

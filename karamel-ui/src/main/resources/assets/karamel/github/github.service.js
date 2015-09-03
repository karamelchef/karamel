'use strict';

angular.module('main.module')
  .service('github.service', ['$log', 'md5', 'core-rest.service', function($log, md5, coreService) {

      var self = this;

      self.emailHash = '';

      self.githubCredentials = {
        user: '',
        email: '',
        password: ''
      };

      self.orgs = [];

      self.repos = [];

//      self.reposSelected = [];
//      self.reposSelected[0] = {
//        name: '',
//        ticked: false
//      };

      self.org = {
        name: '',
        gravitar: ''
      };

      self.repo = {
        name: '',
        description: '',
        sshUrl: ''
      };



      self.getCredentials = function() {
        coreService.getGithubCredentials()
          .success(function(data, status, headers, config) {
            self.githubCredentials.user = data.user;
            self.githubCredentials.password = data.password;
            self.githubCredentials.email = data.email;
            self.emailHash = md5.createHash(self.githubCredentials.email || '');
            self.org.name = data.user;
          })
          .error(function(data, status, headers, config) {
            $log.warn("GitHub Credentials not found.");
          });
        return self.githubCredentials;
      };

      self.generateEmailHash = function() {
        self.emailHash = md5.createHash(self.githubCredentials.email || '');
      }

      self.getOrgs = function() {
        coreService.getGithubOrgs()
          .success(function(data, status, headers, config) {
            for (var i = 0, len = data.length; i < len; i++) {
              self.orgs[i] = {
                name: data[i].name,
                gravitar: data[i].gravitar
              };
            }
          })
          .error(function(data, status, headers, config) {
            $log.warn("GitHub Orgs not found.");
          });
      };
      self.getRepos = function() {
        return coreService.getGithubRepos(self.org.name)
          .success(function(data, status, headers, config) {
            $log.info("GitHub Repos found: " + data.length);
            for (var i = 0, len = data.length; i < len; i++) {
              $log.info("GitHub Repo name: " + data[i].name);
              $log.info("GitHub Repo url: " + data[i].sshUrl);
              self.repos[i] = {
                name: data[i].name,
                description: "",
                sshUrl: data[i].sshUrl,
                ticked: false
              };
            }

          })
          .error(function(data, status, headers, config) {
            $log.info("GitHub Orgs not found.");
          });
      };
      self.setOrg = function(org) {
        if (self.orgs !== null) {
          for (var i = 0, len = self.orgs.length; i < len; i++) {
            if (self.orgs[i].name === org) {
              self.org.name = self.orgs[i].name;
              self.org.gravitar = self.orgs[i].gravitar;
              self.repos = [];
              self.getRepos();
              break;
            }
          }
        }
        self.org.name = org;
        self.org.gravitar = "";

      };

      self.setRepo = function(repo) {
        if (self.repos !== null) {
          for (var i = 0, len = self.repos.length; i < len; i++) {
            if (self.repos[i].name === repo) {
              self.repo.name = self.repos[i].name;
              break;
            }
          }
        }
        self.repo.name = repo;
      };

      self.setTickedRepo = function() {
        if (self.repos !== null) {
          for (var i = 0, len = self.repos.length; i < len; i++) {
<<<<<<< HEAD
            if (self.repos[i].ticked === true) {
=======
            if (self.repos[i].ticked ===
                true) {
>>>>>>> 52e7dbd65e1790dc0c29b8158baa437a8ff2ec39
              self.repo.name = self.repos[i].name;
              break;
            }
          }
        }
      };


      self.newRepo = function(repoName, description) {
        self.repo.name = repoName;
        self.repo.description = description;
        self.repo.sshUrl = "https://github.com:" + self.org.name + "/" + repoName + ".git";
      };
      self.getOrg = function() {
        return self.org;
      };
      self.getRepo = function() {
        return self.repo;
      };
      self.getOrgName = function() {
        return self.org.name;
      };
      self.getOrgGravitar = function() {
        return self.org.name.gravitar;
      };
      self.getRepoName = function() {
        return self.repo.name;
      };
      self.getRepoDescription = function() {
        return self.repo.description;
      };
      self.getRepoSshUrl = function() {
        return self.repo.sshUrl;
      };
      self.getEmail = function() {
        return self.githubCredentials.email;
      };
      self.getEmailHash = function() {
        return self.emailHash;
      };
      self.getPassword = function() {
        return self.githubCredentials.password;
      };
      self.getUser = function() {
        return self.githubCredentials.user;
      };
      self.setOrgName = function(name) {
        self.org.name = name;
      };


    }]);

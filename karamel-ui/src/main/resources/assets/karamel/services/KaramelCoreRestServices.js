  angular.module('karamel.terminal')
          .service('KaramelCoreRestServices', ['$log', '$http', '$location', function($log, $http, $location) {

      // Return the promise object to the users.
      var _getPromiseObject = function(method, url, contentType, data, isArray) {

        isArray = typeof isArray !== 'undefined' ? isArray : false;
        
        var promiseObject = $http({
          method: method,
          url: url,
          headers: {'Content-Type': contentType},
          isArray: isArray,
          data: data
        });

        return promiseObject;
      };
      

      /* window.location.hostname for the webserver  */

      var _defaultHost = 'http://' + $location.host() + ':9090/api';
      var _defaultContentType = 'application/json';


      // Services interacting with the karamel core.
      return{
        // Based on the object passed get the complete url.
        getCompleteYaml: function(json) {

          var method = 'PUT';
          var url = _defaultHost.concat("/fetchYaml");

          return _getPromiseObject(method, url, _defaultContentType, json);

        },
        getCleanYaml: function(json) {

          var method = 'PUT';
          var url = _defaultHost.concat("/cleanYaml");

          return  _getPromiseObject(method, url, _defaultContentType, json);

        },
        getJsonFromYaml: function(ymlString) {

          var method = 'PUT';
          var url = _defaultHost.concat("/fetchJson");

          return _getPromiseObject(method, url, _defaultContentType, ymlString);


        },
        getCookBookInfo: function(requestData) {

          var method = 'PUT';
          var url = _defaultHost.concat("/fetchCookbook");
          return _getPromiseObject(method, url, _defaultContentType, requestData);
        },
        loadSshKeys: function() {
          var method = 'PUT';
          var url = _defaultHost.concat("/loadSshKeys");
          return _getPromiseObject(method, url, _defaultContentType);
        },
        registerSshKeys: function(sshKeypair) {
          var method = 'PUT';
          var url = _defaultHost.concat("/registerSshKeys");
          return _getPromiseObject(method, url, _defaultContentType, sshKeypair);
        },
        generateSshKeys: function() {
          var method = 'PUT';
          var url = _defaultHost.concat("/generateSshKeys");
          return _getPromiseObject(method, url, _defaultContentType);
        },
        loadCredentials: function() {
          var method = 'PUT';
          var url = _defaultHost.concat("/loadCredentials");
          return _getPromiseObject(method, url, _defaultContentType);
        },
        validateCredentials: function(providerInfo) {
          var method = 'PUT';
          var url = _defaultHost.concat("/validateCredentials");
          return _getPromiseObject(method, url, _defaultContentType, providerInfo);
        },
        startCluster: function(clusterJson) {
          var method = 'PUT';
          var url = _defaultHost.concat("/startCluster");
          return _getPromiseObject(method, url, _defaultContentType, clusterJson);
        },
        viewCluster: function(clusterNameJson) {
          var method = 'PUT';
          var url = _defaultHost.concat("/viewCluster");
          return _getPromiseObject(method, url, _defaultContentType, clusterNameJson);
        },
        pauseCluster: function(clusterName) {
          var method = 'PUT';
          var url = _defaultHost.concat("/pauseCluster");
          return _getPromiseObject(method, url, _defaultContentType, clusterName);
        },
        stopCluster: function(clusterName) {
          var method = 'PUT';
          var url = _defaultHost.concat("/stopCluster");
          return _getPromiseObject(method, url, _defaultContentType, clusterName);
        },
        commandSheet: function() {
          var method = 'GET';
          var url = _defaultHost.concat("/getCommandSheet");
          return _getPromiseObject(method, url, _defaultContentType);
        },
        exitKaramel: function() {
          var method = 'GET';
          var url = _defaultHost.concat("/exitKaramel");
          return _getPromiseObject(method, url, _defaultContentType);
        },
        ping: function() {
          var method = 'GET';
          var url = _defaultHost.concat("/ping");
          return _getPromiseObject(method, url, _defaultContentType);
        },
        sudoPassword: function(sudoAccount) {
          var method = 'PUT';
          var url = _defaultHost.concat("/sudoPassword");
          return _getPromiseObject(method, url, _defaultContentType, sudoAccount);
        },
        githubCredentials: function(githubCredentials) {
          var method = 'PUT';
          var url = _defaultHost.concat("/githubCredentials");
          return _getPromiseObject(method, url, _defaultContentType, githubCredentials);
        },
        setGithubCredentials: function(username, password) {
          var method = 'POST';
          var url = _defaultHost.concat("/setGithubCredentials");
          return _getPromiseObject(method, url, 'application/x-www-form-urlencoded',  
          $.param({ "user" : username , "password" : password }));
        },
        getGithubCredentials: function() {
          var method = 'GET';
          var url = _defaultHost.concat("/getGithubCredentials");
          return _getPromiseObject(method, url, _defaultContentType);
        },
        getGithubOrgs: function() {
          var method = 'POST';
          var url = _defaultHost.concat("/getGithubOrgs");
          return _getPromiseObject(method, url, _defaultContentType, "", true);
        },
        getGithubRepos: function(org) {
          var method = 'POST';
          var url = _defaultHost.concat("/getGithubRepos");
          return _getPromiseObject(method, url, 'application/x-www-form-urlencoded',  
          $.param({"org" : org}), true);
        },
        loadExperiment: function(experimentUrl) {
          var method = 'POST';
          var url = _defaultHost.concat("/loadExperiment");
          return _getPromiseObject(method, url, 'application/x-www-form-urlencoded',  
          $.param({ "experimentUrl" : experimentUrl }));
        },        
        pushExperiment: function(experiment) {
          var method = 'PUT';
          var url = _defaultHost.concat("/pushExperiment");
          return _getPromiseObject(method, url, _defaultContentType, experiment);
        },
        removeFileFromExperiment: function(org, repo, fileName) {
          var method = 'POST';
          var url = _defaultHost.concat("/removeFileFromExperiment");
          return _getPromiseObject(method, url, 'application/x-www-form-urlencoded',  
          $.param({ "org" : org, "repo" : repo, "filename" : fileName }));
        },
        removeRepo: function(org, repo, removeLocal, removeRemote) {
          var method = 'POST';
          var url = _defaultHost.concat("/removeRepository");
          return _getPromiseObject(method, url, 'application/x-www-form-urlencoded',  
          $.param({ "org" : org, "repo" : repo, "local" : removeLocal, "remote" : removeRemote}));
        },
        processCommand: function(commandName) {
          var method = 'PUT';
          var url = _defaultHost.concat("/processCommand");
          return _getPromiseObject(method, url, _defaultContentType, commandName);
        },
        loadGceCredentials: function() {
          var method = 'PUT';
          var url = _defaultHost.concat("/gce/loadCredentials");
          return _getPromiseObject(method, url, _defaultContentType);
        },
        validateGceCredentials: function(providerInfo) {
          var method = 'PUT';
          var url = _defaultHost.concat("/gce/validateCredentials");
          return _getPromiseObject(method, url, _defaultContentType, providerInfo);
        }
	  

      }

    }]);

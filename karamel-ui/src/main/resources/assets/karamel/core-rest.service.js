angular.module('main.module')
  .service('core-rest.service', ['$log', '$http', '$location', function($log, $http, $location) {

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

      var _defaultHost = 'http://' + $location.host() + ':' + $location.port() + '/api';
      var _defaultContentType = 'application/json';


      // Services interacting with the karamel core.
      return{
        jsonToYaml: function(json) {
          var method = 'PUT';
          var url = _defaultHost.concat("/definition/json2yaml");
          return _getPromiseObject(method, url, _defaultContentType, json);
        },
        yamlToJson: function(ymlString) {
          var method = 'PUT';
          var url = _defaultHost.concat("/definition/yaml2json");
          return _getPromiseObject(method, url, _defaultContentType, ymlString);
        },
        getCookBookInfo: function(requestData) {

          var method = 'PUT';
          var url = _defaultHost.concat("/definition/fetchCookbook");
          return _getPromiseObject(method, url, _defaultContentType, requestData);
        },
        loadSshKeys: function() {
          var method = 'PUT';
          var url = _defaultHost.concat("/ssh/loadKey");
          return _getPromiseObject(method, url, _defaultContentType);
        },
        registerSshKeys: function(sshKeypair) {
          var method = 'PUT';
          var url = _defaultHost.concat("/ssh/registerKey");
          return _getPromiseObject(method, url, _defaultContentType, sshKeypair);
        },
        generateSshKeys: function() {
          var method = 'PUT';
          var url = _defaultHost.concat("/ssh/generateKey");
          return _getPromiseObject(method, url, _defaultContentType);
        },
        sudoPassword: function(sudoAccount) {
          var method = 'PUT';
          var url = _defaultHost.concat("/ssh/setSudoPassword");
          return _getPromiseObject(method, url, _defaultContentType, sudoAccount);
        },
        startCluster: function(clusterJson) {
          var method = 'PUT';
          var url = _defaultHost.concat("/cluster/start");
          return _getPromiseObject(method, url, _defaultContentType, clusterJson);
        },
        processCommand: function(commandName) {
          var method = 'PUT';
          var url = _defaultHost.concat("/cluster/processCommand");
          return _getPromiseObject(method, url, _defaultContentType, commandName);
        },
        exitKaramel: function() {
          var method = 'GET';
          var url = _defaultHost.concat("/system/exit");
          return _getPromiseObject(method, url, _defaultContentType);
        },
        ping: function() {
          var method = 'GET';
          var url = _defaultHost.concat("/system/ping");
          return _getPromiseObject(method, url, _defaultContentType);
        },
        setGithubCredentials: function(username, password) {
          var method = 'POST';
          var url = _defaultHost.concat("/github/setCredentials");
          return _getPromiseObject(method, url, 'application/x-www-form-urlencoded',
            $.param({"user": username, "password": password}));
        },
        getGithubCredentials: function() {
          var method = 'GET';
          var url = _defaultHost.concat("/github/getCredentials");
          return _getPromiseObject(method, url, _defaultContentType);
        },
        getGithubOrgs: function() {
          var method = 'POST';
          var url = _defaultHost.concat("/github/getOrgs");
          return _getPromiseObject(method, url, _defaultContentType, "", true);
        },
        getGithubRepos: function(org) {
          var method = 'POST';
          var url = _defaultHost.concat("/github/getRepos");
          return _getPromiseObject(method, url, 'application/x-www-form-urlencoded',
            $.param({"org": org}), true);
        },
        removeRepo: function(org, repo, removeLocal, removeRemote) {
          var method = 'POST';
          var url = _defaultHost.concat("/github/removeRepository");
          return _getPromiseObject(method, url, 'application/x-www-form-urlencoded',
            $.param({"org": org, "repo": repo, "local": removeLocal, "remote": removeRemote}));
        },
        loadExperiment: function(experimentUrl) {
          var method = 'POST';
          var url = _defaultHost.concat("/experiment/load");
          return _getPromiseObject(method, url, 'application/x-www-form-urlencoded',
            $.param({"experimentUrl": experimentUrl}));
        },
        pushExperiment: function(experiment) {
          var method = 'PUT';
          var url = _defaultHost.concat("/experiment/push");
          return _getPromiseObject(method, url, _defaultContentType, experiment);
        },
        removeFileFromExperiment: function(org, repo, fileName) {
          var method = 'POST';
          var url = _defaultHost.concat("/experiment/removeFile");
          return _getPromiseObject(method, url, 'application/x-www-form-urlencoded',
            $.param({"org": org, "repo": repo, "filename": fileName}));
        },
        loadEc2Credentials: function() {
          var method = 'PUT';
          var url = _defaultHost.concat("/ec2/loadCredentials");
          return _getPromiseObject(method, url, _defaultContentType);
        },
        validateEc2Credentials: function(providerInfo) {
          var method = 'PUT';
          var url = _defaultHost.concat("/ec2/validateCredentials");
          return _getPromiseObject(method, url, _defaultContentType, providerInfo);
        },
        loadNovaCredentials: function() {
          var method = 'PUT';
          var url = _defaultHost.concat("/nova/loadCredentials");
          return _getPromiseObject(method, url, _defaultContentType);
        },
        validateNovaCredentials: function(providerInfo) {
          var method = 'PUT';
          var url = _defaultHost.concat("/nova/validateCredentials");
          return _getPromiseObject(method, url, _defaultContentType, providerInfo);
        },
        loadOcciCredentials: function() {
          var method = 'PUT';
          var url = _defaultHost.concat("/occi/loadCredentials");
          return _getPromiseObject(method, url, _defaultContentType);
        },
        validateOcciCredentials: function(providerInfo) {
          var method = 'PUT';
          var url = _defaultHost.concat("/occi/validateCredentials");
          return _getPromiseObject(method, url, _defaultContentType, providerInfo);
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

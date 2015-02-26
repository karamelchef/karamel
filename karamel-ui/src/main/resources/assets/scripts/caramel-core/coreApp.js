/**
 * Created by babbarshaer on 2014-10-29.
 */

// This module deals with the core caramel services.

/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */

'use strict';

angular.module('coreApp', [])

    .controller('CommandCenterController', ['$log', '$scope', '$interval', 'CaramelCoreServices', function($log, $scope, $interval, CaramelCoreServices) {

        function initScope(scope) {
          scope.commandObj = [];
          scope.intervalInstance = [];
          for (var i = 0; i < 3; i++) {
            scope.commandObj.push({
              commandName: null,
              commandResult: null
            });
            scope.intervalInstance.push(undefined);
          }
          
          // Register a destroy event.
          scope.$on('$destroy', function(){
            _destroyIntervalInstances();
          })
            
        }

        $scope.processCommand = function(index) {
          _destroyIntervalInstance(index);
          
          var commandName = $scope.commandObj[index].commandName;
          var commandArg = $scope.commandObj[index].commandResult;
          $scope.commandObj[index].commandName = null;
          var regex = /watch\s+-n\s+(\d+)\s+(.*)/;
          var match = regex.exec(commandName);
          if (match !== null) {
            var interval = match[1];
            var intervalCmd = match[2];
            $log.info("On " + interval + " seconds will call-> " + intervalCmd);
            coreProcessCommand(intervalCmd, commandArg, index)();
            $scope.intervalInstance[index] = $interval(coreProcessCommand(intervalCmd, commandArg, index), interval * 1000);
          } else {
            $log.info("Will call-> " + commandName + " just once");
            coreProcessCommand(commandName, commandArg, index)();
          }

        };

        function coreProcessCommand(cmdName, cmdArg, index) {

          return function() {

            var obj = {
              command: cmdName,
              result: cmdArg
            };

            $log.info("Process Command Called with: " + angular.toJson(obj));
            CaramelCoreServices.processCommand(obj)

                .success(function(data) {
                  $scope.commandObj[index].commandResult = data.result;

                })
                .error(function(data) {
                  $log.info(data);
                  $log.info('Core -> Unable to process command: ' + cmdName);
                });
          };

        }


        /**
         * If any interval instances are present, destroy them.
         * Helps to prevent any memory leaks in the system.
         * @private
         */
        function _destroyIntervalInstances(){
            
            for(var i= 0, len = $scope.intervalInstance.length ; i < len ; i++){
                _destroyIntervalInstance(i);
            }
        }
        
        function _destroyIntervalInstance(index){
            
            if(angular.isDefined($scope.intervalInstance[index])){
                $interval.cancel($scope.intervalInstance[index]);
            }  
        }

        initScope($scope);
      }])

    .service('CaramelCoreServices', ['$log', '$http', '$location', function($log, $http, $location) {

        // Return the promise object to the users.
        var _getPromiseObject = function(method, url, contentType, data) {

          var promiseObject = $http({
            method: method,
            url: url,
            headers: {'Content-Type': contentType},
            data: data
          });

          return promiseObject;
        };

        /* window.location.hostname for the webserver  */

        var _defaultHost = 'http://' + $location.host() + ':9090/api';
        var _defaultContentType = 'application/json';


        // Services interacting with the caramel core.
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
          processCommand: function(commandName) {
            var method = 'PUT';
            var url = _defaultHost.concat("/processCommand");
            return _getPromiseObject(method, url, _defaultContentType, commandName);
          }

        }

      }]);



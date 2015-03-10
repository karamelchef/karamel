/**
 * Created by babbarshaer on 2014-10-29.
 */

// This module deals with the core caramel services.

/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */

'use strict';

angular.module('coreApp', [])

  .controller('CommandCenterController', ['$log', '$scope', '$sce', '$interval', '$timeout', 'CaramelCoreServices', function($log, $scope, $sce, $interval, $timeout, CaramelCoreServices) {

      function initScope(scope) {
        scope.commandObj = [];
        scope.intervalInstance = [];
        scope.timeoutInstance = [];
        for (var i = 0; i < 3; i++) {
          scope.commandObj.push({
            commandName: null,
            commandResult: null,
            renderer: 'info'
          });
          scope.intervalInstance.push(undefined);
          scope.timeoutInstance.push(undefined);
        }

        // Register a destroy event.
        scope.$on('$destroy', function() {
          _destroyIntervalInstances();
        });

      }

      $scope.htmlsafe = function(index) {



//        str = str + "<br/>" + "<a href ng-click=\"processCommand(0)\">Test Command!</a>"
//        str = str + "<br/>" + "<a ng-show=\"false\">Test Command!</a>"

        var htmlize = function(str) {
          if (str !== null) {
            return str
              .replace(/&/g, '&amp;')
              .replace(/ /g, '&nbsp;')
              .replace(/"/g, '&quot;')
              .replace(/'/g, '&#39;')
              .replace(/</g, '&lt;')
              .replace(/>/g, '&gt;')
              .replace(new RegExp('\r?\n', 'g'), '<br/>');
          } else
            return "";
        };

        var text = $scope.commandObj[index].commandResult,
          pattern = new RegExp(/<a[^>]*>[^<>]*<\/a>/g),
          match,
          newStr = "",
          lastInx = -1;
        
        var htmlize = function(str) {
          if (str !== null) {
            return str
              .replace(/&/g, '&amp;')
              .replace(/ /g, '&nbsp;')
              .replace(/"/g, '&quot;')
              .replace(/'/g, '&#39;')
              .replace(/</g, '&lt;')
              .replace(/>/g, '&gt;')
              .replace(new RegExp('\r?\n', 'g'), '<br/>');
          } else
            return "";
        };

        while (match = pattern.exec(text)) {
          if (lastInx < match.index) {
            newStr = newStr + htmlize(text.substring(lastInx, match.index));
          }
          lastInx = pattern.lastIndex;
          newStr = newStr + match[0];
        }

        if (lastInx < text.length) {
          newStr = newStr + htmlize(text.substring(lastInx, text.length));
        }

        return $sce.trustAsHtml(newStr);
      };

      $scope.processCommand = function(index) {
        _destroyIntervalInstance(index);
        var commandName = $scope.commandObj[index].commandName;
        var commandArg = $scope.commandObj[index].commandResult;
        $scope.commandObj[index].commandName = null;
        coreProcessCommand(index, commandName, commandArg)();
      };

//      function processCommand(index, commandName, commandArg) {
//        var regex = /watch\s+-n\s+(\d+)\s+(.*)/;
//        var match = regex.exec(commandName);
//        if (match !== null) {
//          var interval = match[1];
//          var intervalCmd = match[2];
//          $log.info("On " + interval + " seconds will call-> " + intervalCmd);
//          coreProcessCommand(intervalCmd, commandArg, index)();
//          $scope.intervalInstance[index] = $interval(coreProcessCommand(index, intervalCmd, commandArg), interval * 1000);
//        } else {
//          $log.info("Will call-> " + commandName + " just once");
//          coreProcessCommand(index, commandName, commandArg)();
//        }
//      }

      function coreProcessCommand(index, cmdName, cmdArg) {

        return function() {
          $log.info("Running " + cmdName);
          var obj = {
            command: cmdName,
            result: cmdArg
          };

          CaramelCoreServices.processCommand(obj)

            .success(function(data) {
              $scope.commandObj[index].commandResult = data.result;

              if (data.renderer !== null) {
                $scope.commandObj[index].renderer = data.renderer;
              } else
                $scope.commandObj[index].renderer = 'info';

              if (data.nextCmd !== null) {
                _destroyIntervalInstance(index);
                $scope.timeoutInstance[index] = $timeout(coreProcessCommand(index, data.nextCmd, null), 2000);
              }
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
      function _destroyIntervalInstances() {
        for (var i = 0, len = $scope.intervalInstance.length; i < len; i++) {
          _destroyIntervalInstance(i);
        }
      }

      function _destroyIntervalInstance(index) {
        if (angular.isDefined($scope.intervalInstance[index])) {
          $interval.cancel($scope.intervalInstance[index]);
        }
        if (angular.isDefined($scope.timeoutInstance[index])) {
          $timeout.cancel($scope.timeoutInstance[index]);
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



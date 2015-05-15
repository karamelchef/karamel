/**
 * Created by babbarshaer on 2014-10-29.
 */

// This module deals with the core karamel services.

/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */

'use strict';

angular.module('karamel.terminal', [])

  .controller('karamelTerminalController', ['$log', '$rootScope', '$scope', '$sce', 
      '$interval', '$timeout', 'KaramelCoreRestServices', 
      function($log, $rootScope, $scope, $sce, $interval, $timeout, KaramelCoreRestServices) {

      function initScope(scope) {
        scope.commandObj = [];
        scope.intervalInstance = [];
        scope.timeoutInstance = [];
        for (var i = 0; i < 3; i++) {
          scope.commandObj.push({
            commandName: null,
            commandResult: null,
            renderer: 'info',
            menuitems: [{command: "home", label: 'Home'}, {command: "new", label: 'New Cluster'}, {command: "help", label: 'Help'}]
          });
          scope.intervalInstance.push(undefined);
          scope.timeoutInstance.push(undefined);
        }

        // Register a destroy event.
        scope.$on('$destroy', function() {
          _destroyIntervalInstances();
        });

        scope.htmlSafeData = undefined;
        scope.dagData = undefined;
        scope.processCommand(0, "home");
      }

      $scope.htmlsafe = function(index) {

        $log.info('Called html safe');
        var text = $scope.commandObj[index].commandResult;

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

        var convertLinks = function(str) {
          if (str !== null) {
            var pattern = new RegExp(/kref='((\w|[-]|\.|\s)*)'/g);
            var match;
            var lastInx = -1;
            var newStr = "";
            while (match = pattern.exec(str)) {

              if (lastInx < match.index) {
                newStr = newStr + str.substring(lastInx, match.index);
              }
              lastInx = pattern.lastIndex;
              newStr = newStr + "ng-click=\"processCommand(0, \'" + match[1] + "\')\"";
            }

            if (str !== null && lastInx < str.length) {
              newStr = newStr + str.substring(lastInx, str.length);
            }

            return newStr;
          } else
            return "";
        };

        text = convertLinks(text);

        var pattern = new RegExp(/<a[^>]*>[^<>]*<\/a>/g);
        var match;
        var lastInx = -1;
        var newStr = "";
        while (match = pattern.exec(text)) {
          if (lastInx < match.index) {
            newStr = newStr + htmlize(text.substring(lastInx, match.index));
          }
          lastInx = pattern.lastIndex;
          newStr = newStr + match[0];
        }

        if (text !== null && lastInx < text.length) {
          newStr = newStr + htmlize(text.substring(lastInx, text.length));
        }

        newStr = "<div>" + newStr + "</div>";
        $scope.htmlSafeData = newStr;
      };

      $scope.processTerminal = function(index) {
        _destroyIntervalInstance(index);
        var commandName = $scope.commandObj[index].commandName;
        var commandArg = $scope.commandObj[index].commandResult;
        $scope.commandObj[index].commandName = null;
        $scope.$emit('ask-core', {index: index, cmdName: commandName, cmdArg: commandArg});
      };

      $scope.processCommand = function(index, cmdName) {
        $log.debug("Process Command Called");
        var commandArg = null;
        if ($scope.commandObj[index].renderer === 'yaml')
          commandArg = $scope.commandObj[index].commandResult;
        _destroyIntervalInstance(index);
        $scope.$emit('ask-core', {index: index, cmdName: cmdName, cmdArg: commandArg});
      };

      $scope.$on('ask-core', function(e, input) {
        var index = input.index;
        var cmdName = input.cmdName;
        var cmdArg = input.cmdArg;
        $log.info("Running " + cmdName);

        var obj = {
          command: cmdName,
          result: cmdArg
        };

        KaramelCoreRestServices.processCommand(obj)

          .success(function(data) {
            $scope.commandObj[index].errormsg = data.errormsg;

            if (data.errormsg === null) {
              $scope.commandObj[index].successmsg = data.successmsg;
              var timeinterval = 5000;

              if (data.successmsg === null) {
                $scope.commandObj[index].commandResult = data.result;
                if (data.renderer !== null) {
                  $scope.commandObj[index].renderer = data.renderer;
                } else {
                  $scope.commandObj[index].renderer = 'info';
                }
                if (data.renderer === 'info') {
                  $scope.htmlsafe(index);
                } else if (data.renderer === 'dag') {
                  $scope.dagData = data.result;
                } else if (data.renderer === 'ssh') {
                  _destroyIntervalInstance(index);
                  $scope.$emit('core-result-ssh', [data.result]);
                }
              } else {
                timeinterval = 5000;
              }

              if (data.nextCmd !== null) {
                _destroyIntervalInstance(index);
                $scope.timeoutInstance[index] = $timeout(function() {
                  $scope.$emit('ask-core', {index: index, cmdName: data.nextCmd, cmdArg: null});
                }, timeinterval);
              }

            }

            if (data.menuItems.length > 0) {
              $scope.commandObj[index].menuitems = data.menuItems;
            }
          })
          .error(function(data) {
            $log.info('Core -> Unable to process command: ' + cmdName);
            // Turn text Red and write error message at the board cluster name
            // http://stackoverflow.com/questions/27030849/change-font-color-of-text-in-scope-variable-in-angularjs
//            $rootScope.headerName = "Karamel Application has Crashed. Restart it.";
            $rootScope.karamelBoard.name = "Karamel Application has Crashed. Restart it.";
//            $rootScope.karamelBoard.name = $interpolate('<font color="red">{{headerName}}</font>')($rootScope);      
          });
      });


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

  .directive('compileData', ['$log', '$sce', '$compile', function($log, $sce, $compile) {
      return {
        restrict: 'A',
        link: function(scope, element, attrs) {
          scope.$watch('htmlSafeData', function(data) {
            if (data !== undefined) {
              element.html('');
              data = $compile(data)(scope);
              element.append(data);
            }
          });
        }
      };
    }])

  .directive('acDagre', ['$compile', function($compile) {
      return {
        restrict: "A",
        link: function(scope, element, attrs) {
          // Set up zoom support
          var svg = d3.select("svg"),
            inner = svg.select("g"),
            zoom = d3.behavior.zoom().on("zoom", function() {
            inner.attr("transform", "translate(" + d3.event.translate + ")" +
              "scale(" + d3.event.scale + ")");
          });
          svg.call(zoom);

          var render = new dagreD3.render();

          // Left-to-right layout
          var g = new dagreD3.graphlib.Graph();
          g.setGraph({
            nodesep: 70,
            ranksep: 50,
            rankdir: "LR",
            marginx: 20,
            marginy: 20
          });
          scope.$watch('dagData', function(data) {
            if (data !== undefined) {
              updateDag(data);
            }
          });

          var updateDag = function(data) {

            var tasks = JSON.parse(data);
            for (var id in tasks) {
              var task = tasks[id];
              var className = '';
              if (task.status === 'WAITING')
                className += 'waiting';
              else if (task.status === "READY")
                className += 'ready ';
              else if (task.status === "ONGOING")
                className += 'ongoing blinking';
              else if (task.status === "DONE")
                className += 'done';
              else if (task.status === "FAILED")
                className += 'failed';

              var html = '<div>';
              html += '<span class="status"></span>';
              html += '<span class="name">' + task.name + '</span>';
              html += '<br>';
              html += '<span class="name">' + task.machine + '</span>';
              html += '</div>';

              g.setNode(task.id, {
                labelType: "html",
                label: html,
                rx: 5,
                ry: 5,
                width: 100,
                height: 25,
                padding: 0,
                class: className
              });

              if (task.preds) {
                for (var e in task.preds) {
                  g.setEdge(task.preds[e], task.id, {
                    label: "",
                    width: 40
                  });
                }
              }
            }
//            var elm = $compile(g)(scope);
            inner.call(render, g);
            var gs = d3.selectAll("svg g g g g g g ");
            for (var i in gs[0]) {
              var gi = gs[0][i];
              gi.setAttribute("transform", "translate(-50,-12.5)");
              var fo = gi.getElementsByTagName("foreignObject");
              fo[0].setAttribute("width", "100");
              fo[0].setAttribute("height", "25");
            }
          };
        }
      };
    }])

  .service('KaramelCoreRestServices', ['$log', '$http', '$location', function($log, $http, $location) {

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
        scaffoldCookbook: function(cookbookName) {
          var method = 'PUT';
          var url = _defaultHost.concat("/scaffold");
          return _getPromiseObject(method, url, _defaultContentType, cookbookName);
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
        processCommand: function(commandName) {
          var method = 'PUT';
          var url = _defaultHost.concat("/processCommand");
          return _getPromiseObject(method, url, _defaultContentType, commandName);
        }

      }

    }])

  .service('sshIps', ['$log', function() {
      var _ipAddress = '192.168.33.10';

      this.ipAddress = _ipAddress;

    }]);



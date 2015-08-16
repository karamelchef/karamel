'use strict';

angular.module('main.module')
  .config(['$routeProvider', function($routeProvider) {
      $routeProvider.when('/', {templateUrl: 'karamel/board/board.html', controller: 'board.controller'});
      $routeProvider.when('/terminal', {templateUrl: 'karamel/terminal/terminal.html', controller: 'terminal.controller'});
      $routeProvider.when('/experiment', {templateUrl: 'karamel/experiment/experiment.html', controller: 'experiment.controller'});
      $routeProvider.otherwise({redirectTo: '/'});
    }], function($httpProvider, $rootScope) {
    $httpProvider.interceptors.push(function($q) {
      return {
        responseError: function(rejection) {
          if (rejection.status === 0) {
            $rootScope.activeCluster.name = "Karamel Application has Crashed!!";
            return;
          }
          return $q.reject(rejection);
        }
      }
    });
  }
  );
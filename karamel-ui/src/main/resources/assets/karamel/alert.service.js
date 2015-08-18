'use strict';

angular.module('main.module')
  .service('alert.service', ['$log', function($log) {

      var _currAlert = null;

      return {
        addAlert: function(alert) {
          _currAlert = alert;
        },
        getAlert: function() {
          return _currAlert;
        }
      }

    }]);
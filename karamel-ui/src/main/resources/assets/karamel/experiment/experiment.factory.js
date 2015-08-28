'use strict';

angular.module('main.module')
  .factory('experiment.factory', ['$window', function($window) {
      return {
        clear: function() {
          try {
            if ($window.Storage) {
              $window.localStorage.removeItem('experiment');
              $window.localStorage.clear();
              return true;
            }
          } catch (error) {
            console.error(error, error.message);
          }
          return false;
        },
        store: function(value) {
          try {
            if ($window.Storage) {
              if (value === null) {
                $window.localStorage.setItem('experiment', value);
              } else {
                $window.localStorage.setItem('experiment', $window.JSON.stringify(value));
              }
              return true;
            } else {
              return false;
            }
          } catch (error) {
            console.error(error, error.message);
          }
          return false;
        },
        recover: function( ) {
          try {
            if ($window.Storage) {
              return $window.JSON.parse($window.localStorage.getItem('experiment'))
            } else {
              return false;
            }
          } catch (error) {
            console.error(error, error.message);
          }
          return false;
        }
      }
    }])
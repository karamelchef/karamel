'use strict';

angular.module('main.module')
  .factory('modal.factory', ['$modal', function($modal) {
      return {
        confirm: function(size, title, msg) {
          var modalInstance = $modal.open({
            templateUrl: 'karamel/experiment/modal-confirm.html',
            controller: 'modal.controller as ctrl',
            size: size,
            resolve: {
              title: function() {
                return title;
              },
              msg: function() {
                return msg;
              }
            }
          });
          return modalInstance.result;
        },
        profile: function(size) {
          var modalInstance = $modal.open({
            templateUrl: 'karamel/github/github-profile.html',
            controller: 'github-profile.controller as ctrl',
            size: size,
            resolve: {
            }
          });
          return modalInstance.result;
        },
        experimentFactory: function(size, githubDetails) {
          var modalInstance = $modal.open({
            templateUrl: 'karamel/experiment/new-experiment.html',
            controller: 'new.experiment.controller as controller',
            size: size,
            windowClass: 'app-modal-window',
            resolve: {
              githubDetails: function() {
                return githubDetails;
              }

            }
          });
          return modalInstance.result;
        },
        loadExperiment: function(size, githubUrl) {
          var modalInstance = $modal.open({
            templateUrl: 'karamel/experiment/load-experiment.html',
            controller: 'load.experiment.controller as controller',
            size: size,
            windowClass: 'app-modal-window',
            resolve: {
              githubUrl: function() {
                return githubUrl;
              }

            }
          });
          return modalInstance.result;
        }
      }

    }]);

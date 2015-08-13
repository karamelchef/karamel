'use strict';

angular.module('karamel.main')
    .factory('ModalService', ['$modal', function ($modal) {
        return {
          confirm: function (size, title, msg) {
            var modalInstance = $modal.open({
              templateUrl: 'karamel/partials/modal-confirm.html',
              controller: 'ModalCtrl as ctrl',
              size: size,
              resolve: {
                title: function () {
                  return title;
                },
                msg: function () {
                  return msg;
                }
              }
            });
            return modalInstance.result;
          },
          profile: function (size) {
            var modalInstance = $modal.open({
              templateUrl: 'karamel/partials/profile.html',
              controller: 'ProfileCtrl as profileCtrl',
              size: size,
              resolve: {
              }
            });
            return modalInstance.result;
          },
          experimentFactory: function (size, githubDetails) {
            var modalInstance = $modal.open({
              templateUrl: 'karamel/partials/experiment-new.html',
              controller: 'NewExperimentCtrl as newExperimentCtrl',
              size: size,
              windowClass: 'app-modal-window',
              resolve: {
                githubDetails: function () {
                  return githubDetails;
                }

              }
            });
            return modalInstance.result;
          },
          loadExperiment: function (size, githubUrl) {
            var modalInstance = $modal.open({
              templateUrl: 'karamel/partials/experiment-load.html',
              controller: 'LoadExperimentCtrl as loadExperimentCtrl',
              size: size,
              windowClass: 'app-modal-window',
              resolve: {
                githubUrl: function () {
                  return githubUrl;
                }

              }
            });
            return modalInstance.result;
          }
        }

      }]);

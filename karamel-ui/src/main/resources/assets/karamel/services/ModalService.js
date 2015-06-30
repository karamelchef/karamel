'use strict';

angular.module('karamel.main')
  .factory('ModalService', ['$modal', function ($modal) {
    return {

      confirm: function (size, title, msg) {
        var modalInstance = $modal.open({
          templateUrl: 'karamel/partials/confirmModal.html',
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
      }

    }]);

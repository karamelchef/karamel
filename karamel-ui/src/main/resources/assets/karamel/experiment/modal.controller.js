'use strict';

angular.module('karamel-main.module')
    .controller('modal.controller', ['$modalInstance', '$scope', 'title', 'msg',
      function ($modalInstance, $scope, title, msg) {

        var self = this;
        self.title = title;
        self.msg = msg;

        self.ok = function () {
          $modalInstance.close();
        };

        self.cancel = function () {
          $modalInstance.dismiss('cancel');
        };

      }]);
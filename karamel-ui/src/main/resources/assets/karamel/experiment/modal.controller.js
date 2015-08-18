'use strict';

angular.module('main.module')
  .controller('modal.controller', ['$modalInstance', 'title', 'msg',
    function($modalInstance, title, msg) {

      var self = this;
      self.title = title;
      self.msg = msg;

      self.ok = function() {
        $modalInstance.close();
      };

      self.cancel = function() {
        $modalInstance.dismiss('cancel');
      };

    }]);
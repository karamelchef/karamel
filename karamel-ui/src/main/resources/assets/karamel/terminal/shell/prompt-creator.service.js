angular.module('main.module')
  .service('prompt-creator.service', [function() {
      var prompt = function(config) {
        var me = {};
        var _user, _path, _userPathSeparator, _promptEnd;

        config = config ? config.promptConfiguration : null;

        var build = function() {
          me.text = _user + _userPathSeparator + _path + _promptEnd;
//            me.text = "";
        };

        me.resetPath = function() {
          _path = config && config.path ? config.path : '\\';
          build();
        };

        me.resetUser = function() {
          _user = config && config.user ? config.user : 'karamel';
          build();
        }

        me.reset = function() {
          _user = config && config.user != null ? (config.user || '') : 'karamel';
          _path = config && config.path != null ? (config.path || '') : '\\';
          _userPathSeparator = config && config.separator != null ? (config.separator || '') : '@';
          _promptEnd = config && config.end != null ? (config.end || '') : ':>';
          build();
        };

        me.user = function(user) {
          if (user) {
            _user = user;
            build();
          }
          return _user;
        };

        me.path = function(path) {
          if (path) {
            _path = path;
            build();
          }
          return _path;
        }

        me.text = '';

        me.reset();

        return me;
      };
      return prompt;
    }]);
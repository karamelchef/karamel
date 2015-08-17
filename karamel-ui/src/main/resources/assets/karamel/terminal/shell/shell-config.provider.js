angular.module('main.module')

  .provider('shell-config.provider', function() {

    var config = function() {
      var me = {};
      me.typeSoundUrl = null;
      me.startSoundUrl = null;
      me.promptConfiguration = {end: ':>', user: 'karamel', separator: '@', path: '\\'};

      me.getTypeEffect = null;
      me.getStartEffect = null;
      me.allowTypingWriteDisplaying = true;

      return me;
    };

    var provider = function() {
      var me = config();
      var configurations = {};
      configurations['default'] = me;

      me.config = function(configName) {
        var c = configurations[configName];
        if (!c) {
          c = config();
          configurations[configName] = c;
        }
        return c;
      };

      me.$get = ['$q', function($q) {

          var loadNotificationSound = function($q, path) {
            var deferred = $q.defer();
            var request = new XMLHttpRequest();
            request.open('GET', path, true);
            request.responseType = 'arraybuffer';
            request.onload = function() {
              window.AudioContext = window.AudioContext || window.webkitAudioContext;
              var context = new AudioContext();
              context.decodeAudioData(request.response, function(buffer) {

                deferred.resolve(function() {
                  var source = context.createBufferSource();
                  source.buffer = buffer;
                  source.connect(context.destination);
                  source.start(0);
                });
              });
            }
            request.send();
            return deferred.promise;
            ;
          };

          for (var key in configurations) {
            var c = configurations[key];
            if (c.typeSoundUrl)
              c.getTypeEffect = loadNotificationSound($q, c.typeSoundUrl);
            if (c.startSoundUrl)
              c.getStartEffect = loadNotificationSound($q, c.startSoundUrl);
          }

          return function(configName) {
            return configurations[configName];
          };
        }];

      return me;
    };
    return provider();
  });
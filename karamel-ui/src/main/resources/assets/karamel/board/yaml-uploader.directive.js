/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */

'use strict';

angular.module('karamel-main.module')
    .directive('yamlUploader', ['$log', '$rootScope', 'KaramelCoreRestServices',
      'AlertService', 'BrowserCacheService',
      function($log, $rootScope, KaramelCoreRestServices, AlertService, BrowserCacheService) {
        return{
          restrict: 'A',
          link: function(scope, element, attributes) {

            element.bind('change', function(changeEvent) {
              var reader = new FileReader();
              reader.onload = function(loadEvent) {
                var ymlJson = {
                  yml: loadEvent.target.result
                };
                $log.info("Requesting Karamel Core Services for JSON. ");
                BrowserCacheService.resetCache();
                KaramelCoreRestServices.getJsonFromYaml(ymlJson)
                    .success(function(data, status, headers, config) {
                      $log.info("Success");

                      try {
                        var cluster = new Cluster();
                        cluster.load(data);
                        $rootScope.activeCluster = cluster;
                        $rootScope.context = cluster.name;
                        AlertService.addAlert({type: 'success', msg: 'Model Created Successfully.'});
                      }
                      catch (err) {
                        $log.error(err);
                        AlertService.addAlert({type: 'danger', msg: 'Unable to parse json from core.'});
                      }

                      $log.info($rootScope.activeCluster);
                    })
                    .error(function(data, status, headers, config) {
                      $log.info("Fetch Call Failed.");
                      AlertService.addAlert({type: 'danger', msg: 'Core: ' + data.message});
                    });

                element.val("");
              };
              reader.readAsText(changeEvent.target.files[0]);
            });

          }
        }
      }])
    .directive('clickDirective', ['$log', function($log) {

        return {
          restrict: 'A',
          link: function(scope, element, attributes) {

            element.bind('click', function(clickEvent) {
              var uploaderElement = angular.element(document.querySelector("#yamlUploader"));
              uploaderElement.trigger('click');
            });

          }
        }

      }]);


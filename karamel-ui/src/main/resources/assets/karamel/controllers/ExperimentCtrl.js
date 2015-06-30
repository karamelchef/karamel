'use strict';

angular.module('karamel.main')
        .controller('ExperimentCtrl', ['$log', '$scope', 'md5', 'KaramelCoreRestServices', 'info',
            function ($log, $scope, md5, KaramelCoreRestServices, info) {



                $scope.selectedIcon = "";
                $scope.icons = [{value: "New", label: "New Experiment"},
                    {value: "Load", label: "Load Experiment"}];

                $scope.tooltip = {
                    title: "Experiment Designer<br />This is a multiline message!",
                    checked: false
                };

                $scope.profileModal = function () {
                    ModalService.profile('md');
                };


//                $scope.emailHash = "";
                $scope.githubRepoUrl = "";
                $scope.loading = false;
//                $scope.githubCredentials = {
//                    email: "",
//                    password: ""
//                };
                $scope.experimentContext = {
                    githubUrl: "",
                    experimentName: "",
                    experimentUser: "",
                    experimentGroup: "",
                    experimentPreChefCode: "",
                    experimentScript: ""
                };

                function _initScope(scope) {
                    KaramelCoreRestServices.getGithubCredentials()
                            .success(function (data, status, headers, config) {
                                $scope.githubCredentials = data;
                                $log.info("Experiment Details Fetched Successfully.");
                            })
                            .error(function (data, status, headers, config) {
                                $log.info("Experiment Details can't be Fetched.");
                            });
                }

                function loadExperiment(experimentUrl) {
                    KaramelCoreRestServices.loadExperiment(experimentUrl)
                            .success(function (data, status, headers, config) {
                                $scope.experimentContext = data;
                                $log.info("Experiment Details Fetched Successfully.");
                            })
                            .error(function (data, status, headers, config) {
                                $log.info("Experiment Details can't be Fetched.");
                            });

                }



                _initScope($scope);
            }])
        .directive('modal', function () {
  return {
    template: '<div class="modal fade">' +
    '<div class="modal-dialog modal-md">' +
    '<div class="modal-content">' +
    '<div class="modal-header">' +
    '<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>' +
    '<h2 class="modal-title">{{ title }}</h2>' +
    '</div>' +
    '<div class="modal-body" ng-transclude></div>' +
    '</div>' +
    '</div>' +
    '</div>',
    restrict: 'E',
    transclude: true,
    replace: true,
    scope: true,
    link: function postLink(scope, element, attrs) {
      scope.title = attrs.title;

      scope.$watch(attrs.visible, function (value) {
        if (value == true)
          $(element).modal('show');
        else
          $(element).modal('hide');
      });

      $(element).on('shown.bs.modal', function () {
        scope.$apply(function () {
          scope.$parent[attrs.visible] = true;
        });
      });

      $(element).on('hidden.bs.modal', function () {
        scope.$apply(function () {
          scope.$parent[attrs.visible] = false;
        });
      });
    }
  };
});
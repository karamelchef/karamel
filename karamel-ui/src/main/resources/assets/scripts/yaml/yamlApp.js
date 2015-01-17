//yaml is a separate section and therefore we have a separate module for updation and validation of yml files.

'use strict';

angular.module('yamlApp', [])
    .controller('yamlEditorController', ['$log', '$rootScope', '$modalInstance', '$scope', 'CaramelCoreServices', function ($log, $rootScope, $modalInstance, $scope, CaramelCoreServices) {

        $scope.updateBoardDataObject = function () {
            // Check if the form is valid, which in turn depends upon the validity of its elements.
//            if (this.editYamlForm.$valid) {
//
//                var ymlJson = {
//                    yml: $scope.cluster.yaml
//                };
//
//                CaramelCoreServices.getJsonFromYaml(ymlJson)
//                    .success(function (data, status, headers, config) {
//                        $log.info("Success");
//                        // Set the root scope and update the window local storage also.
//                        $modalInstance.close(data);
//                    })
//                    .error(function (data, status, headers, config) {
//                        $log.info("Fetch Call Failed.")
//                    });
//            }

            $modalInstance.close();

        };

        $scope.close = function () {
            $modalInstance.close();                             //Close the modal instance.
        };

        var initialize = function (scope) {                       // Initialize the scope.

            scope.cluster = {};

            var _rest = getRestObjBuilder().buildCaramelForRest($rootScope.karamelBoard);

            CaramelCoreServices.getCompleteYaml({
                json: angular.toJson(_rest)
            })
                .success(function (data, status, headers, config) {
                    $log.info("Yaml Fetch Complete.");
                    scope.cluster.yaml = data.yml;
                })
                .error(function (data, status, headers, config) {
                    $log.info("Yaml Fetch Call Failed.");
                });

        };
        initialize($scope);
    }])
    .directive('karamelYamlValidate', function () {

        return{

            require: 'ngModel',                                 // As we are require for the controller in the ngModel, we will have to handle a separate controller.
            link: function (scope, element, attrs, ctrl) {

                ctrl.$validators.karamelYamlValidate = function (modelValue, viewValue) {

                    var clusterConfigurationObject = YAML.parse(viewValue);
                    //IF PARSING WAS FINE.
                    if (clusterConfigurationObject !== undefined) {
                        return true;
                    }
                    return false;

                }
            }
        }

    })
    .directive('allowTabs', ['$log', function ($log) {
        // TAB space need to be synced with the indentation allowed in Yaml.
        return{
            restrict: 'A',
            link: function (scope, element, attrs) {

                element.bind("keydown", function (e) {
                    var keyCode = e.keyCode || e.which;
                    if (keyCode == 9) {

                        //Dont allow the default function.
                        e.preventDefault();

                        // Calculate the new caret position, based on 4 spaces.
                        var newCaretPosition;
                        newCaretPosition = this.selectionStart + "    ".length;
                        this.value = this.value.substring(0, this.selectionStart) + "    " + this.value.substring(this.selectionStart, this.value.length);

                        // Update the selectionstart and end based on the new caret position.
                        this.selectionStart = newCaretPosition;
                        this.selectionEnd = newCaretPosition;
                        this.focus();

                    }
                });

            }
        }
    }]);
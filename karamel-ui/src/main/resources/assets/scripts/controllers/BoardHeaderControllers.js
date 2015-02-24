/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */
'use strict';

angular.module('demoApp')
    .controller('HeaderController', ['$log', 'BoardService' , '$rootScope', '$scope','$window' , '$location','AlertService', function ($log, BoardService, $rootScope, $scope,$window,$location,AlertService) {

        function initScope(scope){

            $log.info("Inside the Board Controllers");

            if($window['sessionStorage'] !== undefined){

                var boardObj = $window.sessionStorage.getItem('karamel');
                boardObj = angular.fromJson(boardObj);

                if(boardObj !== null){

                    try{
                        $rootScope.karamelBoard = BoardService.copyKaramelBoard(boardObj);
                        AlertService.addAlert({type: 'success', msg:'Model Loaded Successfully.'});
                    }
                    catch(err){
                        $log.error(err);
                        AlertService.addAlert({type: 'danger', msg:'Unable to parse the json to generate model.'});
                    }

                }
                else {
                    AlertService.addAlert({type: 'info', msg:'Couldn\'t find any model in cache.'});
                }
            }

            else {
                $log.error("No Support for session storage.");
            }


        }

        // Controller for the header.
        $scope.editYaml = function () {
            BoardService.editClusterConfiguration($rootScope.karamelBoard);

        };

        // Add a new node group to the system.
        $scope.addNodeGroup = function () {
            // Call board service to update the values.
            BoardService.addNewNodeGroup($rootScope.karamelBoard);
        };

        // Load a new file form template.
        $scope.loadYamlFromTemplate = function () {

        };

        // configure the global attributes.
        $scope.configureGlobalAttributes = function () {
            BoardService.configureGlobalAttributes($rootScope.karamelBoard);
        };

        // Save the yaml to the disk.
        $scope.saveYamlToDisk = function(){
            BoardService.saveYaml($rootScope.karamelBoard);
        };

        $scope.editAmazonProvider = function(){
           BoardService.editAmazonProvider($rootScope.karamelBoard,null, false);
        };

        $scope.editSshKeys = function(){
            BoardService.editSshKeys();
        };
        
        $scope.launchCluster = function(){
            $log.info("Launching Cluster");
//            var rootScope = $rootScope.karamelBoard;
//            $log.info(rootScope.getEC2provider());

//            if(rootScope.getEC2provider() == null){
//                BoardService.editAmazonProvider(rootScope,null, true)
//            }
//            else{
                BoardService.startCluster($rootScope.karamelBoard);
//            }

        };

        $scope.stopCluster = function(){
            $log.info("stop cluster invoked.");
            AlertService.addAlert({type: 'info', msg: 'Under development.'});
        };

        $scope.pauseCluster = function(){
            $log.info("pause cluster invoked.");
            AlertService.addAlert({type: 'info', msg: 'Under development.'});
        };

        $scope.viewCluster = function(){
            $log.info("view cluster invoked.");
//            AlertService.addAlert({type: 'warning', msg: 'Under development.'});

            BoardService.viewCluster($rootScope.karamelBoard);

        };
        
        $scope.switchCommandPage = function(){
            $log.info('Switching to Command Page.');
            $location.path('/commandPage');
        };
        

        initScope($scope);

    }])
    .directive('fileUploader', ['$log', '$rootScope', 'BoardService', 'BoardDataService', 'CaramelCoreServices','$window','AlertService', function ($log, $rootScope, BoardService, BoardDataService, CaramelCoreServices,$window,AlertService) {

        // Reset the cache.
        function _cleanKaramelBoard(){
            $rootScope.karamelBoard = undefined;

            if($window['sessionStorage'] !== undefined){
                $window.sessionStorage.setItem('karamel', $rootScope.karamelBoard === undefined ? null : angular.toJson($rootScope.karamelBoard));
            }
        }

        // Upload the file to the system.
        return{
            restrict: 'A',
            link: function (scope, element, attributes) {

                element.bind('change', function (changeEvent) {

                    var reader = new FileReader();

                    reader.onload = function (loadEvent) {
                        var ymlJson = {
                            yml: loadEvent.target.result
                        };

                        $log.info("Requesting Caramel Core Services for JSON. ");

                        // Reset the karamelBoard.
                        _cleanKaramelBoard();

                        // Now issue the call to core.
                        CaramelCoreServices.getJsonFromYaml(ymlJson)
                            .success(function (data, status, headers, config) {
                                $log.info("Success");

                                // Set the root scope and update the window local storage also.
                                try{
                                    $rootScope.karamelBoard = BoardService.createKaramelBoard(data);
                                    AlertService.addAlert({type: 'success', msg:'Model Created Successfully.'});

                                    if($window['sessionStorage'] !== undefined){
                                        $window.sessionStorage.setItem('karamel', $rootScope.karamelBoard === undefined ? null : angular.toJson($rootScope.karamelBoard));
                                    }
                                    else {
                                        $log.error("No Support for session storage.");
                                        AlertService.addAlert({type: 'warning', msg:'Unable to cache the karamel board'});
                                    }
                                }
                                catch(err){
                                    $log.error(err);
                                    AlertService.addAlert({type: 'danger', msg:'Unable to parse json from core.'});
                                }

                                $log.info($rootScope.karamelBoard);
                            })
                            .error(function (data, status, headers, config) {
                                $log.info("Fetch Call Failed.");
                                AlertService.addAlert({type: 'danger', msg: 'Core: '+ data.message});
                            });

                        element.val("");
                    };

                    //Assuming only single file read is allowed.
                    reader.readAsText(changeEvent.target.files[0]);
                });

            }
        }
    }])
    .directive('clickDirective', ['$log', function ($log) {

        return {
            restrict: 'A',
            link: function (scope, element, attributes) {

                element.bind('click', function (clickEvent) {
                    var uploaderElement = angular.element(document.querySelector("#fileUploader"));
                    uploaderElement.trigger('click');
                });

            }
        }

    }]);
/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */
'use strict';
angular.module('karamel-main.module')
    .service('cookbook-manipulation.service', ['$log', function($log) {
        return {
          prepareCookbookMetaData: function(scopeCookbook, defaultCookbook) {
            var globalCookbook = null;
            if (scopeCookbook["name"] == defaultCookbook["name"]) {
              globalCookbook = scopeCookbook;
            }

            // If the attributes section is present.
            if (globalCookbook != null && globalCookbook["attributes"] != null) {
              angular.forEach(defaultCookbook["attributes"], function(attribute) {

                var storedAttributes = globalCookbook["attributes"];

                // If same property present replace the value from the cookbook.
                if (storedAttributes[attribute["name"]] != null) {
                  attribute["value"] = storedAttributes[attribute["name"]];
                }
                else {
                  attribute["value"] = attribute["default"];
                }
              });
            }
            return defaultCookbook;
          },
          persistDataChangesInLocalCookbook: function(storedCookbooks, updatedCookbooksMetaData) {

            angular.forEach(updatedCookbooksMetaData, function(cookbookMetaData) {

              var requiredCookbook = null;

              for (var i = 0; i < storedCookbooks.length; i++) {
                if (cookbookMetaData["name"] === storedCookbooks[i]["name"]) {
                  requiredCookbook = storedCookbooks[i];
                  break;
                }
              }

              // cookbook found.
              if (requiredCookbook != null) {

                var attributes = cookbookMetaData["attributes"];
                angular.forEach(attributes, function(attribute) {

                  // If variable is set and not empty string or full of spaces, then append the property value.
                  if (attribute["type"] === 'array' && attribute["value"] != null && attribute["value"].length > 0) {
                    var diff = (attribute["value"].length !== attribute["default"].length);

                    if (attribute["value"].length === attribute["default"].length) {
                      for (i in attribute["value"]) {
                        if (attribute["value"][i] !== attribute["default"][i])
                          diff = true;
                      }
                    }

                    if (diff)
                      requiredCookbook.addPropertyToAttributes(attribute["name"], attribute["value"]);
                  } else if (attribute["value"] != null && !!(attribute["value"].replace(/\s/g, '').length) && attribute["default"] !== attribute["value"]) {
                    requiredCookbook.addPropertyToAttributes(attribute["name"], attribute["value"]);
                  }
                });
              }
            });
            return storedCookbooks;
          }
        }

      }]);




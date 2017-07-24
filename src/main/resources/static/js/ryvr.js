 /* global angular */
 /* global document */
 /* global console */
 /* global window */
 /* global window */

define(['angular'], function(angular) {
    'use strict';
    
    var app = angular.module('ryvr', []);

    app.config(function($locationProvider, $httpProvider) {
        $locationProvider.html5Mode(true);
        $httpProvider.interceptors.push(function($q) {
            return {
                'request'(config) {
                    angular.element(document.getElementById('controller')).scope().controller.loading = true;
                    angular.element(document.getElementById('controller')).scope().controller.viewLoaded = false;
                    config.headers['Accept'] = 'application/hal+json;q=1,application/json;q=0.8,*/*;q=0.1';
                    if(getLocation(config.url).pathname == '/api-docs') {
                        window.location.href = config.url;
                    }
                    return config;
                },

                'requestError'(rejection) {
                    angular.element(document.getElementById('controller')).scope().controller.loading = false;
                    return $q.reject(rejection);
                },

                'response'(response) {
                    angular.element(document.getElementById('controller')).scope().controller.loading = false;
                    if (response.headers('Content-Type') !== 'application/hal+json' &&
                            response.headers('Content-Type') !== 'application/json' ) {
                        window.location.href = response.config.url;
                    }
                    return response;
                },

                'responseError'(rejection) {
                    angular.element(document.getElementById('controller')).scope().controller.loading = false;
                    return $q.reject(rejection);
                }
            };
        });
    });

    function getLocation(href) {
        var location = document.createElement('a');
        location.href = href;
        // IE doesn't populate all link properties when setting .href with a
        // relative URL,
        // however .href will return an absolute URL which then can be used on
        // itself
        // to populate these additional fields.
        if (location.host === '') {
            location.href = location.href;
        }
        return location;
    }

    function parseResponseHeaders(headerStr) {
        var headers = {};
        if (!headerStr) {
          return headers;
        }
        var headerPairs = headerStr.split('\u000d\u000a');
        for (var i = 0, len = headerPairs.length; i < len; i++) {
          var headerPair = headerPairs[i];
          var index = headerPair.indexOf('\u003a\u0020');
          if (index > 0) {
            var key = headerPair.substring(0, index);
            var val = headerPair.substring(index + 2);
            headers[key] = val;
          }
        }
        return headers;
      }
    
    function linkHeaderParse(linkHeader) {
        var rval = {  };
        var links = linkHeader;
        if( links != null ) {
            for (var i = 0, len = links.length; i < len; i++) {
                var item = links[i].trim();
                var split = item.split(";");
                var href = split[0].trim();
                href = href.substring(1, href.length -1);
                var rel = split[1].trim();
                rel = rel.substring(5, rel.length -1);
                if( rval[rel] === undefined ) {
                    rval[rel] = [ href ];
                }
                else {
                    rval[rel] = rval[rel].concat(href);
                }
            };
        }
        return rval;
    }
    
    app.controller('ResourceController', function($scope, $http, $location, $window) {
        var controller = this;
        controller.loaded = false;
        controller.loading = false;
        controller.viewLoaded = false;
        controller.actionValues = {};
        controller.error = {};
        controller.lastForm = null;
        controller.href = $window.location.href;
        controller.debug = true;

        controller.processNavClick = function(event) {
            return false;
        };

        

        controller.doLoad = function(href) {
            controller.href = href;
            $http.get(href, {
                cache : false,
                eventHandlers: {
                    readystatechange: function(event) {
//                      console.log("change");
//                      console.log(event);
                      if (event.target.readyState == 4 && event.target.status == 200) {
//                          console.log('event.target.getAllResponseHeaders()', event.target.getAllResponseHeaders().split('\u000d\u000a'));          
//                          console.log('parseResponseHeaders(event.target.getAllResponseHeaders())', parseResponseHeaders(event.target.getAllResponseHeaders()));          
                          controller.resourceHeaders = parseResponseHeaders(event.target.getAllResponseHeaders());
                          if( controller.resourceHeaders["Link"] != null ) {
                              controller.resourceLinks = linkHeaderParse(controller.resourceHeaders["Link"].split(","));
                          }
                      }
                    }
                  },
            }).then(function successCallback(response) {
                controller.resource = response.data;
                controller.lastForm = null;
            }, controller.errorCallback);

        };

        controller.doLoadRoot = function(href) {
            $http.get(href, {
                cache : false
            }).then(function successCallback(response) {
                controller.root = response.data;
                controller.rootLinks = linkHeaderParse(response.headers("link").split(","));
            }, controller.errorCallback);

        };

        controller.load = function(href) {
            var currLoc = getLocation($location.absUrl());

            if (href.protocol === currLoc.protocol && href.host === currLoc.host) {
                var relativeHref = href.pathname + href.search + href.hash;
                $location.url(relativeHref);
            } else {

                $window.location.href = href;
            }
        };

        $scope.$on('$locationChangeStart', function(event, newUrl, oldUrl) {
        });

        $scope.$on('$locationChangeSuccess', function(event, newUrl, oldUrl) {
            if (newUrl !== null && oldUrl !== newUrl) {
                controller.doLoad(newUrl);
            }
        });

        controller.initRoot = function() {
            controller.root = JSON.parse(document.getElementById('init-root').textContent);
            // controller.rootLinks =
            // JSON.parse(document.getElementById('init-root-links').textContent);
        };

        controller.initResource = function() {
            controller.resource = JSON.parse(document.getElementById('init-resource').textContent);
            controller.resourceHeaders = JSON.parse(document.getElementById('init-resource-headers').textContent);
            if( "Link" in controller.resourceHeaders ) {
                controller.resourceLinks = linkHeaderParse(controller.resourceHeaders["Link"]);
            }
            else {
                controller.resourceLinks = {};
            }
        };

        controller.initRoot();
        controller.initResource();
        controller.loaded = true;    


        // credit to http://stackoverflow.com/a/12592693/269221
        $scope.isActive = function(path) {
            if ($location.path().substr(0, path.length) === path) {
                if (path === '/' && $location.path() === '/') {
                    return true;
                } else if (path === '/') {
                    return false;
                }
                return true;
            } else {
                return false;
            }
        };

        $scope.typeOf = function(value) {
            return typeof value;
        };

        $scope.resourceProperties = function() {
            var rval = {};
            if (controller.hasOwnProperty('resource')) {
                var keys = Object.keys(controller.resource);
                keys.forEach(function(key) {
                    if (key !== 'title' && key[0] !== '_') {
                        rval[key] = controller.resource[key];
                    }
                });
            }
            return rval;
        };

        $scope.resourceHasProperties = function() {
            return Object.keys($scope.resourceProperties()).length !== 0;
        };
        
        $scope.$on('$viewContentLoaded', function(event) {
            controller.viewLoaded = true;
          });
        
        controller.itemHeadings = function() {
            var keys = [];
            controller.resource._embedded.item.forEach(function(embeddedItem) {
                keys = keys.concat(Object.keys(embeddedItem));
            });
            var keySet = new Set(keys);
            keySet.delete("$$hashKey");
            keys = Array.from(keySet);
            return keys;
        };
        
        controller.linkedItem = function(item) {
            var filtered = controller.resource._embedded.item.filter(function(embeddedItem) {
               return embeddedItem._links.self.href === item.href;
            });
            return filtered.length > 0 ? filtered[0] : item;
        };
        
        controller.itemNavLinks = function(_links) {
            var itemRels = [ 'first', 'prev', 'next', 'last', 'current' ];
            var rval = {};
            Object.keys(_links).filter(function(key) {
                return itemRels.includes(key);            
            }).forEach(function(key) {
                rval[key] = _links[key];
            });
            return rval;
        };
        
        controller.typeOf = function(value) {
            return typeof value;
        };

        // credit to http://stackoverflow.com/a/7220510/269221
        controller.pretty = function(json) {
            return JSON.stringify(json, null, 2);
        };
    });
});
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
        $httpProvider.defaults.headers.common["X-Requested-With"] = 'XMLHttpRequest';

        $httpProvider.interceptors.push(function($q) {
            return {
                'request'(config) {
                    angular.element(document.getElementById('controller')).scope().controller.loading = true;
                    angular.element(document.getElementById('controller')).scope().controller.viewLoaded = false;
                    config.headers['Accept'] = 'application/json;q=1.0,*/*;q=0.1';
                    config.headers['Cache-Control'] = 'max-stale';
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
                    console.log(response);
                     if ( response.status != 204 &&
                            response.headers('Content-Type') !== 'application/hal+json' &&
                            response.headers('Content-Type') !== 'application/json' &&
                                response.headers('Content-Type') !== 'application/json;charset=UTF-8') {
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
                var title = split[2].trim();
                title = title.substring(7, title.length -1);
                var linkObj = { "href": href, "title": title };
                if( rval[rel] === undefined ) {
                    rval[rel] = [ linkObj ];
                }
                else {
                    rval[rel] = rval[rel].concat(linkObj);
                }
            };
        }
        return rval;
    }
    
    // from https://gist.github.com/justinmc/d72f38339e0c654437a2
    function scrollTo(eID) {

        // This scrolling function
        // is from
        // http://www.itnewb.com/tutorial/Creating-the-Smooth-Scroll-Effect-with-JavaScript
        
        var i;
        var startY = currentYPosition();
        var stopY = elmYPosition(eID) - 100;
        var distance = stopY > startY ? stopY - startY : startY - stopY;
        if (distance < 100) {
            scrollTo(0, stopY); return;
        }
        var speed = Math.round(distance / 50);
        if (speed >= 20) speed = 20;
        var step = Math.round(distance / 25);
        var leapY = stopY > startY ? startY + step : startY - step;
        var timer = 0;
        if (stopY > startY) {
            for (i = startY; i < stopY; i += step) {
                setTimeout('window.scrollTo(0, '+leapY+')', timer * speed);
                leapY += step; if (leapY > stopY) leapY = stopY; timer++;
            } return;
        }
        for (i = startY; i > stopY; i -= step) {
            setTimeout('window.scrollTo(0, '+leapY+')', timer * speed);
            leapY -= step; if (leapY < stopY) leapY = stopY; timer++;
        }
    }
    
    /*
     * currentYPosition -
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */
    function currentYPosition() {
        // Firefox, Chrome, Opera, Safari
        if (window.pageYOffset) {
            return window.pageYOffset;
        }
        // Internet Explorer 6 - standards mode
        if (document.documentElement && document.documentElement.scrollTop) {
            return document.documentElement.scrollTop;
        }
        // Internet Explorer 6, 7 and 8
        if (document.body.scrollTop) {
            return document.body.scrollTop;
        }
        return 0;
    }

    /*
     * scrollTo - ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */
    function elmYPosition(eID) {
        var elm = document.getElementById(eID);
        var y = elm.offsetTop;
        var node = elm;
        while (node.offsetParent && node.offsetParent != document.body) {
            node = node.offsetParent;
            y += node.offsetTop;
        } return y;
    }
    
    app.controller('ResourceController', function($rootScope, $scope, $http, $location, $window, $document) {
        var controller = this;
        controller.loaded = false;
        controller.loading = false;
        controller.viewLoaded = false;
        controller.actionValues = {};
        controller.error = {};
        controller.href = $window.location.href;
        controller.debug = true;

        
        controller.processNavClick = function(event) {
            return false;
        };

        controller.errorCallback = function(response) {
            if( response.status == 401 ) {
                console.log("Unauthentiated. Redirecting to: /", );
                $location.url("/");
            }
            else {
                window.location.href = response.config.url;
            }
        }
        

        controller.doLoad = function(href) {
            controller.href = href;
            var links = controller.resourceLinks;
            $http.get(href, {
                cache : false// ,
            }).then(function successCallback(response) {
                if( response.status == 302 ) {
                    console.log("redirecting to: ", response.headers('Location'));
                    $location.url(response.headers('Location'));
                }
                else {
                    controller.resource = response.data;
                    controller.resourceHeaders = response.headers();
                    if( controller.resourceHeaders["link"] != null ) {
                        controller.resourceLinks = linkHeaderParse(controller.resourceHeaders["link"].split(","));
                    }
                }
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
            controller.rootHeaders = JSON.parse(document.getElementById('init-root-headers').textContent);
            console.log("root headers", controller.rootHeaders);
            if( "Link" in controller.rootHeaders ) {
                controller.rootLinks = linkHeaderParse(controller.rootHeaders["Link"]);
            }
            else {
                controller.rootLinks = {};
            }
            console.log("root links", controller.rootLinks);
        };

        $scope.target = function(index) {
            $location.hash(index);
            scrollTo(index);
        }

        controller.initResource = function() {
            controller.resource = JSON.parse(document.getElementById('init-resource').textContent);
            controller.resourceHeaders = JSON.parse(document.getElementById('init-resource-headers').textContent);
            console.log("resource headers", controller.resourceHeaders);
            if( "link" in controller.resourceHeaders ) {
                controller.resourceLinks = linkHeaderParse(controller.resourceHeaders["link"]);
            }
            else {
                controller.resourceLinks = {};
            }
            console.log("resource links", controller.resourceLinks);
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
            var itemRels = [ 'first', 'prev', 'next' ];
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

        var authenticate = function(credentials, callback) {

            var headers = credentials ? {authorization : "Basic "
                + btoa(credentials.username + ":" + credentials.password)
            } : {};

            $http.get('user', {headers : headers}).then(function(response) {
                console.log(response.data);
                $rootScope.user = response.data;
                if (response.data.name) {
                    $rootScope.authenticated = true;
                    
                  } else {
                    $rootScope.authenticated = false;
                  }
                  callback && callback();
                }, function() {
              $rootScope.authenticated = false;
              callback && callback();
            });

          };

          authenticate();
          $scope.credentials = {};
          $scope.login = function() {
              authenticate($scope.credentials, function() {
                if ($rootScope.authenticated) {
                  $scope.error = false;
                } else {
                  $scope.error = true;
                }
              });
          };

        
        $scope.logout = function() {
            $scope.credentials = {};
            $rootScope.user = {};
            $http.post('logout', {}).then(function() {
              $rootScope.authenticated = false;
              $location.url("/");
            },function(data) {
              $rootScope.authenticated = false;
              $location.url("/");
            });
          }
    });
    
});
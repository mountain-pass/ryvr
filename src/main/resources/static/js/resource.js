 /* global angular */
 /* global document */
 /* global console */
 /* global window */
 /* global window */

var app = angular.module('ryvr', []);

app.config(function($locationProvider, $httpProvider) {
    $locationProvider.html5Mode(true);
    $httpProvider.interceptors.push(function($q) {
        return {
            'request'(config) {
                angular.element(document.getElementById('controller')).scope().controller.loading = true;
                // app.controller.loading = true;
                // config.headers.get['Accept'] =
                config.headers['Accept'] = 'application/hal+json;q=1,application/json;q=0.8,*/*;q=0.1';
                return config;
            },

            'requestError'(rejection) {
                angular.element(document.getElementById('controller')).scope().controller.loading = false;
                return $q.reject(rejection);
            },

            'response'(response) {
                angular.element(document.getElementById('controller')).scope().controller.loading = false;
                if (response.headers('Content-Type') !== 'application/hal+json') {
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

app.controller('ResourceController', function($scope, $http, $location, $window) {
    var controller = this;
    controller.loaded = false;
    controller.loading = false;
    controller.actionValues = {};
    controller.error = {};
    controller.lastForm = null;
    controller.href = $window.location.href;
    controller.debug = false;

    controller.processNavClick = function(event) {
        return false;
    };

    controller.successCallback = function(response) {

        delete controller.actionValues[controller.lastForm.action.name];

        if (response.status === 201) {
            var location = getLocation(response.headers('Location'));
            controller.load(location);

        } else if (response.status === 200) {
            controller.resource = response.data;
        } else if (response.status === 204) {
            var href = $window.location.href;
            href = href.substring(0, href.lastIndexOf('/'));
            controller.load(href);

        } else {
            alert('TODO: handle ' + response.status + ' responses');
        }
    };

    controller.errorCallback = function(response) {
        var error = response;
        var errors = [];
        if (response.data && response.data.errors) {
            error = response.data.error;
            errors = response.data.errors;
        }
        if (controller.lastForm != null) {

            controller.error[controller.lastForm.action.name] = {
                error,
                errors
            };
            // todo iterate over errors and
            // update form accordingly
            Object.keys(errors).forEach(function(key, index) {
                controller.lastForm.action.fields[key].error = response.data.errors[key];
            });
            controller.lastForm = null;
        } else {
            alert('TODO: handle error', error);
        }
    };

    controller.doLoad = function(href) {
        controller.href = href;
        $http.get(href, {
            cache : false
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
    };

    controller.initResource = function() {
        controller.resource = JSON.parse(document.getElementById('init-resource').textContent);
    };

    controller.initRoot();
    controller.initResource();
    controller.loaded = true;    

    controller.processForm = function(form) {
        controller.loading = true;
        var action = form.action;
        var href = $(location).attr('href');

        var method = action.method || 'GET';

        var values = controller.actionValues[action.name];
        var requestData = null;
        var requestParams = null;
        if (values !== null) {
            if (method === 'GET') {
                requestParams = values;
            } else {
                requestData = $.param(values);
            }
        }
        var request = {
            method,
            url : href,
            data : requestData, // pass in data as strings
            params : requestParams,
            headers : {
                'Content-Type' : action.type || 'application/x-www-form-urlencoded'
            }
        };
        controller.lastForm = form;

        if (method === 'GET') {
            if (values !== null) {
                href = href + '?' + $.param(values);
            }
            controller.load(getLocation(href));
        } else {
            $http(request, {
                cache : false
            }).then(controller.successCallback, controller.errorCallback);
        }
        return false;
    };

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
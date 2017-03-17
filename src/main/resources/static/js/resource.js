var app = angular.module('ryvr', []);

app.config(function($locationProvider, $httpProvider) {
    $locationProvider.html5Mode(true);
    // $httpProvider.defaults.cache=false;
    // $httpProvider.defaults.headers.common.Accept =
    // 'application/vnd.siren+json';
    // //initialize get if not there
    // if (!$httpProvider.defaults.headers.get) {
    // $httpProvider.defaults.headers.get = {};
    // }
    //
    // // Answer edited to include suggestions from comments
    // // because previous version of code introduced browser-related errors
    //
    // //disable IE ajax request caching
    // $httpProvider.defaults.headers.get['If-Modified-Since'] = 'Mon, 26 Jul
    // 1997 05:00:00 GMT';
    // // extra
    // $httpProvider.defaults.headers.get['Pragma'] = 'no-cache';

    $httpProvider.interceptors.push(function($q) {
        return {
            'request' : function(config) {
                angular.element(document.getElementById('controller')).scope().controller.loading = true;
                // app.controller.loading = true;
                // config.headers.get['Accept'] =
                config.headers['Accept'] = 'application/hal+json;q=1,application/json;q=0.8,*/*;q=0.1';
                console.log('request', new Date());
                console.log(config);
                return config;
            },

            'requestError' : function(rejection) {
                angular.element(document.getElementById('controller')).scope().controller.loading = false;
                console.log('requestError', new Date());
                console.log(rejection);
                return $q.reject(rejection);
            },

            'response' : function(response) {
                angular.element(document.getElementById('controller')).scope().controller.loading = false;
                console.log('response', new Date());
                console.log(response);
                if (response.headers("Content-Type") != "application/hal+json") {
                    window.location.href = response.config.url;
                }
                return response;
            },

            'responseError' : function(rejection) {
                angular.element(document.getElementById('controller')).scope().controller.loading = false;
                console.log('responseError', new Date());
                console.log(rejection);
                return $q.reject(rejection);
            }
        };
    });
});

function getLocation(href) {
    console.log("getLocation(" + href + ")");
    var location = document.createElement("a");
    location.href = href;
    // IE doesn't populate all link properties when setting .href with a
    // relative URL,
    // however .href will return an absolute URL which then can be used on
    // itself
    // to populate these additional fields.
    if (location.host == "") {
        location.href = location.href;
    }
    return location;
};

app.controller('ResourceController', function($scope, $http, $location, $window) {
    var controller = this;
    var loaded = true;
    var loading = true;
    var init = true;
    controller.actionValues = {};
    controller.error = {};
    controller.lastForm = null;
    controller.href = $window.location.href
    controller.debug = true;

    controller.processNavClick = function(event) {
        return false;
        // console.log("processNavClick");
        // console.log(event);
        // $http.get(event.target.href, {
        // cache: false
        // }).success(function(data) {
        // controller.resource = data;
        // });
    };

    controller.successCallback = function(response) {

        delete controller.actionValues[controller.lastForm.action.name];

        if (response.status == 201) {
            var location = getLocation(response.headers("Location"));
            controller.load(location);

        } else if (response.status == 200) {
            controller.resource = response.data;
        } else if (response.status == 204) {
            var href = $window.location.href;
            href = href.substring(0, href.lastIndexOf('/'));
            console.log("reloading:", href)
            controller.load(href);

        } else {
            alert("TODO: handle " + response.status + " responses");
        }
    }

    controller.errorCallback = function(response) {
        console.log("error response", response);
        var error = response;
        var errors = [];
        if (response.data && response.data.errors) {
            error = response.data.error;
            errors = response.data.errors;
        }
        if (controller.lastForm != null) {

            controller.error[controller.lastForm.action.name] = {
                "error" : error,
                "errors" : errors
            };
            console.log(errors);
            console.log(controller.lastForm.action.fields);
            // todo iterate over errors and
            // update form accordingly
            Object.keys(errors).forEach(function(key, index) {
                controller.lastForm.action.fields[key].error = response.data.errors[key];
            });
            console.log(controller.lastForm.action.fields);
            controller.lastForm = null;
        } else {
            alert("TODO: handle error", e);
        }
    }

    controller.doLoad = function(href) {
        controller.href = href
        $http.get(href, {
            cache : false
        }).then(function successCallback(response) {
            controller.resource = response.data;
            controller.lastForm = null;
        }, controller.errorCallback);

    }

    controller.doLoadRoot = function(href) {
        $http.get(href, {
            cache : false
        }).then(function successCallback(response) {
            controller.root = response.data;
        }, controller.errorCallback);

    }

    controller.load = function(href) {
        console.log("setting location", href.href, new Date());
        var currLoc = getLocation($location.absUrl());
        console.log("currLoc.protocol", currLoc.protocol, new Date());
        console.log("currLoc.host", currLoc.host, new Date());
        console.log("href.protocol", href.protocol, new Date());
        console.log("href.host", href.host, new Date());
        console.log("href.host", href.host, new Date());
        console.log("href.protocol == currLoc.protocol", href.protocol == currLoc.protocol, new Date());
        console.log("href.host == currLoc.host", href.host == currLoc.host, new Date());

        if (href.protocol == currLoc.protocol && href.host == currLoc.host) {
            var relativeHref = href.pathname + href.search + href.hash;
            console.log("relativeHref", relativeHref, new Date());
            $location.url(relativeHref);
        } else {

            $window.location.href = href;
        }
    }

    $scope.$on('$locationChangeStart', function(event, newUrl, oldUrl) {
        console.log('$locationChangeStart:', oldUrl, " -> ", newUrl, new Date());
    });

    $scope.$on('$locationChangeSuccess', function(event, newUrl, oldUrl) {
        console.log('$locationChangeSuccess:', oldUrl, " -> ", newUrl, new Date());
        if (newUrl != null) {
            controller.doLoad(newUrl);
        }
    });

    console.log("initial load", new Date());
    controller.doLoadRoot(".");
    controller.doLoad($window.location.href);
    console.log("initial requested", new Date());

    controller.processForm = function(form) {
        controller.loading = true;
        console.log("processForm");
        console.log(form);
        var action = form.action;
        href = $(location).attr('href');

        var method = action.method || "GET";
        console.log(method);
        console.log(method == "GET");

        var values = controller.actionValues[action.name];
        console.log(values);
        var requestData = null;
        var requestParams = null;
        if (values != null) {
            if (method == "GET") {
                requestParams = values;
            } else {
                requestData = $.param(values);
            }
        }
        var request = {
            method : method,
            url : href,
            data : requestData, // pass in data as strings
            params : requestParams,
            headers : {
                'Content-Type' : action.type || "application/x-www-form-urlencoded"
            }
        };
        controller.lastForm = form;

        if (method == "GET") {
            if (values != null) {
                href = href + '?' + $.param(values);
            }
            console.log("starting GET request", href, new Date());
            controller.load(getLocation(href));
        } else {
            console.log("starting " + method + " request", request, new Date());
            $http(request).then(controller.successCallback, controller.errorCallback);
        }
        return false;
    };

    // credit to http://stackoverflow.com/a/12592693/269221
    $scope.isActive = function(path) {
        if ($location.path().substr(0, path.length) == path) {
            if (path == "/" && $location.path() == "/") {
                return true;
            } else if (path == "/") {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    $scope.resourceProperties = function() {
        var rval = {};
        if (controller.hasOwnProperty('resource')) {
            var keys = Object.keys(controller.resource);
            keys.forEach(function(key) {
                if (key != 'title' && key[0] != "_") {
                    rval[key] = controller.resource[key];
                }
            });
        }
        return rval;
    }

    $scope.resourceHasProperties = function() {
        return Object.keys($scope.resourceProperties()).length != 0;
    }

    // credit to http://stackoverflow.com/a/7220510/269221
    controller.pretty = function(json) {
        return JSON.stringify(json, null, 2);
    }
});
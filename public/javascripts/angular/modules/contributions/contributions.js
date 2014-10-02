angular.module('contributions', ['ui.bootstrap', 'ngResource', 'ngRoute'])
    .config(function($routeProvider) {
        $routeProvider
            .when('/dashboard', {
                controller:'DashboardCtrl',
                templateUrl:'/assets/javascripts/angular/modules/contributions/templates/dashboard.html'
            })
            .when('/create', {
                controller:'CreateCtrl',
                templateUrl:'/assets/javascripts/angular/modules/contributions/templates/create.html'
            })
            .when('/distrib', {
                controller:'DistribCtrl',
                templateUrl:'/assets/javascripts/angular/modules/contributions/templates/distrib.html'
            })
            .otherwise({
                redirectTo:'/dashboard'
            });
    })
    .controller('DashboardCtrl', function($scope, $http) {
        $http.get("/api/contributions").success(function(data) {
            $scope.contributions = data;
        });

    })
    .controller('CreateCtrl', function($scope, $http) {
        $http.get("/api/members").success(function(data) {
            $scope.members = data;
        });
        $http.get("/api/contributions?type=template").success(function(data) {
            $scope.contributionTemplates = data;
        });
    })
    .controller('DistribCtrl', function($scope) {

    })
    .controller("HeaderCtrl", function($scope, $location) {

        $scope.isActive = function (viewLocation) {
            return viewLocation === $location.path();
        };
    });
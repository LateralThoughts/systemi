angular.module('members', ['ui.bootstrap', 'ngResource', 'ngRoute'])
    .config(function($routeProvider) {
        $routeProvider
            .when('/dashboard', {
                controller:'DashboardCtrl',
                templateUrl:'/assets/javascripts/angular/modules/members/templates/dashboard.html'
            })
            .when('/create', {
                controller:'CreateCtrl',
                templateUrl:'/assets/javascripts/angular/modules/members/templates/create.html'
            })
            .when('/distrib', {
                controller:'DistribCtrl',
                templateUrl:'/assets/javascripts/angular/modules/members/templates/distrib.html'
            })
            .otherwise({
                redirectTo:'/dashboard'
            });
    })
    .controller('DashboardCtrl', function($scope, $http) {

    })
    .controller('CreateCtrl', function($scope) {

    })
    .controller('DistribCtrl', function($scope) {

    })
    .controller("HeaderCtrl", function($scope, $location) {

        $scope.isActive = function (viewLocation) {
            return viewLocation === $location.path();
        };
    });
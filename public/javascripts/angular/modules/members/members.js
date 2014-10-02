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
            .otherwise({
                redirectTo:'/dashboard'
            });
    })
    .controller('DashboardCtrl', function($scope, $http) {
        $http.get("/api/members").success(function(data) {
            $scope.members = data;
        });
        $http.get("/api/accounts").success(function(data) {
            $scope.accounts = data;
        });
    })
    .controller('CreateCtrl', function($scope, $http) {
        $http.get("/api/members").success(function(data) {
            $scope.members = data;
        });
    })
    .controller("HeaderCtrl", function($scope, $location) {

        $scope.isActive = function (viewLocation) {
            return viewLocation === $location.path();
        };
    });
angular.module('expense', ['ui.bootstrap', 'ngResource', 'ngRoute'])
    .config(function($routeProvider) {
        $routeProvider
            .when('/create', {
                controller:'CreateCtrl',
                templateUrl:'/assets/javascripts/angular/modules/expense/templates/create.html'
            })
            .when('/dashboard', {
                controller:'DashboardCtrl',
                templateUrl:'/assets/javascripts/angular/modules/expense/templates/dashboard.html'
            })
            .otherwise({
                redirectTo:'/dashboard'
            });
    })
    .controller('DashboardCtrl', function($scope, $http) {
        var reload = function(scope) {
                $http.get("/api/accounts").success(function(data){
                    scope.accounts = data;
                });
            };
            reload($scope);
        $http.get("/api/expenses").success(function(data){
            $scope.expenses = data;
        })
    })
    .controller('CreateCtrl', function($scope, $http) {
        var reload = function(scope) {
            $http.get("/api/accounts").success(function(data){
                scope.accounts = data;
            });
        };
        reload($scope);
    })
    .controller("HeaderCtrl", function($scope, $location) {

        $scope.isActive = function (viewLocation) {
            return viewLocation === $location.path();
        };
    });
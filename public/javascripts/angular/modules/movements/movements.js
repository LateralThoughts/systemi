angular.module('movements', ['ui.bootstrap', 'ngResource', 'ngRoute'])
    .config(function($routeProvider) {
        $routeProvider
            .when('/create', {
                controller:'CreateCtrl',
                templateUrl:'/assets/javascripts/angular/modules/movements/templates/create.html'
            })
            .when('/dashboard', {
                controller:'DashboardCtrl',
                templateUrl:'/assets/javascripts/angular/modules/movements/templates/dashboard.html'
            })
            .otherwise({
                redirectTo:'/dashboard'
            });
    })
    .controller('DashboardCtrl', function($scope, $http) {
        var reload = function(scope) {
                $http.get("/api/accounts").success(function(data){
                    _.map(data, function(item) {
                        if (item.stakeholder.user.fullName) {
                            item.fullName = item.name + " (" + item.stakeholder.user.fullName + ")";
                        } else {
                            item.fullName = item.name + " (" + item.stakeholder.underlying + ")";
                        }
                    });
                    scope.accounts = data;
                });
            };
            reload($scope);
        $http.get("/api/movements").success(function(data){
            $scope.movements = data;
        })
    })
    .controller('CreateCtrl', function($scope, $http) {
        var reload = function(scope) {
            $http.get("/api/accounts").success(function(data){
                _.map(data, function(item) {
                    if (item.stakeholder.user.fullName) {
                        item.fullName = item.name + " (" + item.stakeholder.user.fullName + ")";
                    } else {
                        item.fullName = item.name + " (" + item.stakeholder.underlying + ")";
                    }
                });
                scope.accounts = data;
            });
        };
        reload($scope);
        $scope.movement = {};

        $scope.submit = function() {
            $http.post("/api/movements", JSON.stringify($scope.movement));
        }
    })
    .controller("HeaderCtrl", function($scope, $location) {

        $scope.isActive = function (viewLocation) {
            return viewLocation === $location.path();
        };
    });
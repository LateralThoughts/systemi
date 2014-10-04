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
        $http.get("/api/members").success(function(members) {
            $scope.members = members;
            $http.get("/api/accounts").success(function(accounts) {

                // associate members with their accounts
                for (var i=0; i<$scope.members.length; i++) {
                    $scope.members[i].accounts = [];
                    for (var j=0; j<accounts.length; j++) {
                        if (accounts[j].stakeholder.user.userId == $scope.members[i].userId) {
                            $scope.members[i].accounts.push(accounts[j]);
                        }
                    }
                }
            });
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
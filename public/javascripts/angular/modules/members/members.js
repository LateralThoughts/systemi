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

        $http.get("/api/members").success(setupMembers);

        function setupMembers(members) {
            $scope.members = members;
            $http.get("/api/accounts").success(associateMembersWithAccounts);
        }

        function associateMembersWithAccounts(accounts) {
            for (var i = 0; i < $scope.members.length; i++) {
                addAccountsToMember(i, accounts);
            }
        }

        function addAccountsToMember(memberPosition, accounts) {
            $scope.members[memberPosition].accounts = accounts.filter(function(account) {
                return account.stakeholder.user.userId === $scope.members[memberPosition].userId;
            })
        }
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
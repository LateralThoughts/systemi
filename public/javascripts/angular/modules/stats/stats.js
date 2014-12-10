angular.module('stats', ['ui.bootstrap'])
    .controller('DashboardCtrl', function($scope, $http) {
       $http.get("/api/stats").success(function (data) {
           $scope.statsByAccount = data;
       });
    });
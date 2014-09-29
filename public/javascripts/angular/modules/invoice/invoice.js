angular.module('invoice', ['ui.bootstrap', 'ngResource', 'ngRoute'])
    .config(function($routeProvider) {
        $routeProvider
            .when('/pending', {
                controller:'ListCtrl',
                templateUrl:'/assets/javascripts/angular/modules/invoice/templates/list.html'
            })
            .when('/create', {
                controller:'CreateCtrl',
                templateUrl:'/assets/javascripts/angular/modules/invoice/templates/create.html'
            })
            .otherwise({
                redirectTo:'/pending'
            });
    })
    .controller('ListCtrl', function($scope, $http) {
        $http.get("/api/invoices").success(function(data) {
            $scope.invoices = data;
        });
    })
    .controller('CreateCtrl', function($scope) {
        $scope.shouldUpload = true;

        var initTaskLineData = {
            'invoiceDescription': '',
            'invoiceDays': '',
            'invoiceDailyRate': '',
            'invoiceTaxRate': '20.0',
            'addButtonVisible': true,
            'deleteButtonVisible': false
        };

        $scope.tasklines = [
            initTaskLineData
        ];

        $scope.addTask = function(){
            $scope.tasklines[$scope.tasklines.length - 1]['addButtonVisible'] = false;
            $scope.tasklines[$scope.tasklines.length - 1]['deleteButtonVisible'] = false;
            $scope.tasklines.push({
                'invoiceDescription': '',
                'invoiceDays': '',
                'invoiceDailyRate': '',
                'invoiceTaxRate': '20.0',
                'addButtonVisible': true,
                'deleteButtonVisible': true
            });
        };

        $scope.deleteTask = function(){
            $scope.tasklines.pop();
            $scope.tasklines[$scope.tasklines.length - 1]['addButtonVisible'] = true;
            $scope.tasklines[$scope.tasklines.length - 1]['deleteButtonVisible'] = ($scope.tasklines.length>1);
        }
    }).controller("HeaderCtrl", function($scope, $location) {

        $scope.isActive = function (viewLocation) {
            return viewLocation === $location.path();
        };
    });
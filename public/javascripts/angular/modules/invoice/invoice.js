angular.module('invoice', ['ui.bootstrap', 'ngResource', 'ngRoute'])
    .config(function($routeProvider) {
        $routeProvider
            .when('/', {
                controller:'ListCtrl',
                templateUrl:'/assets/javascripts/angular/modules/invoice/templates/list.html'
            })
            .when('/created', {
                controller:'CreateCtrl',
                templateUrl:'/assets/javascripts/angular/modules/invoice/templates/detail.html'
            })
            .otherwise({
                redirectTo:'/'
            });
    })
    .controller('ListCtrl', function($scope) {
        $.get("/api/invoices").success(function(data) {
            console.log(data);
            $scope.invoices = data;
        });
    })
    .controller('CreateCtrl', function($scope, $location, $timeout, Projects) {
        $scope.save = function() {
            Projects.$add($scope.project).then(function(data) {
                $location.path('/');
            });
        };
    }).controller('InvoiceController', function ($scope) {
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
});
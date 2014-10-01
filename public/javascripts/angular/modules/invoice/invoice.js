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
            .when('/in-progress', {
                controller:'InProgressCtrl',
                templateUrl:'/assets/javascripts/angular/modules/invoice/templates/in-progress.html'
            })
            .when('/paid', {
                controller:'PaidCtrl',
                templateUrl:'/assets/javascripts/angular/modules/invoice/templates/paid.html'
            })
            .when('/accounts', {
                controller:'AccountsCtrl',
                templateUrl:'/assets/javascripts/angular/modules/invoice/templates/accounts.html'
            })
            .otherwise({
                redirectTo:'/pending'
            });
    })
    .controller('ListCtrl', function($scope, $http) {
        var reload = function(scope) {
            $http.get("/api/invoices?status=affected&exclude=true").success(function (data) {
                scope.invoices = data;
            });
            $http.get("/api/accounts").success(function(data){
                scope.accounts = data;
            });
        };
        reload($scope);
        $scope.affect = function(invoice, accountOid) {
          $http.post("/api/invoices/" + invoice._id.$oid + "/affect/" + accountOid).success(function() { reload($scope) })
        };
    })
    .controller('InProgressCtrl', function($scope, $http) {
        var reload = function(scope) {
            $http.get("/api/invoices?status=paid&exclude=true").success(function (data) {
                scope.invoices = data;
            });
        };
        reload($scope);

        $scope.pay = function(invoice) {
            $http.post("/api/invoices/" + invoice._id.$oid + "/status/paid").success(function(){ reload($scope)})
        }
    })
    .controller('PaidCtrl', function($scope, $http) {
        var reload = function(scope) {
            $http.get("/api/invoices?status=paid").success(function (data) {
                scope.invoices = data;
            });
        };
        reload($scope);
        $scope.revert = function(invoice) {
            $http.post("/api/invoices/" + invoice._id.$oid + "/status/unpaid").success(function(){ reload($scope)})

        }
    })
    .controller('AccountsCtrl', function($scope, $http) {
        var reload = function(scope) {
            $http.get("/api/accounts").success(function (data) {
                scope.accounts = data;
            });
        };
        reload($scope);
        $scope.create = function(name) {
            var account = {"name" : name};
            $http.post("/api/accounts", account).success(function(){ reload($scope)})

        }
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
angular.module('invoice', ['ui.bootstrap', 'ngResource', 'ngRoute', 'client-search'])
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
            .otherwise({
                redirectTo:'/pending'
            });
    })
    .controller('ListCtrl', function($scope, $http) {
        var reload = function(scope) {
            $http.get("/api/invoices?status=affected&exclude=true").success(function (data) {
                _.map(data, function(item) {
                    item.totalHT = function() {
                        var lines = this.invoice.invoice;
                        var value = _.reduce(lines, function(sum, line) { sum += line.dailyRate * line.days; return sum}, 0);
                        return value;
                    }
                });
                scope.invoices = data;
            });
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
        $scope.open = function(invoice) {
            $scope.affectations = [{
                value: invoice.totalHT()
            }];
            $scope.invoice = invoice;
            $('#affectationModal').modal('show');
        };
        $scope.affect = function(affectations, invoice) {
            _.map(affectations, function(item) { delete item.$$hashKey; });
            $http.post("/api/affectations/" + invoice._id.$oid, JSON.stringify(affectations))
                .success(function(data) {
                    reload($scope);
                    $('#affectationModal').modal('hide');
                });
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

        $scope.client = null;

    }).controller("HeaderCtrl", function($scope, $location) {

        $scope.isActive = function (viewLocation) {
            return viewLocation === $location.path();
        };
    });

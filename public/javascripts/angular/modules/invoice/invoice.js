angular.module('invoice', ['ui.bootstrap', 'ngResource', 'ngRoute', 'default-values', 'client-select'])
    .config(function($routeProvider) {
        $routeProvider
            .when('/list', {
                controller:'ListCtrl',
                templateUrl:'/assets/javascripts/angular/modules/invoice/templates/list.html'
            })
            .when('/create', {
                controller:'CreateCtrl',
                templateUrl:'/assets/javascripts/angular/modules/invoice/templates/create.html'
            })
            .otherwise({
                redirectTo:'/list'
            });
    })
    .directive('ngConfirmClick', [
        function(){
            return {
                priority: -1,
                restrict: 'A',
                link: function(scope, element, attrs){
                    element.bind('click', function(e){
                        var message = attrs.ngConfirmClick;
                        if(message && !confirm(message)){
                            e.stopImmediatePropagation();
                            e.preventDefault();
                        }
                    });
                }
            }
        }
    ])
    .directive('radioDetectChange', [function radioDetectChange() {

        return {
            replace: false,
            require: 'ngModel',
            scope: false,
            link: function (scope, element, attrs, ngModelCtrl) {
                element.on('change', function () {
                    scope.$apply(function () {
                        ngModelCtrl.$setViewValue(element[0].type.toLowerCase() == 'radio' ? element[0].value : element[0].checked);                    });
                });
            }
        };
    }])
    .controller('ListCtrl', ['$scope','$http','InvoicesService', 'default_contractor',function($scope, $http, invoicesService, default_contractor) {

        $scope.isCreated =  function(invoice) {
          return invoice.status === "created";
        };

        $scope.isAllocated =  function(invoice) {
            return invoice.status === "allocated";
        };

        $scope.isPaid =  function(invoice) {
            return invoice.status === "paid";
        };

        $scope.isCanceled =  function(invoice) {
            return invoice.status === "canceled";
        };

        $scope.isInProgress = function(invoice) {
            return $scope.isCreated(invoice) || $scope.isAllocated(invoice);
        };

        $scope.isFinished = function(invoice) {
            return $scope.isCanceled(invoice) || $scope.isPaid(invoice);
        };

        var filter = function($scope) {
            var filteredInvoices = $scope.invoices.slice();

            if ($scope.selectedClient !== $scope.clients[0]) {
                filteredInvoices = filteredInvoices.filter(function(element) {
                    return element.invoice.client.name === $scope.selectedClient;
                })
            }

            if ($scope.selectedCreator !== $scope.creators[0]) {
                filteredInvoices = filteredInvoices.filter(function(element) {
                    return element.statuses[0].email === $scope.selectedCreator.content;
                })
            }

            if ($scope.selectedStatus != "Tous") {
                switch($scope.selectedStatus) {
                    case "En Cours":
                        filteredInvoices = filteredInvoices.filter($scope.isInProgress);
                        break;
                    case "Créée":
                        filteredInvoices = filteredInvoices.filter($scope.isCreated);
                        break;
                    case "Affectée":
                        filteredInvoices = filteredInvoices.filter($scope.isAllocated);
                        break;
                    case "Terminée":
                        filteredInvoices = filteredInvoices.filter($scope.isFinished);
                        break;
                    case "Payée":
                        filteredInvoices = filteredInvoices.filter($scope.isPaid);
                        break;
                    case "Annulée":
                        filteredInvoices = filteredInvoices.filter($scope.isCanceled);
                        break;
                }
            }

            return filteredInvoices;
        };

        $scope.creators = [{label: "Tous", content: "Tous"}];
        $scope.selectedCreator = {label: default_contractor.name, content: default_contractor.email};

        $scope.clients = ["Tous"];
        $scope.selectedClient = $scope.clients[0];

        $scope.inProgressStatuses = ["En Cours", "Créée", "Affectée"];
        $scope.finishedStatuses = ["Terminée", "Payée", "Annulée"];
        $scope.selectedStatus = $scope.inProgressStatuses[0];

        $http.get("/api/members").success(function(data){
            data.reduce(function(creators, item) {

                creators.push({label: item.fullName, content: item.email});
                return creators;
            }, $scope.creators);
        });

        $http.get("/api/clients").success(function(data){
            data.reduce(function(clients, item) {
                clients.push(item.name);
                return clients;
            }, $scope.clients);
        });

        var reload = function(scope) {

            $http.get("/api/invoices").success(function (data) {
                _.map(data, function(item) {
                    item.totalHT = function() {
                        var lines = this.invoice.invoice;
                        return _.reduce(lines, function(sum, line) { sum += line.dailyRate * line.days; return sum}, 0);
                    };

                    item.totalTTC = function() {
                        if (this.invoice.withTaxes) {
                            var lines = this.invoice.invoice;
                            return _.reduce(lines, function(sum, line) { sum += Math.round((line.dailyRate * line.days * (1 + line.taxRate/100))*100)/100; return sum}, 0);
                        } else {
                            return item.totalHT();
                        }

                    }
                });
                scope.invoices = data;
                scope.filteredInvoices = filter(scope);

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

        $scope.filterCreator = function(newCreator) {
            $scope.selectedCreator = newCreator;
            $scope.filteredInvoices = filter($scope);
        };

        $scope.filterClient = function(newClient) {
            $scope.selectedClient = newClient;
            $scope.filteredInvoices = filter($scope);
        };

        $scope.filterStatus = function(newStatus) {
            $scope.selectedStatus = newStatus;
            $scope.filteredInvoices = filter($scope);
        };

        $scope.getStatusStyle = function(item) {
            if ($scope.isCanceled(item) || $scope.isPaid(item)) {
                return "success";
            }

            if ((item.statuses[0].createdAt + (item.invoice.paymentDelay*3600*24*1000))>Date.now()) {
                return "warning";
            }

            return "danger";
        };

        $scope.cancel = function(invoice) {
            invoicesService.cancelInvoice($scope, $http, invoice, reload)
        };

        $scope.pay = function(invoice) {
            $http.post("/api/invoices/" + invoice._id.$oid + "/status/paid").success(function(){ reload($scope)})
        };

        $scope.revert = function(invoice) {
            $http.post("/api/invoices/" + invoice._id.$oid + "/status/unpaid").success(function(){ reload($scope)})
        };

        $scope.openAffectationDialog = function(invoice) {
            $scope.affectations = [{
                addButtonVisible: true,
                deleteButtonVisible: true,
                value: invoice.totalHT(),
            }];
            $scope.invoice = invoice;
            $('#affectationModal').modal('show');
        };

        $scope.openReallocationDialog = function(invoice) {
          $http.get("/api/allocations/invoice/" + invoice._id.$oid)
              .success(function(data) {
                  _.map(data, function(item) {
                      item.addButtonVisible = false;
                      item.deleteButtonVisible = true;
                      for (var i = 0; i < $scope.accounts.length; i++) {
                          if ($scope.accounts[i]._id.$oid === item.account._id.$oid) {
                              item.account = $scope.accounts[i];
                              break;
                          }
                      }
                  });
                  $scope.affectations = data;
                  $scope.affectations[$scope.affectations.length -1].addButtonVisible = true;

                  $scope.invoice = invoice;
                  $('#affectationModal').modal('show');
                }
              )
        };

        $scope.affect = function(affectations, invoice) {
            var totalForAffectations = _.reduce(affectations, function(sum, item) { return sum + item.value }, 0);
            if (totalForAffectations > invoice.totalHT()) {
                alert("Petit Chenapan, On ne peut affecter plus que le total d'une facture !");
            } else {
                _.map(affectations, function (item) {
                    delete item.$$hashKey;
                    delete item.addButtonVisible;
                    delete item.deleteButtonVisible;
                    delete item.account.fullname;
                });
                $http.post("/api/invoices/" + invoice._id.$oid + "/affectation", JSON.stringify(affectations))
                    .success(function () {
                        reload($scope);
                        $('#affectationModal').modal('hide');
                    });
            }
        };

        $scope.addAffectationLine = function() {
            $scope.affectations[$scope.affectations.length - 1]['addButtonVisible'] = false;
            $scope.affectations[$scope.affectations.length - 1]['deleteButtonVisible'] = false;
            $scope.affectations.push({
                'addButtonVisible': true,
                'deleteButtonVisible': true
            });
        };

        $scope.deleteAffectationLine = function() {
            $scope.affectations.pop();
            $scope.affectations[$scope.affectations.length - 1]['addButtonVisible'] = true;
            $scope.affectations[$scope.affectations.length - 1]['deleteButtonVisible'] = ($scope.affectations.length>1);
        }
    }])
    .controller('CreateCtrl', function($scope) {
        $scope.shouldUpload = true;

        $scope.tasklines = [{
            invoiceDescription: '',
            invoiceDays: '',
            invoiceDailyRate: '',
            invoiceTaxRate: '20.0',
            addButtonVisible: true,
            deleteButtonVisible: false
        }];

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
        };

        $scope.client = null;

        $scope.taxes = true;
    })
    .controller("HeaderCtrl", function($scope, $location) {

        $scope.isActive = function (viewLocation) {
            return viewLocation === $location.path();
        };
    })
    .factory("InvoicesService", function() {

        return {
            cancelInvoice: function($scope, $http, invoice, callback) {
                $http.post("/api/invoices/" + invoice._id.$oid + "/cancel", "{}").success(function(){ callback($scope)})
            },

            removeAffectation: function($scope, $http, invoice, callback) {
                $http.delete("/api/invoices/" + invoice._id.$oid + "/affectation").success(function(){ callback($scope)})
            }
        };
    });

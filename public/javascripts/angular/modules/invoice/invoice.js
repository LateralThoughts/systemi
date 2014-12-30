angular.module('invoice', ['ui.bootstrap', 'ngResource', 'ngRoute', 'default-values', 'client-select', 'invoice-modal'])
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
            .when('/delayed', {
                controller:'DelayedCtrl',
                templateUrl:'/assets/javascripts/angular/modules/invoice/templates/delayed.html'
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
                        ngModelCtrl.$setViewValue(element[0].type.toLowerCase() == 'radio' ? element[0].value : element[0].checked);
                    });
                });
            }
        };
    }])
    .controller('ListCtrl', ['$scope','$http','$location','$modal','InvoicesService', 'InvoiceModalService', 'default_contractor',function($scope, $http, $location, $modal, invoicesService, invoiceModalService, default_contractor) {

        var selectedInvoiceId = $location.search().invoice;
        if (selectedInvoiceId) {
            invoiceModalService.openInvoiceModal($modal, selectedInvoiceId);
        }

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

            return filteredInvoices.sort(function(a,b) {
                return a.statuses[0].createdAt - b.statuses[0].createdAt;
            });
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
                _.map(data, invoicesService.computeInvoiceTotals);
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
            invoicesService.payInvoice($scope, $http, invoice, reload)
        };

        $scope.revert = function(invoice) {
            $http.post("/api/invoices/" + invoice._id.$oid + "/status/unpaid").success(function(){ reload($scope)})
        };

        $scope.openAffectationDialog = function(invoice) {
            $scope.affectations = [{
                addButtonVisible: true,
                deleteButtonVisible: true,
                value: invoice.totalHT()
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
    .controller('CreateCtrl', ['$scope', '$http', function($scope, $http) {

        $scope.invoice = {
            invoiceNumber: "VTXXX", // TODO retrieve invoice number in database
            paymentDelay: 30,
            withTaxes: true,
            client: null,
            invoice: [
                {
                    taxRate: 20,
                    addButtonVisible: true,
                    deleteButtonVisible: false
                }
            ]
        };

        $scope.addTask = function(){
            $scope.invoice.invoice[$scope.invoice.invoice.length - 1]['addButtonVisible'] = false;
            $scope.invoice.invoice[$scope.invoice.invoice.length - 1]['deleteButtonVisible'] = false;
            $scope.invoice.invoice.push({
                taxRate: 20,
                addButtonVisible: true,
                deleteButtonVisible: true
            });
        };

        $scope.deleteTask = function(){
            $scope.invoice.invoice.pop();
            $scope.invoice.invoice[$scope.invoice.invoice.length - 1]['addButtonVisible'] = true;
            $scope.invoice.invoice[$scope.invoice.invoice.length - 1]['deleteButtonVisible'] = ($scope.invoice.invoice.length>1);
        };

        $scope.submit = function() {

            $scope.invoice.withTaxes = ($scope.invoice.withTaxes !== "false");

            $http.post("/api/invoice", JSON.stringify($scope.invoice))
                .success(function (invoiceId) {
                    window.location.href = "/invoice#/list?invoice=" + invoiceId;
                })
                .error(function () {
                })

        }

    }])
    .controller("DelayedCtrl", ['$scope','$http','InvoicesService',function($scope, $http, invoicesService) {

        var reload = function(scope) {

            function computeTotalHT(invoicesList) {
                return Math.round(_.reduce(invoicesList, function (sum, invoice) {
                    sum += invoice.totalHT();
                    return sum;
                    }, 0) * 100) / 100;
            }

            function computeTotalTTC(invoicesList) {
                return Math.round(_.reduce(invoicesList, function (sum, invoice) {
                        sum += invoice.totalTTC();
                        return sum;
                    }, 0) * 100) / 100;
            }

            $http.get("/api/invoices/delayed").success(function (data) {
                _.map(data, invoicesService.computeInvoiceTotals);

                _.map(data, function(item) {
                    var lastTime = item.statuses[0].createdAt + (item.invoice.paymentDelay*3600*24*1000);
                    var delayInMillis = Date.now() - lastTime;

                   item.delayInDays = Math.floor(delayInMillis/(1000*3600*24));
                });

                scope.invoices = data.sort(function(a, b) {
                    return b.delayInDays - a.delayInDays;
                });

                scope.delayedInvoicesNumber = data.length;
                scope.delayedInvoicesTotalHT = computeTotalHT(data)
                scope.delayedInvoicesTotalTTC = computeTotalTTC(data);

                var thirtyDaysData = data.filter(function(item) {
                   return item.delayInDays > 30;
                });

                scope.delayedThirtyDaysInvoicesNumber = thirtyDaysData.length;
                scope.delayedThirtyDaysInvoicesTotalHT = computeTotalHT(thirtyDaysData);
                scope.delayedThirtyDaysInvoicesTotalTTC = computeTotalTTC(thirtyDaysData);
            });
        };
        reload($scope);

        $scope.openInvoiceModal = function(invoice) {
            $scope.invoicePdfUrl = "/api/invoices/document/" + invoice._id.$oid;
            $('#invoiceModal').modal('show');
        };

        $scope.cancel = function(invoice) {
            invoicesService.cancelInvoice($scope, $http, invoice, reload)
        };

        $scope.pay = function(invoice) {
            invoicesService.payInvoice($scope, $http, invoice, reload)
        };

    }])
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

            payInvoice: function($scope, $http, invoice, callback) {
                $http.post("/api/invoices/" + invoice._id.$oid + "/status/paid").success(function(){ callback($scope)})
            },

            computeInvoiceTotals: function(invoice) {
                invoice.totalHT = function () {
                    var lines = this.invoice.invoice;
                    return _.reduce(lines, function (sum, line) {
                        sum += line.dailyRate * line.days;
                        return sum
                    }, 0);
                };

                invoice.totalTTC = function () {
                    if (this.invoice.withTaxes) {
                        var lines = this.invoice.invoice;
                        return _.reduce(lines, function (sum, line) {
                            sum += Math.round((line.dailyRate * line.days * (1 + line.taxRate / 100)) * 100) / 100;
                            return sum
                        }, 0);
                    } else {
                        return invoice.totalHT();
                    }

                }
            }
        };
    });

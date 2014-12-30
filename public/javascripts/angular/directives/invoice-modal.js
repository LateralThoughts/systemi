angular.module("invoice-modal", [])
    .directive('ltInvoiceModal', ['$modal', 'InvoiceModalService', function ($modal, InvoiceModalService) {
        return {
            template:'<a ng-click="openInvoiceModal(invoiceId)">Afficher la facture (PDF)</a>',
            restrict: 'EA',
            replace: true,
            scope: {
                invoiceId: '@'
            },
            link: function(scope, element, attrs){

                scope.openInvoiceModal = function(invoiceId) {
                    InvoiceModalService.openInvoiceModal($modal, invoiceId);
                };
            }
        }
    }])
    .factory("InvoiceModalService", function() {

        return {
            openInvoiceModal: function($modal, invoiceId) {
                $modal.open({
                    templateUrl: '/assets/javascripts/angular/templates/invoice-modal.html',
                    controller: function ($scope, $modalInstance, invoiceId) {
                        $scope.invoiceId = invoiceId;

                        $scope.dismissInvoiceModal = function () {
                            $modalInstance.dismiss("cancel");
                        }
                    },
                    resolve: {
                        invoiceId: function () {
                            return invoiceId;
                        }
                    }
                });
            }
        };
    });

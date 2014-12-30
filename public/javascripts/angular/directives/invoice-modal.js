var ModalInstanceCtrl = function ($scope, $modalInstance, invoiceId) {
    $scope.invoiceId = invoiceId;

    $scope.dismissInvoiceModal = function() {
        $modalInstance.dismiss("cancel");
    }
};

angular.module("invoice-modal", [])
    .directive('ltInvoiceModal', function ($modal) {
        return {
            template:'<a ng-click="openInvoiceModal(invoiceId)">Afficher la facture (PDF)</a>',
            restrict: 'EA',
            replace: true,
            scope: {
                invoiceId: '@'
            },
            link: function(scope, element, attrs){


                scope.openInvoiceModal = function(invoiceId) {
                    $modal.open({
                        templateUrl: '/assets/javascripts/angular/templates/invoice-modal.html',
                        controller: ModalInstanceCtrl,
                        resolve: {
                            invoiceId: function () {
                                return invoiceId;
                            }
                        }
                    });
                };
            }
        }
    });

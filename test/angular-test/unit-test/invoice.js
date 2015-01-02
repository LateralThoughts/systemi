'use strict';

describe("Fac stemi - invoice", function () {
    beforeEach(module('invoice'));

    var $httpBackend, rootScope, scope, controller, createInvoiceCreateCtrl;

    // Install requests mock hooker
    beforeEach(function ( ) {
        inject(function ($injector) {
            rootScope = $injector.get('$rootScope');
            controller = $injector.get('$controller');
            $httpBackend = $injector.get('$httpBackend');
            scope = rootScope.$new();

            createInvoiceCreateCtrl = function() {
                return controller('CreateCtrl', {
                    $scope: scope
                });
            };
        })
    });

	it('should initialize invoice model in scope', function () {

        $httpBackend.whenGET("/api/invoice/numbers/last").respond({
            prefix: "VT",
            value: 150
        });


        createInvoiceCreateCtrl();
        $httpBackend.flush();

        expect(scope.invoice.invoiceNumber).toBe("VT150");
        expect(scope.invoice.paymentDelay).toBe(30);
        expect(scope.invoice.withTaxes).toBe(true);

        expect(scope.invoice.invoice.length).toBe(1);
        expect('addButtonVisible' in scope.invoice.invoice[0]).toBe(true);
        expect(scope.invoice.invoice[0].addButtonVisible).toEqual(true);
        expect('deleteButtonVisible' in scope.invoice.invoice[0]).toBe(true);
        expect(scope.invoice.invoice[0].deleteButtonVisible).toEqual(false);
        expect(scope.invoice.invoice[0].taxRate).toEqual(20);
	});

});
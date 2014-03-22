'use strict';

describe("Fac stemi - invoice", function () {
    beforeEach(module('fac-stemi'));

    var rootScope, scope, controller;

    // Install requests mock hooker
    beforeEach(function () {
        inject(function ($injector) {
            rootScope = $injector.get('$rootScope');
            controller = $injector.get('$controller');
            scope = rootScope.$new();

            var invoiceCtrl = controller('InvoiceController', {
                $scope: scope
            });
            /*if(!rootScope.$$phase) {
                rootScope.$apply();
            }*/

        })
    });

	it('should initialize one task line in scope', function () {
        expect(scope.tasklines.length).toBe(1);
        expect('invoiceDescription' in scope.tasklines[0]).toBe(true);
        expect('invoiceDays' in scope.tasklines[0]).toBe(true);
        expect('invoiceDailyRate' in scope.tasklines[0]).toBe(true);
        expect('invoiceTaxRate' in scope.tasklines[0]).toBe(true);
        expect('addButtonVisible' in scope.tasklines[0]).toBe(true);
        expect(scope.tasklines[0].addButtonVisible).toEqual(true);
        expect('deleteButtonVisible' in scope.tasklines[0]).toBe(true);
        expect(scope.tasklines[0].deleteButtonVisible).toEqual(false);
        expect(scope.tasklines[0].invoiceTaxRate).toEqual("20.0");
	});

	it('should upload invoice to google drive by default', function () {
        expect(scope.shouldUpload).toBe(true);
    });
});
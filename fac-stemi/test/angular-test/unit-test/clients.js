'use strict';

describe("Fac stemi - Client module", function () {
    beforeEach(module('fac-stemi'));

    var rootScope, scope, controller, $httpBackend;

    // Install requests mock hooker
    beforeEach(function () {
        inject(function ($injector) {
            rootScope = $injector.get('$rootScope');
            controller = $injector.get('$controller');
            $httpBackend = $injector.get('$httpBackend');
            scope = rootScope.$new();

            var invoiceCtrl = controller('ClientController', {
                $scope: scope
            });

        })
    });

    afterEach(function() {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });

	it('should be able to fetch all clients', function () {
	    var client = {
                    _id: { $oid: "532ac39796bab6d8af6c83ce"},
                     name:"vidal",
                     address:"la rue",
                     postalCode:"65432",
                     city:"de ta mère"}
	    var clients = [client];
        $httpBackend.expect('GET', '/api/clients').respond(clients);

        // have to use $apply to trigger the $digest which will
        // take care of the HTTP request
        scope.$apply(function() {
            scope.findAll();
        });

        $httpBackend.flush();

        expect(scope.clients).toBeDefined();
        expect(scope.clients[0].name).toEqual(client.name);
        expect(scope.clients[0].address).toEqual(client.address);
        expect(scope.clients[0].postalCode).toEqual(client.postalCode);
        expect(scope.clients[0].city).toEqual(client.city);
	});

	it('should PUT and update client when id is defined', function () {
        var client = {
                    _id: { $oid: "532ac39796bab6d8af6c83ce"},
                     name:"vidal",
                     address:"la rue",
                     postalCode:"65432",
                     city:"de ta mère"}
        $httpBackend.expect('PUT', '/api/clients/532ac39796bab6d8af6c83ce').respond();

        // have to use $apply to trigger the $digest which will
        // take care of the HTTP request
        scope.$apply(function() {
            scope.handle(client);
        });

        $httpBackend.flush();
    });

	it('should be only POST new client when id is not defined', function () {
        var client = {
                     name:"vidal",
                     address:"la rue",
                     postalCode:"65432",
                     city:"de ta mère"}
        $httpBackend.expect('POST', '/api/clients', JSON.stringify(client)).respond();

        // have to use $apply to trigger the $digest which will
        // take care of the HTTP request
        scope.$apply(function() {
            scope.handle(client);
        });

        $httpBackend.flush();
    });

    it('should handle properly $oid used in mongo db objects when updating client', function () {
        var client = {
                     _id: { $oid: "532ac39796bab6d8af6c83ce"},
                     name:"vidal",
                     address:"la rue",
                     postalCode:"65432",
                     city:"de ta mère"}
        $httpBackend.expect('PUT', '/api/clients/532ac39796bab6d8af6c83ce', JSON.stringify(client)).respond();

        // have to use $apply to trigger the $digest which will
        // take care of the HTTP request
        scope.$apply(function() {
            scope.handle(client);
        });

        $httpBackend.flush();
    });
});
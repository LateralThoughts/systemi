'use strict';

describe("Fac stemi - C.R.A.", function () {
    beforeEach(module('fac-stemi'));

    var rootScope, scope, controller, $httpBackend;

    // Install requests mock hooker
    beforeEach(function () {
        inject(function ($injector) {
            rootScope = $injector.get('$rootScope');
            controller = $injector.get('$controller');
            scope = rootScope.$new();
            $httpBackend = $injector.get('$httpBackend');

            var craCtl = controller('CraController', {
                $scope: scope
            });

        })
    });

    afterEach(function() {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });


	it('should generate all dates between those selected by user', function () {
	    var start = moment("20111031", "YYYYMMDD");
	    var end = moment("20111031", "YYYYMMDD").add('days', 30);

        scope.datesSelected(start, end);

        expect(scope.cra).toBeDefined();
        expect(scope.cra.days).toBeDefined();
        expect(scope.weeks).toBeDefined();

        var middleDay = moment("20111114", "YYYYMMDD");
        var generatedDays = scope.cra.days[14];
        expect(generatedDays.day.isSame(middleDay)).toBe(true);
	});

	it('should push all cra object to backend', function () {
        var start = moment("20111031", "YYYYMMDD");
        var end = moment("20111031", "YYYYMMDD").add('days', 2);

        scope.datesSelected(start, end);

        expect(scope.cra).toBeDefined();
        expect(scope.cra.days).toBeDefined();
        expect(scope.weeks).toBeDefined();

        $httpBackend.expect('POST', '/api/cra').respond({});

        scope.submit();

        $httpBackend.flush();
    });

    it('should compute the cra number of days directly from selected days', function () {
        var start = moment("20111031", "YYYYMMDD");
        var end = moment("20111031", "YYYYMMDD").add('days', 30);

        scope.datesSelected(start, end);

        expect(scope.cra).toBeDefined();
        expect(scope.cra.days).toBeDefined();
        expect(scope.weeks).toBeDefined();

        $httpBackend.expect('POST', '/api/cra').respond({});

        scope.submit();

        $httpBackend.flush();

        expect(scope.cra.numberOfDays).toBe(23);
    });
});
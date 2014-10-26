angular.module('activity', ['ui.bootstrap', 'ngResource', 'ngRoute', 'client-select','default-values', 'day-block'])
    .config(function($routeProvider) {
        $routeProvider
            .when('/list', {
                controller:'ListCtrl',
                templateUrl:'/assets/javascripts/angular/modules/activity/templates/list.html'
            })
            .when('/create', {
                controller:'CreateCtrl',
                templateUrl:'/assets/javascripts/angular/modules/activity/templates/create.html'
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
    .controller('ListCtrl', ['$scope','$http',function($scope, $http) {
        $scope.invoiceRequest = {
            paymentDelay :30
        };
        function reloadActivities() {
            $http.get("/api/activities").success(function (data) {
                $scope.activities = data;
            });
        }

        reloadActivities();
        $scope.deleteActivity = function(id) {
            $http.delete("/api/activity/" + id).success(function(){ reloadActivities()});
        };

        $scope.open = function(activity) {
            $scope.taxes = 'true';
            $scope.activity = activity;
            $scope.invoiceRequest.title = activity.activity.contractor.toUpperCase() + ' - ' + activity.activity.title;
            $scope.invoiceRequest.invoice = [{
                description: 'Prestations de Services Informatiques',
                days: activity.activity.numberOfDays,
                taxRate: 20
            }];
            $('#invoiceGenerationModal').modal('show');
        };

        $scope.createInvoice = function () {
            $scope.invoiceRequest.client = $scope.activity.activity.client;
            $scope.invoiceRequest.withTaxes = $scope.taxes === 'true';
            $http.post("/api/activity/invoice/" + $scope.activity._id.$oid, JSON.stringify($scope.invoiceRequest))
                .success(function () {
                    $('#invoiceGenerationModal').modal('hide');
                    reloadActivities();
                });

        }

        }])
    .controller('CreateCtrl', ['$scope', '$modal', '$log', '$http', 'Client', 'default_contractor',
    function ($scope, $modal, $log, $http, Client, default_contractor) {
        moment.lang("fr");

        $scope.cra = { days: [], contractor: default_contractor};

        $scope.datesSelected = function (start, end, label) {
            $('#reportrange span').html(start.format('D MMMM YYYY') + ' - ' + end.format('D MMMM YYYY'));
            $scope.startDate = start;
            $scope.endDate = end;
            // generate blocks
            var weeks = [];
            var days = [];
            var currentDayOfWeek = 0;
            var currentWeek = [];
            moment().utc().range(start, end).by('days', function (day) {
                if (currentDayOfWeek == 0) {
                    var count = day.isoWeekday();
                    while (--count != 0) {
                        currentWeek.push({});
                    }
                } else if (day.isoWeekday() <= currentDayOfWeek) {
                    // change week
                    weeks.push(currentWeek);
                    currentWeek = [];
                }
                var currentDay = $scope.createDay(day);
                days.push(currentDay);
                currentWeek.push(currentDay);
                currentDayOfWeek = day.isoWeekday();
                if (day.isSame(end)) {
                    weeks.push(currentWeek);
                }
            });
            $scope.weeks = weeks;
            $scope.cra.days = days;
            $scope.$apply();
        }

        $scope.createDay = function (date) {
            return {
                day: date,
                halfUp: (date.isoWeekday() != 6 && date.isoWeekday() != 7),
                halfDown: (date.isoWeekday() != 6 && date.isoWeekday() != 7),
                state: (date.isoWeekday() != 6 && date.isoWeekday() != 7) ? 0 : 3,
                toggleNextState: function () {
                    this.state = (this.state + 1) % 4;
                    var newState = this.stateToHalfDay(this.state);
                    this.halfUp = newState.halfUp;
                    this.halfDown = newState.halfDown;
                },
                stateToHalfDay: function (state) {
                    if (state === 0) {
                        return { halfUp: true, halfDown: true };
                    }
                    if (state === 1) {
                        return { halfUp: true, halfDown: false };
                    }
                    if (state === 2) {
                        return { halfUp: false, halfDown: true };
                    }
                    if (state === 3) {
                        return { halfUp: false, halfDown: false };
                    }
                }
            }
        }

        $scope.submit = function () {
            var accumulator = function (acc, day) {
                return acc + (day.halfUp ? 0.5 : 0) + (day.halfDown ? 0.5 : 0);
            };

            $scope.cra.numberOfDays = _.reduce($scope.cra.days, accumulator, 0);
            $http.post("/api/activity", JSON.stringify($scope.cra))
                .success(function (url) {
                    window.location.href = url
                })
                .error(function () {
                })
        }

        var optionSet1 = {
            startDate: moment().utc().startOf('month'),
            endDate: moment().utc().endOf('month'),
            minDate: moment().utc().subtract('year', 1),
            maxDate: moment().utc().add('year', 1),
            dateLimit: { days: 60 },
            showDropdowns: true,
            showWeekNumbers: true,
            timePicker: false,
            timePickerIncrement: 1,
            timePicker12Hour: true,
            ranges: {
                'Aujourd\'hui': [moment().utc(), moment().utc()],
                'Hier': [moment().utc().subtract('days', 1), moment().utc().subtract('days', 1)],
                '7 Derniers Jours': [moment().utc().subtract('days', 6), moment().utc()],
                '30 Derniers Jours': [moment().utc().subtract('days', 29), moment().utc()],
                'Ce Mois-ci': [moment().utc().startOf('month'), moment().utc().endOf('month')],
                'Le Mois Dernier': [moment().utc().subtract('month', 1).startOf('month'), moment().utc().subtract('month', 1).endOf('month')]
            },
            opens: 'left',
            buttonClasses: ['btn btn-default'],
            applyClass: 'btn-small btn-primary',
            cancelClass: 'btn-small',
            format: 'DD/MM/YYYY',
            separator: ' à ',
            locale: {
                applyLabel: 'Ok',
                cancelLabel: 'Effacer',
                fromLabel: 'Du',
                toLabel: 'Au',
                customRangeLabel: 'Personnalisé',
                daysOfWeek: ['Di', 'Lu', 'Ma', 'Me', 'Je', 'Ve', 'Sa'],
                monthNames: ['Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin', 'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'],
                firstDay: 1
            }
        };

        $('#reportrange').daterangepicker(optionSet1, $scope.datesSelected);

        // workaround to default select current month at page loading
        setTimeout(function () {
            $scope.datesSelected(moment().utc().startOf('month'), moment().utc().endOf('month').startOf('day'), 'This Month');
        }, 0);
    }])
    .controller("HeaderCtrl", ['$scope','$location', function($scope, $location) {

        $scope.isActive = function (viewLocation) {
            return viewLocation === $location.path();
        };
    }]);
/* global angular */
var facstemi = angular.module('fac-stemi', []);

facstemi.controller('BasicInvoiceController', function BasicInvoiceController($scope, $http) {
  
});

facstemi.controller('CraController', function($scope) {
  $scope.datesSelected = function(start, end, label) {
    $('#reportrange span').html(start.format('MMMM D, YYYY') + ' - ' + end.format('MMMM D, YYYY'));
    $scope.startDate = start;
    $scope.endDate = end;
    var days = [];
    // generate blocks 
    moment().range(start, end).by('days', function(day) {
      days.push({'day': day, 'state': true});
    });
    $scope.days = days;
    $scope.$apply();
  }

  $scope.submit = function() {
    console.log($scope.days);
  }

  var optionSet1 = {
    startDate: moment().subtract('days', 29),
    endDate: moment(),
    minDate: '01/01/2012',
    maxDate: '12/31/2014',
    dateLimit: { days: 60 },
    showDropdowns: true,
    showWeekNumbers: true,
    timePicker: false,
    timePickerIncrement: 1,
    timePicker12Hour: true,
    ranges: {
       'Today': [moment(), moment()],
       'Yesterday': [moment().subtract('days', 1), moment().subtract('days', 1)],
       'Last 7 Days': [moment().subtract('days', 6), moment()],
       'Last 30 Days': [moment().subtract('days', 29), moment()],
       'This Month': [moment().startOf('month'), moment().endOf('month')],
       'Last Month': [moment().subtract('month', 1).startOf('month'), moment().subtract('month', 1).endOf('month')]
    },
    opens: 'left',
    buttonClasses: ['btn btn-default'],
    applyClass: 'btn-small btn-primary',
    cancelClass: 'btn-small',
    format: 'MM/DD/YYYY',
    separator: ' to ',
    locale: {
        applyLabel: 'Submit',
        cancelLabel: 'Clear',
        fromLabel: 'From',
        toLabel: 'To',
        customRangeLabel: 'Custom',
        daysOfWeek: ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr','Sa'],
        monthNames: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'],
        firstDay: 1
    }
  };

  var optionSet2 = {
    startDate: moment().subtract('days', 7),
    endDate: moment(),
    opens: 'left',
    ranges: {
       'Today': [moment(), moment()],
       'Yesterday': [moment().subtract('days', 1), moment().subtract('days', 1)],
       'Last 7 Days': [moment().subtract('days', 6), moment()],
       'Last 30 Days': [moment().subtract('days', 29), moment()],
       'This Month': [moment().startOf('month'), moment().endOf('month')],
       'Last Month': [moment().subtract('month', 1).startOf('month'), moment().subtract('month', 1).endOf('month')]
    }
  };

  $('#reportrange span').html(moment().subtract('days', 29).format('MMMM D, YYYY') + ' - ' + moment().format('MMMM D, YYYY'));

  $('#reportrange').daterangepicker(optionSet1, $scope.datesSelected);

  $('#reportrange').on('show.daterangepicker', function() { console.log("show event fired"); });
  $('#reportrange').on('hide.daterangepicker', function() { console.log("hide event fired"); });
  $('#reportrange').on('apply.daterangepicker', function(ev, picker) { 
    console.log("apply event fired, start/end dates are " 
      + picker.startDate.format('MMMM D, YYYY') 
      + " to " 
      + picker.endDate.format('MMMM D, YYYY')
    ); 
  });
  $('#reportrange').on('cancel.daterangepicker', function(ev, picker) { console.log("cancel event fired"); });
});
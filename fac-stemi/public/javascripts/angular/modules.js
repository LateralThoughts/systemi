/* global angular */
var facstemi = angular.module('fac-stemi',[]);

facstemi.controller('BasicInvoiceController', function BasicInvoiceController($scope, $http) {
  $scope.generateInvoice = function() {
    var obj = $scope.obj;
    obj.invoice.days = parseFloat(obj.invoice.days);
    obj.invoice.dailyRate = parseFloat(obj.invoice.dailyRate);
    obj.invoice.taxRate = parseFloat(obj.invoice.taxRate);
    obj.invoice = [obj.invoice];
    $.fileDownload("/api/invoice", {
          httpMethod: "POST",
          data: JSON.stringify(obj),
        }, {
            preparingMessageHtml: "We are preparing your report, please wait...",
            failMessageHtml: "There was a problem generating your report, please try again."
        });
    $http({
      
      headers: { 'Content-Type': "application/json;charset=UTF-8" }
    }).success(function(data, status, headers, config) {
        
    });
  }
});
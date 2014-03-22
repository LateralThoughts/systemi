facstemi.controller('InvoiceController', function ($scope) {
    $scope.shouldUpload = true;

    var initTaskLineData = {
        'invoiceDescription': '',
        'invoiceDays': '',
        'invoiceDailyRate': '',
        'invoiceTaxRate': '20.0',
        'addButtonVisible': true,
        'deleteButtonVisible': false
    };

    $scope.tasklines = [
        initTaskLineData
    ];

    $scope.addTask = function(){
        $scope.tasklines[$scope.tasklines.length - 1]['addButtonVisible'] = false;
        $scope.tasklines[$scope.tasklines.length - 1]['deleteButtonVisible'] = false;
        $scope.tasklines.push({
            'invoiceDescription': '',
            'invoiceDays': '',
            'invoiceDailyRate': '',
            'invoiceTaxRate': '20.0',
            'addButtonVisible': true,
            'deleteButtonVisible': true
        });
    }

    $scope.deleteTask = function(){
        $scope.tasklines.pop();
        $scope.tasklines[$scope.tasklines.length - 1]['addButtonVisible'] = true;
        $scope.tasklines[$scope.tasklines.length - 1]['deleteButtonVisible'] = ($scope.tasklines.length>1);
    }
});
angular.module("client-select", ['customer', 'client-search'])
    .directive('ltClientSelect', function ($modal, $log, Client) {
        return {
            templateUrl: '/assets/javascripts/angular/templates/client-select.html',
            restrict: 'EA',
            replace: true,
            scope: {
                client: '='
            }
        }
    });
            
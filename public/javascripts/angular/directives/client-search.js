
angular.module("client-search", ['customer'])
    .directive('ltClientSearch', function ($modal, $log, Client) {
        return {
            templateUrl:'/assets/javascripts/angular/templates/client-search.html',
            restrict: 'EA',
            replace: true,
            scope: {
                client : '='
            },
            link: function(scope, element, attrs){
                // Any function returning a promise object can be used to load values asynchronously
                scope.search = function(val) {
                    if (val.length >= 3) {
                        return Client.query({ q: val }).$promise.then(function(res) {
                            var clients = [];
                            angular.forEach(res, function(item){
                                clients.push(item);
                            });
                            return clients;
                        });
                    }
                    return [];
                };
            }
        }
    });

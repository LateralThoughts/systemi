
facstemi.factory('Client', ['$resource', function($resource){
    var Client = $resource('/api/clients/:clientId', {clientId : '@id'}, {
        query : { method : 'GET', isArray: true },
        create : { method : 'POST' },
        save : { method : 'PUT' }
    });

    return Client
  }]
);

facstemi.controller('ClientController', function($scope, Client) {
    $scope.create = function(client) {
        Client.create(client);
    }

    $scope.save = function(client) {
        Client.save(client);
    }
});


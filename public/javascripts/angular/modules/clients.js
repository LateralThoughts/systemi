
facstemi.controller('ClientController', function($scope, Client) {

    $scope.findAll = function() {
        $scope.clients = Client.query();
    }

    $scope.handle = function(client) {
        if (client._id) {
            Client.save(client)
        } else {
            Client.create(client)
        }
    }
});


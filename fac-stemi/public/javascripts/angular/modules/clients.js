
facstemi.factory('Client', ['$resource', function($resource){
    var Client = $resource('/api/clients/:clientId', {clientId : '@id'}, {
        query : { method : 'GET', isArray: true}
    });

    Client.getAll = function(params, successCb, errorCb) {
        return Client.query(params, successCb, errorCb);
    };

    return Client
  }]
);

facstemi.controller('ClientController', function(Client) {

 // Any function returning a promise object can be used to load values asynchronously
  /*$scope.getLocation = function(val) {
    return $http.get('/api/clients', {
      params: {
        q: val
      }
    }).then(function(res){
      var addresses = [];
      angular.forEach(res.data, function(item){
        addresses.push(item.name);
      });
      return addresses;
    });
  };*/


});


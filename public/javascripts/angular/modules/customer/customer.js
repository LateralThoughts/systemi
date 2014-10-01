
angular.module('customer', []).factory('Client', ['$resource', function($resource){
        var Client = $resource('/api/clients/:clientId', {clientId : '@_id.$oid'}, {
            query : { method : 'GET', isArray: true },
            create : { method : 'POST' },
            save : { method : 'PUT', transformRequest: function(data, headersGetter) {return JSON.stringify(data) } }
        });

        return Client;
    }]
);

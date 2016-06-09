(function() {
    'use strict';
    angular
        .module('eveSmartTraderApp')
        .factory('SellableInvType', SellableInvType);

    SellableInvType.$inject = ['$resource'];

    function SellableInvType ($resource) {
        var resourceUrl =  'api/sellable-inv-types/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();

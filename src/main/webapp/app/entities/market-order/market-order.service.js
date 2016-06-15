(function() {
    'use strict';
    angular
        .module('eveSmartTraderApp')
        .factory('MarketOrder', MarketOrder);

    MarketOrder.$inject = ['$resource', 'DateUtils'];

    function MarketOrder ($resource, DateUtils) {
        var resourceUrl =  'api/market-orders/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    data.issued = DateUtils.convertDateTimeFromServer(data.issued);
                    return data;
                }
            },
            'update': { method:'PUT' },
            'reload': { method: 'GET', url: 'api/market-orders/reload' }
        });
    }
})();

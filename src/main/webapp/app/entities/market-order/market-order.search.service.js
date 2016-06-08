(function() {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .factory('MarketOrderSearch', MarketOrderSearch);

    MarketOrderSearch.$inject = ['$resource'];

    function MarketOrderSearch($resource) {
        var resourceUrl =  'api/_search/market-orders/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true}
        });
    }
})();

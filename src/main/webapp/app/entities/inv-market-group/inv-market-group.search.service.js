(function() {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .factory('InvMarketGroupSearch', InvMarketGroupSearch);

    InvMarketGroupSearch.$inject = ['$resource'];

    function InvMarketGroupSearch($resource) {
        var resourceUrl =  'api/_search/inv-market-groups/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true}
        });
    }
})();

(function() {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .factory('SellableInvTypeSearch', SellableInvTypeSearch);

    SellableInvTypeSearch.$inject = ['$resource'];

    function SellableInvTypeSearch($resource) {
        var resourceUrl =  'api/_search/sellable-inv-types/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true}
        });
    }
})();

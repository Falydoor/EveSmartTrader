(function() {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .factory('InvTypeSearch', InvTypeSearch);

    InvTypeSearch.$inject = ['$resource'];

    function InvTypeSearch($resource) {
        var resourceUrl =  'api/_search/inv-types/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true}
        });
    }
})();

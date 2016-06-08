(function() {
    'use strict';
    angular
        .module('eveSmartTraderApp')
        .factory('InvMarketGroup', InvMarketGroup);

    InvMarketGroup.$inject = ['$resource'];

    function InvMarketGroup ($resource) {
        var resourceUrl =  'api/inv-market-groups/:id';

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

(function() {
    'use strict';
    angular
        .module('eveSmartTraderApp')
        .factory('InvType', InvType);

    InvType.$inject = ['$resource'];

    function InvType ($resource) {
        var resourceUrl =  'api/inv-types/:id';

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

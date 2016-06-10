(function () {
    'use strict';
    angular
        .module('eveSmartTraderApp')
        .factory('Trade', Trade);

    Trade.$inject = ['$resource'];

    function Trade($resource) {
        var resourceUrl = 'api/trades/:id';

        return $resource(resourceUrl, {}, {
            'query': {method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    return data;
                }
            }
        });
    }
})();

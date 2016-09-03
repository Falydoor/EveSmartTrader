(function () {
    'use strict';
    angular
        .module('eveSmartTraderApp')
        .factory('Trade', Trade);

    Trade.$inject = ['$resource'];

    function Trade($resource) {
        var resourceUrl = 'api/trades';

        return $resource(resourceUrl, {}, {
            'changeStation': {method: 'GET', url: 'api/changeStation'},
            'hubTrades': {method: 'GET', isArray: true, url: 'api/hubTrades'},
            'penuryTrades': {method: 'GET', isArray: true, url: 'api/penuryTrades'},
            'stationTrades': {method: 'GET', isArray: true, url: 'api/stationTrades'}
        });
    }
})();

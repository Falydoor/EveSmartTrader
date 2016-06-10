(function() {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider.state('trade', {
            parent: 'app',
            url: '/trade',
            data: {
                authorities: ['ROLE_USER']
            },
            views: {
                'content@': {
                    templateUrl: 'app/home/trade/trades.html',
                    controller: 'TradeController',
                    controllerAs: 'vm'
                }
            }
        });
    }
})();

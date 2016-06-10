(function () {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .controller('TradeController', TradeController);

    TradeController.$inject = ['$scope', '$state', 'Trade', 'clipboard'];

    function TradeController($scope, $state, Trade, clipboard) {
        var vm = this;
        vm.trades = Trade.query();
        vm.copy = function (name) {
            clipboard.copyText(name);
        };
    }
})();

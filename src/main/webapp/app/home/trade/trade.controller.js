(function () {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .controller('TradeController', TradeController);

    TradeController.$inject = ['$scope', '$state', 'Trade', 'clipboard'];

    function TradeController($scope, $state, Trade, clipboard) {
        var vm = this;
        vm.showHubTrades = true;
        Trade.hubTrades({}, function (trades) {
            vm.hubTrades = trades;
            vm.hubTradesSize = trades.length;
        });
        vm.showPenuryTrades = false;
        Trade.penuryTrades({}, function (trades) {
            vm.penuryTrades = trades;
            vm.penuryTradesSize = trades.length;
        });
        vm.showStationTrades = false;
        Trade.stationTrades({}, function (trades) {
            vm.stationTrades = trades;
            vm.stationTradesSize = trades.length;
        });
        vm.copy = function (name) {
            clipboard.copyText(name);
        };
        vm.refresh = function () {
            $state.reload();
        };
    }
})();

(function () {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .controller('TradeController', TradeController);

    TradeController.$inject = ['$scope', '$state', 'Trade', 'clipboard', 'identity'];

    function TradeController($scope, $state, Trade, clipboard, identity) {
        var vm = this;
        vm.stations = identity.stations;
        vm.station = vm.stations[3];
        vm.sellerStation = vm.stations[0];
        vm.showHubTrades = true;
        vm.showPenuryTrades = false;
        vm.showStationTrades = false;
        vm.loadTrades = loadTrades;
        vm.loadTrades();
        vm.copy = copy;

        function copy(trade) {
            trade.copied = true;
            clipboard.copyText(trade.name);
        }

        function loadTrades() {
            vm.hubTrades = undefined;
            vm.penuryTrades = undefined;
            vm.stationTrades = undefined;
            Trade.hubTrades({station: vm.station}, function (trades) {
                vm.hubTrades = trades;
            });
            Trade.penuryTrades({station: vm.station}, function (trades) {
                vm.penuryTrades = trades;
                vm.showPenuryTrades = vm.penuryTrades.length.penuryTradesSize > 0;
            });
            Trade.stationTrades({station: vm.station}, function (trades) {
                vm.stationTrades = trades;
            });
        }
    }
})();

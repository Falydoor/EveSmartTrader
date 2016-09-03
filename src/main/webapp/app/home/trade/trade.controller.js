(function () {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .controller('TradeController', TradeController);

    TradeController.$inject = ['$scope', '$state', 'Trade', 'clipboard', 'identity'];

    function TradeController($scope, $state, Trade, clipboard, identity) {
        var vm = this;
        vm.stations = identity.stations;
        vm.station = identity.station;
        vm.sellerStation = vm.stations[0];
        vm.showHubTrades = true;
        vm.showPenuryTrades = false;
        vm.showStationTrades = false;
        vm.changeStation = changeStation;
        vm.loadTrades = loadTrades;
        vm.copy = copy;

        vm.loadTrades();

        function copy(trade) {
            trade.copied = true;
            clipboard.copyText(trade.name);
        }

        function loadTrades() {
            vm.hubTrades = undefined;
            vm.penuryTrades = undefined;
            vm.stationTrades = undefined;
            Trade.hubTrades(function (trades) {
                vm.hubTrades = trades;
            });
            Trade.penuryTrades(function (trades) {
                vm.penuryTrades = trades;
                vm.showPenuryTrades = vm.penuryTrades.length.penuryTradesSize > 0;
            });
            Trade.stationTrades(function (trades) {
                vm.stationTrades = trades;
            });
        }

        function changeStation() {
            Trade.changeStation({station: vm.station}, function () {
                vm.loadTrades();
            });
        }
    }
})();

(function () {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .controller('TradeController', TradeController);

    TradeController.$inject = ['$scope', '$state', 'Trade', 'clipboard', 'Principal'];

    function TradeController($scope, $state, Trade, clipboard, Principal) {
        var vm = this;
        Principal.identity().then(function (account) {
            vm.stations = account.stations;
            vm.station = 'JitaHUB';
            vm.sellerStation = 'AmarrHUB';
            vm.loadTrades();
        });
        vm.showHubTrades = true;
        vm.showPenuryTrades = false;
        vm.showStationTrades = false;

        vm.copy = function (name) {
            clipboard.copyText(name);
        };
        vm.loadTrades = function () {
            vm.hubTradesSize = undefined;
            vm.penuryTradesSize = undefined;
            vm.stationTradesSize = undefined;
            vm.hubTrades = [];
            vm.penuryTrades = [];
            vm.stationTrades = [];
            Trade.hubTrades({station: vm.station}, function (trades) {
                vm.hubTrades = trades;
                vm.hubTradesSize = trades.length;
            });
            Trade.penuryTrades({station: vm.station}, function (trades) {
                vm.penuryTrades = trades;
                vm.penuryTradesSize = trades.length;
            });
            Trade.stationTrades({station: vm.station}, function (trades) {
                vm.stationTrades = trades;
                vm.stationTradesSize = trades.length;
            });
        };
    }
})();

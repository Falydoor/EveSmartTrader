(function() {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .controller('MarketOrderDetailController', MarketOrderDetailController);

    MarketOrderDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'entity', 'MarketOrder', 'InvType', 'SellableInvType'];

    function MarketOrderDetailController($scope, $rootScope, $stateParams, entity, MarketOrder, InvType, SellableInvType) {
        var vm = this;
        vm.marketOrder = entity;
        vm.load = function (id) {
            MarketOrder.get({id: id}, function(result) {
                vm.marketOrder = result;
            });
        };
        var unsubscribe = $rootScope.$on('eveSmartTraderApp:marketOrderUpdate', function(event, result) {
            vm.marketOrder = result;
        });
        $scope.$on('$destroy', unsubscribe);

    }
})();

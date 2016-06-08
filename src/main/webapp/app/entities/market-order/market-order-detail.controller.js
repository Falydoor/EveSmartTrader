(function() {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .controller('MarketOrderDetailController', MarketOrderDetailController);

    MarketOrderDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'entity', 'MarketOrder', 'InvType'];

    function MarketOrderDetailController($scope, $rootScope, $stateParams, entity, MarketOrder, InvType) {
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

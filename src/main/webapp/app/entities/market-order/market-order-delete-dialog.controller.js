(function() {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .controller('MarketOrderDeleteController',MarketOrderDeleteController);

    MarketOrderDeleteController.$inject = ['$uibModalInstance', 'entity', 'MarketOrder'];

    function MarketOrderDeleteController($uibModalInstance, entity, MarketOrder) {
        var vm = this;
        vm.marketOrder = entity;
        vm.clear = function() {
            $uibModalInstance.dismiss('cancel');
        };
        vm.confirmDelete = function (id) {
            MarketOrder.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        };
    }
})();

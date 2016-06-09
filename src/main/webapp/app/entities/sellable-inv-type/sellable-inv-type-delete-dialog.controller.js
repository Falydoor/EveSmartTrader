(function() {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .controller('SellableInvTypeDeleteController',SellableInvTypeDeleteController);

    SellableInvTypeDeleteController.$inject = ['$uibModalInstance', 'entity', 'SellableInvType'];

    function SellableInvTypeDeleteController($uibModalInstance, entity, SellableInvType) {
        var vm = this;
        vm.sellableInvType = entity;
        vm.clear = function() {
            $uibModalInstance.dismiss('cancel');
        };
        vm.confirmDelete = function (id) {
            SellableInvType.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        };
    }
})();

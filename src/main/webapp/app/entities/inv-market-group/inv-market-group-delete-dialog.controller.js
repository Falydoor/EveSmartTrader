(function() {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .controller('InvMarketGroupDeleteController',InvMarketGroupDeleteController);

    InvMarketGroupDeleteController.$inject = ['$uibModalInstance', 'entity', 'InvMarketGroup'];

    function InvMarketGroupDeleteController($uibModalInstance, entity, InvMarketGroup) {
        var vm = this;
        vm.invMarketGroup = entity;
        vm.clear = function() {
            $uibModalInstance.dismiss('cancel');
        };
        vm.confirmDelete = function (id) {
            InvMarketGroup.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        };
    }
})();

(function() {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .controller('InvTypeDeleteController',InvTypeDeleteController);

    InvTypeDeleteController.$inject = ['$uibModalInstance', 'entity', 'InvType'];

    function InvTypeDeleteController($uibModalInstance, entity, InvType) {
        var vm = this;
        vm.invType = entity;
        vm.clear = function() {
            $uibModalInstance.dismiss('cancel');
        };
        vm.confirmDelete = function (id) {
            InvType.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        };
    }
})();

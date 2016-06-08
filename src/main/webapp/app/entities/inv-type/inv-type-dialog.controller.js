(function() {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .controller('InvTypeDialogController', InvTypeDialogController);

    InvTypeDialogController.$inject = ['$scope', '$stateParams', '$uibModalInstance', 'entity', 'InvType', 'InvMarketGroup'];

    function InvTypeDialogController ($scope, $stateParams, $uibModalInstance, entity, InvType, InvMarketGroup) {
        var vm = this;
        vm.invType = entity;
        vm.invmarketgroups = InvMarketGroup.query();
        vm.load = function(id) {
            InvType.get({id : id}, function(result) {
                vm.invType = result;
            });
        };

        var onSaveSuccess = function (result) {
            $scope.$emit('eveSmartTraderApp:invTypeUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        };

        var onSaveError = function () {
            vm.isSaving = false;
        };

        vm.save = function () {
            vm.isSaving = true;
            if (vm.invType.id !== null) {
                InvType.update(vm.invType, onSaveSuccess, onSaveError);
            } else {
                InvType.save(vm.invType, onSaveSuccess, onSaveError);
            }
        };

        vm.clear = function() {
            $uibModalInstance.dismiss('cancel');
        };
    }
})();

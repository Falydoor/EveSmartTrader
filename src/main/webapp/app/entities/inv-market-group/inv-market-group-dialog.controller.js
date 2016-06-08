(function() {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .controller('InvMarketGroupDialogController', InvMarketGroupDialogController);

    InvMarketGroupDialogController.$inject = ['$scope', '$stateParams', '$uibModalInstance', 'entity', 'InvMarketGroup'];

    function InvMarketGroupDialogController ($scope, $stateParams, $uibModalInstance, entity, InvMarketGroup) {
        var vm = this;
        vm.invMarketGroup = entity;
        vm.load = function(id) {
            InvMarketGroup.get({id : id}, function(result) {
                vm.invMarketGroup = result;
            });
        };

        var onSaveSuccess = function (result) {
            $scope.$emit('eveSmartTraderApp:invMarketGroupUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        };

        var onSaveError = function () {
            vm.isSaving = false;
        };

        vm.save = function () {
            vm.isSaving = true;
            if (vm.invMarketGroup.id !== null) {
                InvMarketGroup.update(vm.invMarketGroup, onSaveSuccess, onSaveError);
            } else {
                InvMarketGroup.save(vm.invMarketGroup, onSaveSuccess, onSaveError);
            }
        };

        vm.clear = function() {
            $uibModalInstance.dismiss('cancel');
        };
    }
})();

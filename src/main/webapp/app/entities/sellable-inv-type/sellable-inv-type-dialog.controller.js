(function() {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .controller('SellableInvTypeDialogController', SellableInvTypeDialogController);

    SellableInvTypeDialogController.$inject = ['$scope', '$stateParams', '$uibModalInstance', 'entity', 'SellableInvType', 'MarketOrder', 'InvType'];

    function SellableInvTypeDialogController ($scope, $stateParams, $uibModalInstance, entity, SellableInvType, MarketOrder, InvType) {
        var vm = this;
        vm.sellableInvType = entity;
        vm.marketorders = MarketOrder.query();
        vm.invtypes = InvType.query();
        vm.load = function(id) {
            SellableInvType.get({id : id}, function(result) {
                vm.sellableInvType = result;
            });
        };

        var onSaveSuccess = function (result) {
            $scope.$emit('eveSmartTraderApp:sellableInvTypeUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        };

        var onSaveError = function () {
            vm.isSaving = false;
        };

        vm.save = function () {
            vm.isSaving = true;
            if (vm.sellableInvType.id !== null) {
                SellableInvType.update(vm.sellableInvType, onSaveSuccess, onSaveError);
            } else {
                SellableInvType.save(vm.sellableInvType, onSaveSuccess, onSaveError);
            }
        };

        vm.clear = function() {
            $uibModalInstance.dismiss('cancel');
        };
    }
})();

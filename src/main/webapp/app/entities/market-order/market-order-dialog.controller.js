(function() {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .controller('MarketOrderDialogController', MarketOrderDialogController);

    MarketOrderDialogController.$inject = ['$scope', '$stateParams', '$uibModalInstance', 'entity', 'MarketOrder', 'InvType'];

    function MarketOrderDialogController ($scope, $stateParams, $uibModalInstance, entity, MarketOrder, InvType) {
        var vm = this;
        vm.marketOrder = entity;
        vm.invtypes = InvType.query();
        vm.load = function(id) {
            MarketOrder.get({id : id}, function(result) {
                vm.marketOrder = result;
            });
        };

        var onSaveSuccess = function (result) {
            $scope.$emit('eveSmartTraderApp:marketOrderUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        };

        var onSaveError = function () {
            vm.isSaving = false;
        };

        vm.save = function () {
            vm.isSaving = true;
            if (vm.marketOrder.id !== null) {
                MarketOrder.update(vm.marketOrder, onSaveSuccess, onSaveError);
            } else {
                MarketOrder.save(vm.marketOrder, onSaveSuccess, onSaveError);
            }
        };

        vm.clear = function() {
            $uibModalInstance.dismiss('cancel');
        };

        vm.datePickerOpenStatus = {};
        vm.datePickerOpenStatus.issued = false;

        vm.openCalendar = function(date) {
            vm.datePickerOpenStatus[date] = true;
        };
    }
})();

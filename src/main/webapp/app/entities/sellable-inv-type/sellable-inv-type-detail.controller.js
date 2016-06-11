(function() {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .controller('SellableInvTypeDetailController', SellableInvTypeDetailController);

    SellableInvTypeDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'entity', 'SellableInvType', 'MarketOrder', 'InvType'];

    function SellableInvTypeDetailController($scope, $rootScope, $stateParams, entity, SellableInvType, MarketOrder, InvType) {
        var vm = this;
        vm.sellableInvType = entity;
        vm.load = function (id) {
            SellableInvType.get({id: id}, function(result) {
                vm.sellableInvType = result;
            });
        };
        var unsubscribe = $rootScope.$on('eveSmartTraderApp:sellableInvTypeUpdate', function(event, result) {
            vm.sellableInvType = result;
        });
        $scope.$on('$destroy', unsubscribe);

    }
})();

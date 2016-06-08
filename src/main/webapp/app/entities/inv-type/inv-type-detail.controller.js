(function() {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .controller('InvTypeDetailController', InvTypeDetailController);

    InvTypeDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'entity', 'InvType', 'InvMarketGroup'];

    function InvTypeDetailController($scope, $rootScope, $stateParams, entity, InvType, InvMarketGroup) {
        var vm = this;
        vm.invType = entity;
        vm.load = function (id) {
            InvType.get({id: id}, function(result) {
                vm.invType = result;
            });
        };
        var unsubscribe = $rootScope.$on('eveSmartTraderApp:invTypeUpdate', function(event, result) {
            vm.invType = result;
        });
        $scope.$on('$destroy', unsubscribe);

    }
})();

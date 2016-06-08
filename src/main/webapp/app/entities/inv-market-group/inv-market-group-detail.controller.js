(function() {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .controller('InvMarketGroupDetailController', InvMarketGroupDetailController);

    InvMarketGroupDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'entity', 'InvMarketGroup'];

    function InvMarketGroupDetailController($scope, $rootScope, $stateParams, entity, InvMarketGroup) {
        var vm = this;
        vm.invMarketGroup = entity;
        vm.load = function (id) {
            InvMarketGroup.get({id: id}, function(result) {
                vm.invMarketGroup = result;
            });
        };
        var unsubscribe = $rootScope.$on('eveSmartTraderApp:invMarketGroupUpdate', function(event, result) {
            vm.invMarketGroup = result;
        });
        $scope.$on('$destroy', unsubscribe);

    }
})();

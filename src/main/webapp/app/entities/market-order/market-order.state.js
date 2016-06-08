(function() {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('market-order', {
            parent: 'entity',
            url: '/market-order?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'MarketOrders'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/market-order/market-orders.html',
                    controller: 'MarketOrderController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,asc',
                    squash: true
                },
                search: null
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
            }
        })
        .state('market-order-detail', {
            parent: 'entity',
            url: '/market-order/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'MarketOrder'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/market-order/market-order-detail.html',
                    controller: 'MarketOrderDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'MarketOrder', function($stateParams, MarketOrder) {
                    return MarketOrder.get({id : $stateParams.id});
                }]
            }
        })
        .state('market-order.new', {
            parent: 'market-order',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/market-order/market-order-dialog.html',
                    controller: 'MarketOrderDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                buy: false,
                                issued: null,
                                price: null,
                                volumeEntered: null,
                                stationID: null,
                                volume: null,
                                range: null,
                                minVolume: null,
                                duration: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('market-order', null, { reload: true });
                }, function() {
                    $state.go('market-order');
                });
            }]
        })
        .state('market-order.edit', {
            parent: 'market-order',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/market-order/market-order-dialog.html',
                    controller: 'MarketOrderDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['MarketOrder', function(MarketOrder) {
                            return MarketOrder.get({id : $stateParams.id});
                        }]
                    }
                }).result.then(function() {
                    $state.go('market-order', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('market-order.delete', {
            parent: 'market-order',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/market-order/market-order-delete-dialog.html',
                    controller: 'MarketOrderDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['MarketOrder', function(MarketOrder) {
                            return MarketOrder.get({id : $stateParams.id});
                        }]
                    }
                }).result.then(function() {
                    $state.go('market-order', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();

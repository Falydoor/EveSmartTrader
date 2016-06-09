(function() {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('sellable-inv-type', {
            parent: 'entity',
            url: '/sellable-inv-type?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'SellableInvTypes'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/sellable-inv-type/sellable-inv-types.html',
                    controller: 'SellableInvTypeController',
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
        .state('sellable-inv-type-detail', {
            parent: 'entity',
            url: '/sellable-inv-type/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'SellableInvType'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/sellable-inv-type/sellable-inv-type-detail.html',
                    controller: 'SellableInvTypeDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'SellableInvType', function($stateParams, SellableInvType) {
                    return SellableInvType.get({id : $stateParams.id});
                }]
            }
        })
        .state('sellable-inv-type.new', {
            parent: 'sellable-inv-type',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/sellable-inv-type/sellable-inv-type-dialog.html',
                    controller: 'SellableInvTypeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('sellable-inv-type', null, { reload: true });
                }, function() {
                    $state.go('sellable-inv-type');
                });
            }]
        })
        .state('sellable-inv-type.edit', {
            parent: 'sellable-inv-type',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/sellable-inv-type/sellable-inv-type-dialog.html',
                    controller: 'SellableInvTypeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['SellableInvType', function(SellableInvType) {
                            return SellableInvType.get({id : $stateParams.id});
                        }]
                    }
                }).result.then(function() {
                    $state.go('sellable-inv-type', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('sellable-inv-type.delete', {
            parent: 'sellable-inv-type',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/sellable-inv-type/sellable-inv-type-delete-dialog.html',
                    controller: 'SellableInvTypeDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['SellableInvType', function(SellableInvType) {
                            return SellableInvType.get({id : $stateParams.id});
                        }]
                    }
                }).result.then(function() {
                    $state.go('sellable-inv-type', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();

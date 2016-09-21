(function() {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('inv-market-group', {
            parent: 'entity',
            url: '/inv-market-group?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'InvMarketGroups'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/inv-market-group/inv-market-groups.html',
                    controller: 'InvMarketGroupController',
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
                }]
            }
        })
        .state('inv-market-group-detail', {
            parent: 'entity',
            url: '/inv-market-group/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'InvMarketGroup'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/inv-market-group/inv-market-group-detail.html',
                    controller: 'InvMarketGroupDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'InvMarketGroup', function($stateParams, InvMarketGroup) {
                    return InvMarketGroup.get({id : $stateParams.id});
                }]
            }
        })
        .state('inv-market-group.new', {
            parent: 'inv-market-group',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/inv-market-group/inv-market-group-dialog.html',
                    controller: 'InvMarketGroupDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                parentGroupID: null,
                                marketGroupName: null,
                                description: null,
                                iconID: null,
                                hasTypes: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('inv-market-group', null, { reload: true });
                }, function() {
                    $state.go('inv-market-group');
                });
            }]
        })
        .state('inv-market-group.edit', {
            parent: 'inv-market-group',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/inv-market-group/inv-market-group-dialog.html',
                    controller: 'InvMarketGroupDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['InvMarketGroup', function(InvMarketGroup) {
                            return InvMarketGroup.get({id : $stateParams.id});
                        }]
                    }
                }).result.then(function() {
                    $state.go('inv-market-group', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('inv-market-group.delete', {
            parent: 'inv-market-group',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/inv-market-group/inv-market-group-delete-dialog.html',
                    controller: 'InvMarketGroupDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['InvMarketGroup', function(InvMarketGroup) {
                            return InvMarketGroup.get({id : $stateParams.id});
                        }]
                    }
                }).result.then(function() {
                    $state.go('inv-market-group', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();

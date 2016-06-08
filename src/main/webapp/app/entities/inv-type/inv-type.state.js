(function() {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('inv-type', {
            parent: 'entity',
            url: '/inv-type?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'InvTypes'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/inv-type/inv-types.html',
                    controller: 'InvTypeController',
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
        .state('inv-type-detail', {
            parent: 'entity',
            url: '/inv-type/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'InvType'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/inv-type/inv-type-detail.html',
                    controller: 'InvTypeDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'InvType', function($stateParams, InvType) {
                    return InvType.get({id : $stateParams.id});
                }]
            }
        })
        .state('inv-type.new', {
            parent: 'inv-type',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/inv-type/inv-type-dialog.html',
                    controller: 'InvTypeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                groupID: null,
                                typeName: null,
                                description: null,
                                mass: null,
                                volume: null,
                                capacity: null,
                                portionSize: null,
                                raceID: null,
                                basePrice: null,
                                published: null,
                                marketGroupID: null,
                                iconID: null,
                                soundID: null,
                                graphicID: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('inv-type', null, { reload: true });
                }, function() {
                    $state.go('inv-type');
                });
            }]
        })
        .state('inv-type.edit', {
            parent: 'inv-type',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/inv-type/inv-type-dialog.html',
                    controller: 'InvTypeDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['InvType', function(InvType) {
                            return InvType.get({id : $stateParams.id});
                        }]
                    }
                }).result.then(function() {
                    $state.go('inv-type', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('inv-type.delete', {
            parent: 'inv-type',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/inv-type/inv-type-delete-dialog.html',
                    controller: 'InvTypeDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['InvType', function(InvType) {
                            return InvType.get({id : $stateParams.id});
                        }]
                    }
                }).result.then(function() {
                    $state.go('inv-type', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();

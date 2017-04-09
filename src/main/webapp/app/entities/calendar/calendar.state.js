(function() {
    'use strict';

    angular
        .module('calendarApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('calendar', {
            parent: 'entity',
            url: '/calendar',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Calendars'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/calendar/calendars.html',
                    controller: 'CalendarController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
            }
        })
        .state('calendar-detail', {
            parent: 'calendar',
            url: '/calendar/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Calendar'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/calendar/calendar-detail.html',
                    controller: 'CalendarDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'Calendar', function($stateParams, Calendar) {
                    return Calendar.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'calendar',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('calendar-detail.edit', {
            parent: 'calendar-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/calendar/calendar-dialog.html',
                    controller: 'CalendarDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Calendar', function(Calendar) {
                            return Calendar.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('calendar.new', {
            parent: 'calendar',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/calendar/calendar-dialog.html',
                    controller: 'CalendarDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                name: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('calendar', null, { reload: 'calendar' });
                }, function() {
                    $state.go('calendar');
                });
            }]
        })
        .state('calendar.edit', {
            parent: 'calendar',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/calendar/calendar-dialog.html',
                    controller: 'CalendarDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Calendar', function(Calendar) {
                            return Calendar.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('calendar', null, { reload: 'calendar' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('calendar.delete', {
            parent: 'calendar',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/calendar/calendar-delete-dialog.html',
                    controller: 'CalendarDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Calendar', function(Calendar) {
                            return Calendar.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('calendar', null, { reload: 'calendar' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();

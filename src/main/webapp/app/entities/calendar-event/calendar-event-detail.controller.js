(function() {
    'use strict';

    angular
        .module('calendarApp')
        .controller('CalendarEventDetailController', CalendarEventDetailController);

    CalendarEventDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'CalendarEvent', 'Calendar'];

    function CalendarEventDetailController($scope, $rootScope, $stateParams, previousState, entity, CalendarEvent, Calendar) {
        var vm = this;

        vm.calendarEvent = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('calendarApp:calendarEventUpdate', function(event, result) {
            vm.calendarEvent = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();

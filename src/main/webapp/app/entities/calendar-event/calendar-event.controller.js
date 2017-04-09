(function() {
    'use strict';

    angular
        .module('calendarApp')
        .controller('CalendarEventController', CalendarEventController);

    CalendarEventController.$inject = ['CalendarEvent'];

    function CalendarEventController(CalendarEvent) {

        var vm = this;

        vm.calendarEvents = [];

        loadAll();

        function loadAll() {
            CalendarEvent.query(function(result) {
                vm.calendarEvents = result;
                vm.searchQuery = null;
            });
        }
    }
})();

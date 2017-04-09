(function() {
    'use strict';

    angular
        .module('calendarApp')
        .controller('CalendarEventDeleteController',CalendarEventDeleteController);

    CalendarEventDeleteController.$inject = ['$uibModalInstance', 'entity', 'CalendarEvent'];

    function CalendarEventDeleteController($uibModalInstance, entity, CalendarEvent) {
        var vm = this;

        vm.calendarEvent = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            CalendarEvent.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();

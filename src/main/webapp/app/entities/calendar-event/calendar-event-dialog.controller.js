(function() {
    'use strict';

    angular
        .module('calendarApp')
        .controller('CalendarEventDialogController', CalendarEventDialogController);

    CalendarEventDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'CalendarEvent', 'Calendar'];

    function CalendarEventDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, CalendarEvent, Calendar) {
        var vm = this;

        vm.calendarEvent = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;
        vm.calendars = Calendar.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.calendarEvent.id !== null) {
                CalendarEvent.update(vm.calendarEvent, onSaveSuccess, onSaveError);
            } else {
                CalendarEvent.save(vm.calendarEvent, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('calendarApp:calendarEventUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.time = false;
        vm.datePickerOpenStatus.reminderTime = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();

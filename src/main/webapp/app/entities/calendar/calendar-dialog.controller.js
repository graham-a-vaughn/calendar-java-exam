(function() {
    'use strict';

    angular
        .module('calendarApp')
        .controller('CalendarDialogController', CalendarDialogController);

    CalendarDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Calendar'];

    function CalendarDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Calendar) {
        var vm = this;

        vm.calendar = entity;
        vm.clear = clear;
        vm.save = save;

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.calendar.id !== null) {
                Calendar.update(vm.calendar, onSaveSuccess, onSaveError);
            } else {
                Calendar.save(vm.calendar, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('calendarApp:calendarUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();

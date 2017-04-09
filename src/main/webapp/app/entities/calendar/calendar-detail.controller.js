(function() {
    'use strict';

    angular
        .module('calendarApp')
        .controller('CalendarDetailController', CalendarDetailController);

    CalendarDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Calendar'];

    function CalendarDetailController($scope, $rootScope, $stateParams, previousState, entity, Calendar) {
        var vm = this;

        vm.calendar = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('calendarApp:calendarUpdate', function(event, result) {
            vm.calendar = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();

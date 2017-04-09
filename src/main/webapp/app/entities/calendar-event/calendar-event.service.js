(function() {
    'use strict';
    angular
        .module('calendarApp')
        .factory('CalendarEvent', CalendarEvent);

    CalendarEvent.$inject = ['$resource', 'DateUtils'];

    function CalendarEvent ($resource, DateUtils) {
        var resourceUrl =  'api/calendar-events/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.time = DateUtils.convertDateTimeFromServer(data.time);
                        data.reminderTime = DateUtils.convertDateTimeFromServer(data.reminderTime);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();

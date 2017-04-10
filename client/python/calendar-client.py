import requests
import datetime as dt
import json
import random
import types


clientUrl = 'http://localhost:8080/api'
authPath = clientUrl + '/authenticate'
eventsPath = clientUrl + '/calendar-events'

adminUser = {'username': 'admin', 'password': 'admin'}
defaultUser = {'username': 'user', 'password': 'user'}

defaultHeaders = {}
jsonContentHeader = {'Content-Type': 'application/json'}
defaultHeaders.update(jsonContentHeader)
credentials = adminUser
authToken = 'unauthorized'

def populate_participant_pool(count):
    results = []
    for i in range(0, count, 1):
        email = 'participant.' + str(i) + '.jones@example.com'
        results.append(email)
    return results

def authenticate(user):
    auth_response = requests.post(authPath, headers=defaultHeaders, json=user)
    verify_response_or_exit(auth_response, 'Authentication Failed for user: ' + user['username'])

    auth_token = auth_response.json()['id_token']
    print 'JWT Token Retrieved ...'
    print auth_token
    return auth_token


def check_response(resp, success_code=(200,299)):
    if resp.status_code < success_code[0] or resp.status_code > success_code[1]:
        return False
    return True


def verify_response_or_exit(resp, failure_message='This experiment has failed, goodbye ...'):
    if not check_response(resp):
        print failure_message
        print 'Failure Response: '
        print resp.status_code
        print resp.url
        print resp.text
        exit(1)


def print_events(event_list):
    if len(event_list) == 0:
        print 'No events to list'
    else:
        for item in event_list:
            print(item)

def create_event(title, minutes_from_now=20, reminder_window=10, location='1-800-meetnow', attendees=[]):
    time_now = dt.datetime.now()
    event_time = time_now + dt.timedelta(minutes=minutes_from_now)
    reminder_time = event_time - dt.timedelta(minutes=reminder_window)
    event_attendees = []
    event_attendees.extend(attendees)
    if len(event_attendees) == 0:
        event_attendees = [credentials['username']]

    event = {'title': title, 'time': get_time_string(event_time),
             'reminderTime': get_time_string(reminder_time), 'location': location, 'attendees': event_attendees}
    print 'POSTING new event to ' + eventsPath
    print(event)
    event_response = requests.post(eventsPath, headers=authenticationHeaders, json=event)
    verify_response_or_exit(event_response)
    event_response_object = event_response.json()
    print 'Event created, response from server: '
    print(event_response_object)

def get_time_string(dtime):
    fmt = '%Y-%m-%dT%H:%M:%S.000Z'
    return dtime.strftime(fmt)


def initialize_user_authentication(user):
    auth_token = authenticate(user)
    auth_header = {'Authorization': 'Bearer ' + auth_token}
    request_headers = {}
    request_headers.update(defaultHeaders)
    request_headers.update(auth_header)
    return request_headers


def get_user_events(authentication_headers):
    event_list_response = requests.get(eventsPath, headers=authentication_headers)
    verify_response_or_exit(event_list_response)
    event_list_content = event_list_response.content
    isu = isinstance(event_list_content, types.UnicodeType)
    loaded = json.loads(event_list_content)
    event_list = event_list_response.json()
    return event_list


currentUser = adminUser
print 'Authenticating User: '
print(currentUser)
authenticationHeaders = initialize_user_authentication(currentUser)
admin_events = get_user_events(authenticationHeaders)
print 'Pre-existing event count for user: ' + currentUser['username'] + '  ... ' + str(len(admin_events))
print 'Pre-existing lists for user: '
print currentUser
print_events(admin_events)

newAdminEventCount = 4
meetingParticipantPool = []
meetingParticipantPool.extend(populate_participant_pool(1000))

newAdminEvents = []
minutesFromNow = 15
random.seed(dt.datetime.now().microsecond)
for i in range(0, newAdminEventCount, 1):
    attendee_count = random.randrange(2, 12, 1)
    attendees = []
    for j in range(0, attendee_count, 1):
        index = random.randrange(0, 1000, 1)
        attendees.append(meetingParticipantPool[index])
    title = 'Admin: Python Party #' + str(i)
    create_event(title, minutes_from_now=minutesFromNow, attendees=attendees)

admin_events = get_user_events(authenticationHeaders)
print 'All events for user: '
print currentUser
print_events(admin_events)


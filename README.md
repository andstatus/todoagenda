# [Todo Agenda](https://github.com/andstatus/todoagenda#readme) - Calendar and Task widgets for Android

[![Build Status](https://travis-ci.com/andstatus/todoagenda.svg?branch=master)](https://travis-ci.com/github/andstatus/todoagenda)

Todo Agenda is home screen widgets for your Android device. 
Each widget has its own settings and displays configured list of calendar events and tasks
so that you can easily have a glimpse at your due, current and upcoming appointments.

[<img src="doc/images/get-it-on-rustore.png" alt="Get ToDo Agenda on RuStore" height="60" />](https://apps.rustore.ru/app/org.andstatus.todoagenda)
[<img src="doc/images/5x5.png" width="10">](#)
[<img src="doc/images/get-it-on-google-play.png" alt="Get ToDo Agenda on Google Play" height="60" />](https://play.google.com/store/apps/details?id=org.andstatus.todoagenda)
[<img src="doc/images/5x5.png" width="10">](#)
[<img src="doc/images/get-it-on-fdroid.png" alt="Get ToDo Agenda on F-Droid" height="60">](https://f-droid.org/packages/org.andstatus.todoagenda)

![Calendar Widget Screenshots](app/src/main/play/listings/en-US/graphics/large-tablet-screenshots/widget-collage.png)

## Features

* No advertising. Free and Open Source.
* Displays events from your calendars and task lists for the selected periods in the past and in the future.
* Automatically updates when you add/delete/modify an event. Or you may update the list instantly.
* Select only the calendars and task lists that you want to see in the widget.
* Create several widgets, if you need. Each widget has its own settings, including layouts, colors, filters, 
selected calendars and task lists.
* Customize colors and transparency of different widget parts, of their texts and backgrounds.
* Scroll through the list of events. Use "Go to Today" button to return to today instantly.
* Customize the text size of the widget.
* Fully resizable widget with alternative layouts.
* Indicators for alerts and recurring events.
* Lock time zone when travelling to different time zones.
* Change start hour of a day if you stay up late or wake up early.
* Turn off Widget header, Day headers, event icons, Days from today, etc. and see only what you need.
* Hide duplicated events.
* Backup and restore settings, cloning widgets on the same or different devices.
* Android 7+ supported. Supports Android tablets.

Note on Tasks support: As there is no unified way to access tasks created in different applications, 
each application needs its own implementation. Currently supported:
* [Tasks: Astrid To-Do List Clone v.10+](https://github.com/tasks/tasks#readme), Google Tasks can be used via it.
* [OpenTasks (by dmfs GmbH)](https://github.com/dmfs/opentasks#readme).
* Partially supported: Tasks of Samsung Calendar ([looks like a deprecating feature...](
  https://eu.community.samsung.com/t5/Galaxy-S9-S9/New-Update-Calendar-Issues/td-p/940866)). 

<a id="changelog"/>

See the ToDo Agenda [Changelog](CHANGELOG.md).

<a id="troubleshooting"/>

## Troubleshooting after installation or an update

Due to Android design, widgets may misbehave or even may not work at all  
 after installation or an update. In this case, try the below:

1. Restart your device.

If the problem is still present:

1. Uninstall old version of the "ToDo Agenda" app.
2. Restart your device.
3. Install the new app version.
4. Recreate your widgets.

In a case the widget doesn't work properly even after this, please
 [search, read and follow up on similar issues here](https://github.com/andstatus/todoagenda/issues?q=is%3Aissue+sort%3Aupdated-desc).
In particular, see these solutions:
* [Buttons don't work](https://github.com/andstatus/todoagenda/issues/45).
* [Refresh doesn't help showing new events](https://github.com/andstatus/todoagenda/issues/17).
* [New recurring calendar events not showing](https://github.com/andstatus/todoagenda/issues/55).

<a id="collaborate"/>

## Collaborate

Want to contribute to the project? Start by translating the widget to another language or update existing translation
at [Translation project at Crowdin](http://crowdin.net/project/todoagenda)
and be a part of the next release. :)

We are developing this application in public to bring you a tool that _you_ want to use. Please feel free to open
[issues](https://github.com/andstatus/todoagenda/issues?q=is%3Aissue+sort%3Aupdated-desc) and provide
[pull requests](https://github.com/andstatus/todoagenda/pulls).
To get timely feedback we are also providing Beta versions,
see [Beta testing of ToDo Agenda](https://github.com/andstatus/todoagenda/issues/3).

<a id="development"/>

## Development

App development is fun. And it's even more fun, when you have automated testing
set up to show you how your code performs. ToDo Agenda has such automated tests, allowing not only
to replay event timelines, shared by users and thus figure out problems and understand wishes.
We can even see all the replayed timelines on an Android device (or an emulator),
[as this tests execution recording shows](https://youtu.be/oiJkzx86rFg).
Create a widget with a name ending with "Test replay" and Tests will start showing in this widget.
Source code of these tests is in the repository also.

### Automatic Android tests
* For full widget tests please create a widget specifically for testing.
  This widget must have a Widget name ending with <code>Test replay</code>.
  Android doesn't allow to create a widget programmatically,
  so you need to create the widget manually on a target device (or a device emulator).

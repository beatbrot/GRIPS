# G.R.I.P.S API implementation
The goal of the project is to create a concise implementation of the [G.R.I.P.S](https://elearning.uni-regensburg.de)-API.

## Getting Started
Usage is simple. After importing the library, the Client can be created by calling `GripsClient(loginData: LoginData)` and then invoking `login()`

```
var loginData = LoginData(Realm.HS, "login", "password")
var client = GripsClient(loginData)
client.login() 
```

Afterwards, the client-object can be used until the session is expired.

## Usage

[uml]: http://www.plantuml.com/plantuml/uml/VP1D2i8m48NtEKMM_b0F44IgNOZWJYyGqg4EJISbCnL4lBknBRLkt6M-lFVc9JcBZT8rNZblcNKHkiZmL6kDfT4dadGYsu3H5ub8LHhT3LmDwVOGWWTBwgL6ypMqwFvwIs0NiHKCDAFM2TvH4DZeCx9iznFOVTD7-FXTu97gAIffXmVeLPODTOm-feLSU0IIJGNIquNhPKlLJn_r56zcTLGELARFUGC0 "UML Diagram"

The Data-Layer follows an hierarchical approach. The Courses can be requested by ``GripsClient.getCourses()``. Courses consist of 0 to n topic. For performance/bandwith reasons, the topics are not downloaded automatically. To get all topics of a course, ``GripsClient.getTopics(course: Course)`` can be invoked.
A topic can have 0 to n activities. Activities are for now just files.

Example: To get a list of all activities available to an account, we need to ``flatMap()`` twice. First, for flatmapping courses to topics. Seconds, to get all activities for each topic. This would, in the end, look like this:
```
    client.getCourses().stream()
            .flatMap { client.getTopics(it).stream() }
            .flatMap { it.activities.stream() }
            .collect(Collectors.toList())
```

## Disclaimer
This project is in no way affiliated with [OTH Regensburg](https://oth-regensburg.de) and/or  [Universität Regensburg](http://www.uni-regensburg.de/). The code is only accessing information, that is already publicly available at [G.R.I.P.S](https://elearning.uni-regensburg.de) 
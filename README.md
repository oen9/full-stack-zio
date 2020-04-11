# full-stack-zio

full-stack-zio

[![Build Status](https://travis-ci.org/oen9/full-stack-zio.svg?branch=master)](https://travis-ci.org/oen9/full-stack-zio)
[![CircleCI](https://circleci.com/gh/oen9/full-stack-zio.svg?style=svg)](https://circleci.com/gh/oen9/full-stack-zio)

![alt text](https://raw.githubusercontent.com/oen9/full-stack-zio/master/img/web.png "web")

## Waiting for scalajs-1.0.0 support

```sbt
"io.scalaland" %%% "chimney" % "0.5.0",
```

## Libs

### backend

1. scala
1. ZIO
1. cats-core
1. http4s
1. pureconfig
1. circe
1. swagger
1. reactivemongo
1. doobie
1. flyway

### frontend

1. scalajs
1. slinky (react)
1. diode
1. bootstrap
1. circe

## in progress

Something with doobie(PostgreSQL)

## soon

1. doobie
1. auth example
1. more tests
1. and more

## Production

### docker

1. `sbt stage`
1. `docker-compose up -d web`
1. open `http://localhost:8080` in browser

### standalone

1. `sbt stage`
1. set `MONGO_URL_FULL_STACK_ZIO` env variable\
example: `MONGO_URL_FULL_STACK_ZIO=mongodb://test:test@localhost:27017/test`
1. set `DATABASE_URL_FULL_STACK_ZIO` env variable\
example: `DATABASE_URL_FULL_STACK_ZIO="jdbc:postgresql://localhost:5432/fullstackzio?user=test&password=test"`
1. run `./target/universal/stage/bin/app`
1. open `http://localhost:8080` in browser

## DEV

### required services

- docker\
run `docker-compose up -d mongo postgres`

- other\
set `MONGO_URL_FULL_STACK_ZIO` env variable\
example: `MONGO_URL_FULL_STACK_ZIO=mongodb://test:test@localhost:27017/test`
- set `DATABASE_URL_FULL_STACK_ZIO` env variable\
example: `DATABASE_URL_FULL_STACK_ZIO="jdbc:postgresql://localhost:5432/fullstackzio?user=test&password=test"`

### js

`fastOptJS::webpack`\
`~fastOptJS`\
open `js/src/main/resources/index-dev.html` in browser

### server

`reStart`\
http://localhost:8080/

### js + server (dev conf)

Run server normally `reStart`.\
Connect your js api to http://localhost:8080
(e.g.`js/src/main/scala/example/services/AjaxClient.scala`).\
Run js: `fastOptJS::webpack` and `fastOptJS`.\
Open `js/src/main/resources/index-dev.html` in browser.\
When server changed run `reStart`.\
When js changed run `fastOptJS`.

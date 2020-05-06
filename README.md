# full-stack-zio

full-stack-zio

[![Build Status](https://travis-ci.org/oen9/full-stack-zio.svg?branch=master)](https://travis-ci.org/oen9/full-stack-zio)
[![CircleCI](https://circleci.com/gh/oen9/full-stack-zio.svg?style=svg)](https://circleci.com/gh/oen9/full-stack-zio)

![alt text](https://raw.githubusercontent.com/oen9/full-stack-zio/master/img/web.png "web")

## Features

1. TODO list - MongoDB
1. Small Flappy Bird game with scoreboard - Postgres\
The game is badly optimised because of not working directly with canvas.
It is just my demo focusing on react with scalajs.
1. Auth example (register, singin, signout, secured endpoint) - Postgres
1. API documentation - Swagger

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

1. small chat with websockets

## soon

1. websockets
1. more?

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
example: `MONGO_URL_FULL_STACK_ZIO=mongodb://test:test@localhost:27017/test`\
set `DATABASE_URL_FULL_STACK_ZIO` env variable\
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
Run js: `fastOptJS::webpack` and `fastOptJS`.\
Open `js/src/main/resources/index-dev.html` in browser.\
When server changed run `reStart`.\
When js changed run `fastOptJS`.

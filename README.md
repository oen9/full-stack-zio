# full-stack-zio

full-stack-zio

[![Build Status](https://travis-ci.org/oen9/full-stack-zio.svg?branch=master)](https://travis-ci.org/oen9/full-stack-zio)
[![CircleCI](https://circleci.com/gh/oen9/full-stack-zio.svg?style=svg)](https://circleci.com/gh/oen9/full-stack-zio)

![alt text](https://raw.githubusercontent.com/oen9/full-stack-zio/master/img/web.png "web")

## Waiting for scalajs-1.0.0 support

```sbt
"io.circe" %%% "circe-generic-extras" % "0.13.0"
"io.circe" %%% "circe-generic" % "0.13.0"
"io.circe" %%% "circe-literal" % "0.13.0"
"io.scalaland" %%% "chimney" % "0.4.1",
"com.softwaremill.quicklens" %%% "quicklens" % "1.4.12"
```

## Libs

### backend

1. scala
1. ZIO
1. cats-core
1. http4s
1. pureconfig
1. circe

### frontend

1. scalajs
1. slinky (react)
1. diode
1. bootstrap
1. circe

## soon

1. mongo
1. doobie
1. swagger
1. docker
1. auth example
1. more tests
1. and more

## Production

1. `sbt stage`
1. run `./target/universal/stage/bin/app`
1. open `http://localhost:8080` in browser

## DEV

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

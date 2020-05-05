![Java CI](https://github.com/JKatzwinkel/tla-web/workflows/build/badge.svg)
![LINE](https://img.shields.io/badge/line--coverage-62%25-yellow.svg)
![METHOD](https://img.shields.io/badge/method--coverage-48%25-orange.svg)

TLA web frontend.

Depends on:

- Spring Boot
- [Thymeleaf Layout Dialect](https://ultraq.github.io/thymeleaf-layout-dialect/)
- [Lombok](https://projectlombok.org/)
- [JSesh](https://github.com/rosmord/jsesh)
- [tla-common](https://github.com/JKatzwinkel/tla-common)
- [tla-es](https://github.com/JKatzwinkel/tla-es)


## Usage

Install, populate, and run the [backend](https://github.com/JKatzwinkel/tla-es) first.


Run the frontend application using the `bootRun` task from the spring boot gradle plugin
(on windows, you will probably need to use the native wrapper `./gradlew.bat` instead):

    gradle bootrun

On its first run, this will download and install [Bootstrap](https://getbootstrap.com/), [Font Awesome](https://fontawesome.com/),
[JQuery](https://jquery.com/), and [Headroom.js](https://wicky.nillia.ms/headroom.js/).
In order to override the respective default bundle versions of some of these, you can use the environment variables
shown in [`.env.template`](.env.template), e.g. by defining them in a `.env` file:

    BOOTSTRAP_VERSION=4.4.1
    FONTAWESOME_VERSION=5.12.1
    JQUERY_VERSION=3.5.0

However, this is of course optional.


## Misc

You can check for the newest version of package dependencies by running:

    gradle dependencyUpdates


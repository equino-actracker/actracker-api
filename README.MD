# Running locally
By default it's not required to have any infrastructure to run application locally - local run is configured to use in-memory infrastructure (database etc).
It can be changed in main module configuration.
If locally setup infrastructure is required to run the application, the gradle task `setupInfra` should be run prior to application startup to ensure, that all the required infrastructure has been set up locally (docker and docker-compose needs to be installed):
`./gradlew setupInfra`
`./gradlew tearDownInfra`

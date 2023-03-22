# Purpose of actracker-api
This application has been created with learning the following topics in mind:
- DDD methodology
- Relational databases
- Messaging (event publishing)
- Spring Boot (using as little framework features as possible, to provide ability to quickly switch to other framework)
- Kubernetes
- Gradle
- Observability and debugging (the application is intentionally not bullet-prove,
performance tests for sure will point a need for metrics, debugging, tweaking)

# Role of actracker-api in Actracker project
The role of this application is exposing the API for Actracker entities CRUD operations.
This is the monolith backend application for Actracker.

# Architecture
The application runs as a Spring Boot container.
The entities are saved in the backend database.
After the entity changed, the notification event is published to messaging system.

# Running locally

## Setting up infrastructure
By default it's not required to have any infrastructure to run application locally -
local run is configured to use in-memory infrastructure (database etc).
It can be changed in main module configuration.
If locally setup infrastructure is required to run the application, the gradle task `setupInfra` should be run prior 
to application startup to ensure that all the required infrastructure components have been set up locally 
(docker and docker-compose needs to be installed):

`./gradlew setupInfra`

`./gradlew tearDownInfra`

## Running application
To run the application locally `ActrackerApi` must be run. It's a class starting the Spring Boot container.
Alternatively, Gradle Spring Boot plugin can be used:

`./gradlew bootRun`

# Build pipelines
There are build pipelines defined in jenkins_files directory.

After the pull request is created, the [PR pipeline](jenkins_files/Jenkinsfile_pr) is started in Equino Jenkins,
which tests the PR and deploys the application to development environment.

When the build passes, the change may be deployed and merged by adding the 'ready for release' comment to the pull request.
The comment triggers the [Release pipeline](jenkins_files/Jenkinsfile_release).
It deploys the application to production environment, merges the PR and deletes branch.

Merge of the PR triggers the [Master pipeline](jenkins_files/Jenkinsfile_master).
The pipeline updates version of the application using the [equino-version](https://github.com/marcinciapa/equino-gradle-plugins/blob/master/equino-version/README.md)
Gradle plugin and deploys the application to integration environment.

# Versioning
The application version is maintained automatically with the [equino-version](https://github.com/marcinciapa/equino-gradle-plugins/blob/master/equino-version/README.md)
Gradle plugin.

# Deployment
The application is deployed to [Equino Kubernetes cluster](https://github.com/marcinciapa/equino-kubernetes) from Jenkins pipelines.
The deployment is handled by Gradle [jkube](https://www.eclipse.org/jkube/docs/kubernetes-gradle-plugin/) plugin.

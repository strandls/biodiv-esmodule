# Building

- This file is for reference purpose only that gives you basic idea on how CI system is going to build this or how to setup dev environment locally
- Dummy Credintials replace it with actual one

> **DO NOT COMMIT CREDINTIALS**

```sh
# Will be set globally on CD
export ARTIFACTORY_USERNAME=admin
export ARTIFACTORY_PASSWORD=password
export ARTIFACTORY_URL=http://venus.strandls.com/artifactory
```

### Build commands

```sh
./@ci/maven-toolbox configure-m2
mvn clean install
./@ci/maven-toolbox configure-sdk
```

### Maven Toolbox Documentation

- [maven-toolbox](https://github.com/harshzalavadiya/maven-toolbox/blob/master/README.md)

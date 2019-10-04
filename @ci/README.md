# Building

- This file is for reference purpose only that gives you basic idea on how CI system is going to build this or how to setup dev environment locally
- Dummy Credintials replace it with actual one

> **DO NOT COMMIT CREDINTIALS**

```sh
# Will be set globally on CD
export ARTIFACTORY_USERNAME=admin
export ARTIFACTORY_PASSWORD=password
export ARTIFACTORY_URL=http://venus.strandls.com/artifactory

# Naksha Specific
export MTPROP_GEOSERVER.URL="http://localhost:8080/geoserver/"
export MTPROP_GEOSERVER.WEB.USERNAME="admin"
export MTPROP_GEOSERVER.WEB.PASSWORD="geoserver"
export MTPROP_GEOSERVER.DBUSER="postgres"
export MTPROP_DB.USERNAME="postgres"
export MTPROP_DB.PASSWORD="postgres123"
export MTPROP_TMPDIR.PATH="/app/data/geoserver/tmp/"
export MTPROP_TMPDIRGEOSERVERPATH="/app/data/geoserver/data"
export MTPROP_NAMESPACE.ID="NamespaceInfoImpl--21489445:78545345680:-7ffe"
export MTPROP_DATASTORE.ID="DataStoreInfoImpl--21483454:136743037af:-7ffg"
```

### Build commands

```sh
sh ./@ci/install-maven-toolbox.sh
sh ./@ci/build-and-deploy.sh
```

### Maven Toolbox Documentation

- [maven-toolbox](https://github.com/harshzalavadiya/maven-toolbox/blob/master/README.md)

# Technical Notes Application


## Init Steps:

- to create a docker image of an application: 
```bash
sbt docker
```
- to scale up the containers and start the application:
```bash
docker-compose up -d 
```

or

```bash
sbt dockerComposeUp
```


to stop containers:
```bash
sbt dockerComposeStop
```


# Alternatively

#### Start postgres instance:

```bash 
docker run --rm --name pg-docker -e POSTGRES_DB=technical_notes -e POSTGRES_PASSWORD=docker -d -p 5432:5432 -v $HOME/.docker/volumes/postgres/var/lib/postgresql/data  postgres
```

#### Run migrations 
(inside project root)
- sbt flywayMigrate
- sbt flywayClean

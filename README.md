# Technical Notes Application

## Init Steps:

#### Start postgres instance:

```bash 
docker run --rm --name pg-docker -e POSTGRES_DB=technical_notes -e POSTGRES_PASSWORD=docker -d -p 5432:5432 -v $HOME/.docker/volumes/postgres/var/lib/postgresql/data  postgres
```

#### Run migrations 
(inside project root)
- sbt flywayMigrate
- sbt flywayClean (plugin broken)

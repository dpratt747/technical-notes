package io.github.dpratt747.technical_notes.domain.adt.configuration

import io.github.dpratt747.technical_notes.domain.adt.values._


final case class Properties(databaseName: DatabaseName, user: UserName, password: Password)

final case class PostgresConf(hostName: HostName, port: Port, properties: Properties)

final case class Conf(postgres: PostgresConf)


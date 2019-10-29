package io.github.dpratt747.technical_notes.domain.adt.configuration

final case class DatabaseName(value: String) extends AnyVal

final case class UserName(value: String) extends AnyVal

final case class HostName(value: String) extends AnyVal

final case class Password(value: String) extends AnyVal

final case class Port(value: Int) extends AnyVal

final case class Properties(databaseName: DatabaseName, user: UserName, password: Password)

final case class PostgresConf(hostName: HostName, port: Port, properties: Properties)

final case class Conf(postgres: PostgresConf)


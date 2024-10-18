package org.vitrivr.engine.database.pgvector

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.SQLException
import java.util.*

/***
 * Singleton provides a connection pool for the PostgreSQL database.
 * @param url URL of the PostgreSQL database in the form `"jdbc:postgresql://${host}:${port}/${database}"`.
 * @param properties `java.util.Properties` for the connection. Options are `user`, `password`.

 */
class PoolingConn(url: String, properties: Properties) {

    private val config: HikariConfig = HikariConfig()
    private  var datasource: HikariDataSource

    init {
        this.config.jdbcUrl = url
        this.config.username = properties["user"]!!.toString()
        this.config.password = properties["password"]!!.toString()
        this.config.addDataSourceProperty("pool", "true")
        this.config.addDataSourceProperty("prepStmtCacheSize", "250")
        this.config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        this.datasource = HikariDataSource(config)
    }

    /***
     * Returns a connection for the connection pool.
     * @return Connection
     * @throws SQLException If the connection pool has not been initialized.
     */
    @get:Throws(SQLException::class)
    val connection: Connection
        get() {
            return datasource.connection
        }
}

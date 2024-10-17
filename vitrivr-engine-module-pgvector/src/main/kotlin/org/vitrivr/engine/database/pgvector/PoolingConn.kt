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
class PoolingConn(url:String, properties: Properties) {

    private val config: HikariConfig = HikariConfig()
    private lateinit var datasource: HikariDataSource

    init {
        if (instance == null) {
            this.config.jdbcUrl = url
            this.config.username = properties["user"]!!.toString()
            this.config.password = properties["password"]!!.toString()
            this.config.addDataSourceProperty("pool", "true")
            this.config.addDataSourceProperty("prepStmtCacheSize", "250")
            this.config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
            this.datasource = HikariDataSource(config)
            instance = this
        }else{
            throw SQLException("Connection pool has already been initialized!")
        }
    }

    /***
     * Returns a connection for the connection pool.
     * @return Connection
     */
    val connection: Connection
        get() = datasource.connection

    companion object {
        @JvmStatic
        private var instance: PoolingConn? = null

        /***
         * Returns a connection for the connection pool.
         * @return Connection
         * @throws SQLException If the connection pool has not been initialized.
         */
        @get:Throws(SQLException::class)
        val connection: Connection
            get() {
                if (instance == null) {
                    throw SQLException("Connection pool has not been initialized!")
                }
                return instance!!.datasource.connection
            }
    }
}
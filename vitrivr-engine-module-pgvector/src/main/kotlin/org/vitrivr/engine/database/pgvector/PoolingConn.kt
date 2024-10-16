package org.vitrivr.engine.database.pgvector

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.SQLException
import java.util.*


class PoolingConn(properties: Properties) {

    private val config: HikariConfig = HikariConfig()
    private lateinit var datasource: HikariDataSource

    init {
        if (instance == null) {
            this.config.jdbcUrl = "jdbc:postgresql://10.34.64.130/postgres"
            this.config.username = "postgres"
            this.config.password = "admin"
            this.config.addDataSourceProperty("cachePrepStmts", "true")
            this.config.addDataSourceProperty("prepStmtCacheSize", "250")
            this.config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
            this.datasource = HikariDataSource(config)
            instance = this
        }
    }

    val connection: Connection
        get() = datasource.connection

    companion object {
        @JvmStatic
        private var instance: PoolingConn? = null

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
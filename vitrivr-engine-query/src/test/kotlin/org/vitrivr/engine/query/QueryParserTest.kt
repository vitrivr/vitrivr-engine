package org.vitrivr.engine.query

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.vitrivr.engine.query.model.api.InformationNeedDescription
import org.vitrivr.engine.query.model.api.result.QueryResult
import org.vitrivr.engine.query.parsing.QueryParser

class QueryParserTest {

    @BeforeEach
    fun setUp() {
        val informationNeed =

        /* Obtain query parser. */
        val operator = QueryParser(schema).parse(informationNeed)
        val results = executor.query(operator)
    }


    @Test

    @AfterEach
    fun tearDown() {
    }
}
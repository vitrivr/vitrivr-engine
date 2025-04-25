import io.javalin.json.JsonMapper
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.vitrivr.engine.core.config.pipeline.execution.ExecutionStatus
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.query.model.api.result.QueryResult
import org.vitrivr.engine.server.api.rest.KotlinxJsonMapper
import org.vitrivr.engine.server.api.rest.handlers.SchemaList
import org.vitrivr.engine.server.api.rest.model.ErrorStatus
import org.vitrivr.engine.server.api.rest.model.IngestStatus
import java.lang.reflect.Type
import java.util.*

/**
 * A collection of tests to make sure that serialization works properly-
 *
 * @author Fynn Faber
 * @version 1.1.0
 */
class KotlinxJsonMapperTest {

    private val jsonMapper: JsonMapper = KotlinxJsonMapper
    private val schemaList = SchemaList(listOf("schema1", "schema2"))
    private val queryResult : QueryResult
    private val ingestStatus = IngestStatus(UUID.randomUUID().toString(), ExecutionStatus.RUNNING, System.currentTimeMillis())
    private val errorStatus = ErrorStatus("Error")

    private val schemaListStr = """{"schemas":["schema1","schema2"]}"""
    private val ingestStatusStr = """{"jobId":"${ingestStatus.jobId}","executionStatus":"RUNNING","timestamp":${ingestStatus.timestamp}}"""
    private val errorStatusStr = """{"message":"Error"}"""

    init {
        val r1 = Retrieved(UUID.randomUUID(), "SOURCE:IMAGE", emptyList(), emptySet(), emptySet(), emptySet(), false)
        val r2 = Retrieved(UUID.randomUUID(), "SOURCE:IMAGE", emptyList(), emptySet(), emptySet(), emptySet(), false)
        val rel = Relationship.ByRef(r1, "partOf", r2, false)

        queryResult = QueryResult(listOf(r1.copy(relationships = setOf(rel)), r2))

    }

    @Test
    fun testFromJsonString_validSchemaList() {
        val result: SchemaList = jsonMapper.fromJsonString(schemaListStr, SchemaList::class.java)
        assertNotNull(result)
        assertEquals(2, result.schemas.size)
        assertTrue(result.schemas.contains("schema1"))
        assertTrue(result.schemas.contains("schema2"))
    }

    @Test
    fun testToJsonString_validSchemaList() {
        val jsonString = jsonMapper.toJsonString(schemaList, SchemaList::class.java)
        assert(jsonString == schemaListStr)
    }

    @Test
    fun testFromJsonString_validIngestStatus() {
        val result: IngestStatus = jsonMapper.fromJsonString(ingestStatusStr, IngestStatus::class.java)
        assertNotNull(result)
        assertEquals(ingestStatus.jobId, result.jobId)
        assertEquals(ingestStatus.executionStatus, result.executionStatus)
        assertEquals(ingestStatus.timestamp, result.timestamp)
    }

    @Test
    fun testToJsonString_validIngestStatus() {
        val jsonString = jsonMapper.toJsonString(ingestStatus, IngestStatus::class.java)
        assert(jsonString == ingestStatusStr)
    }


    @Test
    fun testFromJsonString_validErrorStatus() {
        val result: ErrorStatus = jsonMapper.fromJsonString(errorStatusStr, ErrorStatus::class.java)
        assertNotNull(result)
        assertEquals(errorStatus.message, result.message)
    }

    @Test
    fun testToJsonString_validErrorStatus() {
        val jsonString = jsonMapper.toJsonString(errorStatus, ErrorStatus::class.java)
        assert(jsonString == errorStatusStr)
    }

    @Serializable
    data class TestObject(val name: String, val age: Int)

    @Test
    fun testFromJsonString_validJson() {
        val jsonString = """{"name":"John Doe","age":30}"""
        val type: Type = TestObject::class.java
        val result: TestObject = jsonMapper.fromJsonString(jsonString, type)
        assertNotNull(result)
        assertEquals("John Doe", result.name)
        assertEquals(30, result.age)
    }

    @Test
    fun testFromJsonString_invalidJson() {
        val jsonString = """{"name":"John Doe","age":"thirty"}"""
        val type: Type = TestObject::class.java
        val exception = assertThrows(Throwable::class.java) {
            jsonMapper.fromJsonString<TestObject>(jsonString, type)
        }
    }

    @Test
    fun testToJsonString_validObject() {
        val testObject = TestObject("John Doe", 30)
        val type: Type = TestObject::class.java
        val jsonString = jsonMapper.toJsonString(testObject, type)
        assertNotNull(jsonString)
        assertTrue(jsonString.contains("John Doe"))
        assertTrue(jsonString.contains("30"))
    }

}

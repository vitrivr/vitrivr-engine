package org.vitrivr.engine.core.database.descriptor.struct

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.vitrivr.engine.core.database.AbstractDatabaseTest
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.FileSourceMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.basics.ComparisonOperator
import org.vitrivr.engine.core.model.query.bool.SimpleBooleanQuery
import org.vitrivr.engine.core.model.query.fulltext.SimpleFulltextQuery
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.types.Value
import java.nio.file.Paths
import java.util.*

/**
 * An [AbstractDatabaseTest] that tests for basic boolean queries on [FileSourceMetadataDescriptor].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@Suppress("UNCHECKED_CAST")
abstract class AbstractFileMetadataDescriptorReaderTest(schemaPath: String) : AbstractDatabaseTest(schemaPath) {

    companion object {
        /* Define a list of possible root directories. */
        val ROOTS = arrayOf(
            "/home",
            "/home/test",
            "/home/user/",
            "/home/admin/",
            "/tmp/",
            "/var/log/",
            "/var/",
            "/usr/local/bin/",
            "/var/opt",
            "/etc",
            "/tank",
            "/mnt/tank",
            "/usr",
            "/usr/lib"
        )

        /* Define a list of possible root directories. */
        val FILENAMES = listOf(
            "file1.txt",
            "test.txt",
            "document.pdf",
            "image.png",
            "image.jpg",
            "image.jpeg",
            "notes.docx",
            "video.mp4",
            "audio.mp3",
            "test.jpg",
            "test.png"
        )
    }

    /** The [Schema.Field] used for this [AbstractFileMetadataDescriptorReaderTest]. */
    private val field: Schema.Field<*, FileSourceMetadataDescriptor> = this.testSchema["file"]!! as Schema.Field<*, FileSourceMetadataDescriptor>

    /**
     * Tests for equals comparison.
     */
    @Test
    fun testBooleanQueryEquals() {
        val writer = this.testConnection.getDescriptorWriter(this.field)
        val reader = this.testConnection.getDescriptorReader(this.field)
        val random = SplittableRandom()

        /* Generate and store test data. */
        val descriptors = this.initialize(writer, random)
        val d = descriptors[random.nextInt(0, descriptors.size)]

        /* Prepare and execute query. */
        val query = SimpleBooleanQuery(
            d.path,
            ComparisonOperator.EQ,
            "path"
        )

        /* Check results. */
        val result = reader.query(query).toList()
        Assertions.assertTrue(result.isNotEmpty())
        for (r in result) {
            Assertions.assertEquals(d.path, r.path)
        }
    }

    /**
     * Tests for LIKE comparison.
     */
    @Test
    fun testBooleanQueryLike() {
        val writer = this.testConnection.getDescriptorWriter(this.field)
        val reader = this.testConnection.getDescriptorReader(this.field)
        val random = SplittableRandom()

        /* Generate and store test data. */
        this.initialize(writer, random)

        /* Prepare and execute query. */
        val query = SimpleBooleanQuery(
            Value.String("%.jpg"),
            ComparisonOperator.LIKE,
            "path"
        )

        /* Check results. */
        val result = reader.query(query).toList()
        Assertions.assertTrue(result.isNotEmpty())
        for (r in result) {
            Assertions.assertTrue(r.path.value.endsWith(".jpg"))
        }
    }

    /**
     * Tests for greater-than comparison.
     */
    @Test
    fun testBooleanQueryGreater() {
        val writer = this.testConnection.getDescriptorWriter(this.field)
        val reader = this.testConnection.getDescriptorReader(this.field)
        val random = SplittableRandom()

        /* Generate and store test data. */
        this.initialize(writer, random)

        /* Prepare and execute query. */
        val size = Value.Long(random.nextLong(0, 100_000_000L))
        val query = SimpleBooleanQuery(
            size,
            ComparisonOperator.GR,
            "size"
        )

        /* Check results. */
        val result = reader.query(query).toList()
        Assertions.assertTrue(result.isNotEmpty())
        for (r in result) {
            Assertions.assertTrue(r.size.value > size.value)
        }
    }

    /**
     * Tests for less-than comparison.
     */
    @Test
    fun testBooleanQueryLess() {
        val writer = this.testConnection.getDescriptorWriter(this.field)
        val reader = this.testConnection.getDescriptorReader(this.field)
        val random = SplittableRandom()

        /* Generate and store test data. */
        this.initialize(writer, random)

        /* Prepare and execute query. */
        val size = Value.Long(random.nextLong(0, 100_000_000L))
        val query = SimpleBooleanQuery(
            size,
            ComparisonOperator.LE,
            "size"
        )

        /* Check results. */
        val result = reader.query(query).toList()
        Assertions.assertTrue(result.isNotEmpty())
        for (r in result) {
            Assertions.assertTrue(r.size.value < size.value)
        }
    }

    /**
     * Tests for less-than comparison.
     */
    @Test
    fun testFulltextQuery() {
        val writer = this.testConnection.getDescriptorWriter(this.field)
        val reader = this.testConnection.getDescriptorReader(this.field)
        val random = SplittableRandom()

        /* Generate and store test data. */
        this.initialize(writer, random)

        /* Prepare and execute query. */
        val query = SimpleFulltextQuery(
            Value.Text("var"),
            "path"
        )

        /* Check results. */
        val result = reader.query(query).toList()
        // TODO enable Assertions.assertTrue(result.isNotEmpty())
        for (r in result) {
            Assertions.assertTrue(r.path.value.contains("var"))
        }
    }

    /**
     * Initializes the test data.
     */
    private fun initialize(writer: DescriptorWriter<FileSourceMetadataDescriptor>, random: SplittableRandom): List<FileSourceMetadataDescriptor> {
        val size = random.nextInt(500, 5000)

        /* Generate and store test data. */
        val retrievables = (0 until size).map {
            Ingested(UUID.randomUUID(), "SOURCE:TEST", emptyList(), emptySet(), emptySet(), emptySet(), true)
        }
        Assertions.assertTrue(this.testConnection.getRetrievableWriter().addAll(retrievables))

        val descriptors = retrievables.map {
            FileSourceMetadataDescriptor(UUID.randomUUID(), it.id, this.generateRandomPath(random), Value.Long(random.nextLong(0, 100_000_000L)), this.field)
        }
        Assertions.assertTrue(writer.addAll(descriptors))
        return descriptors
    }

    /**
     * Generates a random path and wraps it in a Value.String.
     *
     * @return A Value.String representing a random path.
     */
    private fun generateRandomPath(random: SplittableRandom): Value.String {
        /* Define a list of possible root directories. */
        val root = ROOTS[random.nextInt(ROOTS.size)]
        val fileName = FILENAMES[random.nextInt(FILENAMES.size)]

        /* Define a list of possible root directories. */
        val path = Paths.get(root, fileName).toString()

        /* Define a list of possible root directories. */
        return Value.String(path)
    }


    /**
     * Cleans up the database after each test.
     */
    @BeforeEach
    open fun prepare() {
        this.testConnection.getRetrievableInitializer().initialize()
        this.testConnection.getDescriptorInitializer(this.field).initialize()
    }

    /**
     * Cleans up the database after each test.
     *
     */
    @AfterEach
    open fun cleanup() {
        this.testConnection.getDescriptorInitializer(this.field).deinitialize()
        this.testConnection.getRetrievableInitializer().deinitialize()
    }
}
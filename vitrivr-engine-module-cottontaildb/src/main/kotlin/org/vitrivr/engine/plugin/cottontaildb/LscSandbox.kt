package org.vitrivr.engine.plugin.cottontaildb

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import org.vitrivr.cottontail.client.SimpleClient
import org.vitrivr.cottontail.client.language.basics.Direction
import org.vitrivr.cottontail.client.language.dml.Insert
import org.vitrivr.cottontail.client.language.dql.Query
import org.vitrivr.cottontail.core.database.Name
import org.vitrivr.cottontail.core.values.DateValue
import org.vitrivr.cottontail.core.values.PublicValue
import org.vitrivr.cottontail.core.values.StringValue
import org.vitrivr.cottontail.core.values.UuidValue
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.TextStyle
import java.util.*

object LscSandbox {

    val retrievableEntityName = Name.EntityName.create("lsc", "retrievable")
    val relationshipEntityName = Name.EntityName.create("lsc", "relationships")
    val fileEntityName = Name.EntityName.create("lsc", "descriptor_file")
    val dayEntityName = Name.EntityName.create("lsc", "descriptor_day")
    val timeEntityName = Name.EntityName.create("lsc", "descriptor_time")

    val checkDF = SimpleDateFormat("YYYYMMDD")

    @JvmStatic
    fun main(args: Array<String>) {

        val channel: ManagedChannel = ManagedChannelBuilder.forAddress("127.0.0.1", 1865)
            .enableFullStreamDecompression()
            .usePlaintext()
            .build()

        val client = SimpleClient(channel)

//        val q = Delete(retrievableEntityName).where(
//            Compare(
//            Column(Name.ColumnName.create("lsc", "retrievable", "type")),
//            Compare.Operator.EQUAL,
//            Literal(StringValue("DAY"))),
//        )
//        client.delete(q).forEach { println(it.values()) }
//        return

        val qFile = Query(fileEntityName)
            .select("*").order("path", Direction.ASC)

        val result = client.query(qFile)
        val tuples = result.toList()
        println("Time: ${result.queryDuration}")
        println("Results: ${tuples.size}")

        var day = "";
        var dayRetId = UUID.randomUUID()

        tuples.forEach {
            val date = convertToDate(it["path"].toString())
            val checkDay = checkDF.format(date)
            val retrievableId = UUID.fromString(it["retrievableid"].toString())
            if(day == checkDay) {
                /* only relationship */
                val rel = RelationshipEntity(retrievableId, "partOf", dayRetId)
                val iRel = Insert(relationshipEntityName).values(*rel.toPairs())
                val tx = client.begin(false)
                try {
                    client.insert(iRel.txId(tx))
                    client.commit(tx)
                    println("Inserted: $day")
                }catch(e: Exception){
                    client.rollback(tx)
                    e.printStackTrace()
                    System.err.println("Error: ${e.message}" )
                }
            }else{
                /* day retrievable, day descriptor, relationship */
                day = checkDay
                dayRetId = UUID.randomUUID()
                val (day, weekday) = convert(date)
                val (time, phase) = convertToTime(date)
                val dayDesc = DayEntity(dayRetId, day, weekday)
                val dayRet = RetrievableEntity(dayRetId, "DAY")
                val rel = RelationshipEntity(retrievableId, "partOf", dayRetId)
                val iRel = Insert(relationshipEntityName).values(*rel.toPairs())
                val iDesc = Insert(dayEntityName).values(*dayDesc.toPairs())
                val iRet = Insert(retrievableEntityName).values(*dayRet.toPairs())
                val tx = client.begin(false)
                try {
                    client.insert(iDesc.txId(tx))
                    client.insert(iRet.txId(tx))
                    client.insert(iRel.txId(tx))
                    client.commit(tx)
                    println("Inserted NEW: $day")
                } catch (e: Exception) {
                    client.rollback(tx)
                    e.printStackTrace()
                    System.err.println("Error: ${e.message}" )
                }
            }
        }

    }

    val df = SimpleDateFormat("yyyyMMdd_HHmmss")

    private fun convertToDate(path: String): Date {
        val pathSep = if (path.contains("\\")) {
            "\\"
        } else {
            "/"
        }
        val dateInfo = path.substring(path.lastIndexOf(pathSep) + 1, path.lastIndexOf("."))
        // println("DateInfo: $dateInfo")
        return df.parse(dateInfo)
    }

    private fun convert(date: Date): Pair<Date, String> {
        val ld = LocalDate.ofInstant(date.toInstant(), ZoneOffset.UTC)
        val dayOfWeek = ld.dayOfWeek.getDisplayName(
            TextStyle.FULL, Locale.UK
        )
        val day = Date.from(ld.atStartOfDay().toInstant(ZoneOffset.UTC))
        // println("Day: $day")
        // println("Day of Week: $dayOfWeek")
        return day to dayOfWeek
    }

    private fun convertToTime(date: Date): Pair<Date, String> {
        val ldt = LocalDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC)
        val timeDate = Date.from(ldt.toInstant(ZoneOffset.UTC))
        val phase = when (ldt.hour) {
            in 0 until 7 -> "NIGHT"
            in 22..23 -> "NIGHT"
            in 7 until 12 -> "MORNING"
            12 -> "NOON"
            in 13 until 17 -> "AFTERNOON"
            in 17 until 22 -> "EVENING"
            else -> "${ldt.hour}"
        }
        return timeDate to phase
    }

    data class RetrievableEntity(val retrievableId: UUID, val type: String) {
        fun toPairs(): Array<Pair<String, PublicValue>> {
            return listOf(
                "retrievableid" to UuidValue(retrievableId),
                "type" to StringValue(type)
            ).toTypedArray()
        }
    }

    data class RelationshipEntity(val subjectId: UUID, val predicate: String, val objectId: UUID) {
        fun toPairs(): Array<Pair<String, PublicValue>> {
            return listOf(
                "subjectid" to UuidValue(subjectId),
                "predicate" to StringValue(predicate),
                "objectid" to UuidValue(objectId)
            ).toTypedArray()
        }
    }

    data class DayEntity(
        val retrievableId: UUID,
        val day: Date,
        val weekday: String
    ) {
        fun toPairs(): Array<Pair<String, PublicValue>> {
            return listOf(
                "descriptorid" to UuidValue(UUID.randomUUID()),
                "retrievableid" to UuidValue(retrievableId),
                "day" to DateValue(day),
                "dayofweek" to StringValue(weekday)
            ).toTypedArray()
        }
    }

    data class TimeEntity(
        val retrievableId: UUID,
        val time: Date,
        val phaseOfDay: String
    ) {
        fun toPairs(): Array<Pair<String, PublicValue>> {
            return listOf(
                "descriptorid" to UuidValue(UUID.randomUUID()),
                "retrievableid" to UuidValue(retrievableId),
                "time" to DateValue(time),
                "phaseofday" to StringValue(phaseOfDay)
            ).toTypedArray()
        }
    }


}

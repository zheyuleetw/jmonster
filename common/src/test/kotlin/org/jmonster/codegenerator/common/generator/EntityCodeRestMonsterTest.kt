package org.jmonster.codegenerator.common.generator

import org.jmonster.codegenerator.common.model.dto.entity.Column
import org.jmonster.codegenerator.common.model.dto.entity.EntityCodeGenerateByTableRequestDto
import org.jmonster.codegenerator.common.monster.EntityCodeRestMonster
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class EntityCodeRestMonsterTest {

    @Test
    fun test_generate_by_table_annotations() {

        // mock data
        val request = EntityCodeGenerateByTableRequestDto(
            tableName = "shiba",
            enversAudit = true,
            columns = listOf(
                Column(
                    key = "PK",
                    name = "shiba_id",
                    type = "UUID",
                    nullable = "notnull",
                    description = "柴犬ID",
                ),
                Column(
                    key = "PK",
                    name = "shiba_name",
                    type = "varchar(200)",
                    nullable = "notnull",
                    description = "柴犬名稱",
                ),
            )
        )

        // execute
        val result = EntityCodeRestMonster(request).produce()

        // assert
        assertAll(
            { assert(result.any { it.contains("@Entity") }) { "Failed to generate annotation @Entity" } },
            { assert(result.any { it.contains("@Audited") }) { "Failed to generate annotation @Audited" } },
            { assert(result.any { it.contains("@IdClass(shibaPrimaryKey::class)") }) { "Failed to generate annotation @IdClass" } },
        )
    }


    @Test
    fun test_generate_by_table_table_name() {

        // mock data
        val request = EntityCodeGenerateByTableRequestDto(
            tableName = "shiba",
            enversAudit = true,
            columns = listOf(
                Column(
                    key = "PK",
                    name = "shiba_id",
                    type = "UUID",
                    nullable = "notnull",
                    description = "柴犬ID",
                ),
            )
        )

        // execute
        val result = EntityCodeRestMonster(request).produce()

        // assert
        val idIndex = result.indexOf("@Table(name = \"tbl_shiba\")")
        assertAll(
            { assert(idIndex != -1) { "Failed to generate table name" } },
        )
    }


    @Test
    fun test_generate_by_table_PK_uuid() {

        // mock data
        val request = EntityCodeGenerateByTableRequestDto(
            tableName = "shiba",
            enversAudit = true,
            columns = listOf(
                Column(
                    key = "PK",
                    name = "shiba_id",
                    type = "uuid",
                    nullable = "notnull",
                    description = "柴犬ID",
                ),
            )
        )

        // execute
        val result = EntityCodeRestMonster(request).produce()

        // assert
        val idIndex = result.indexOf("\t@Id")
        assertAll(
            { assert(idIndex != -1) { "Failed to generate @Id [UUID]" } },
            {
                assert(result[idIndex + 1].contains("@Column(name = \"shiba_id\")")) { "Failed to generate @Column [UUID]" }
            },
            { assert(result[idIndex + 2].contains("var shibaId: UUID,")) { "Failed to generate field [UUID]" } },

            )

    }

    @Test
    fun test_generate_by_table_varchar_200() {

        // mock data
        val request = EntityCodeGenerateByTableRequestDto(
            tableName = "shiba",
            enversAudit = true,
            columns = listOf(
                Column(
                    key = "",
                    name = "shiba_id",
                    type = "varchar(200)",
                    nullable = "nullable",
                    description = "柴犬名稱",
                ),
            )
        )

        // execute
        val result = EntityCodeRestMonster(request).produce()

        // assert
        val idIndex = result.indexOf("\t@Column(name = \"shiba_id\", length=200)")
        assertAll(
            { assert(idIndex != -1) { "Failed to generate @Column [varchar(200)?]" } },
            { assert(result[idIndex + 1].contains("var shibaId: String?,")) { "Failed to generate field [String?]" } },
        )

    }

    @Test
    fun test_generate_by_table_numeric_precision_19_scale_2() {

        // mock data
        val request = EntityCodeGenerateByTableRequestDto(
            tableName = "shiba",
            enversAudit = true,
            columns = listOf(
                Column(
                    key = "",
                    name = "shiba_deposit",
                    type = "numeric(19,2)",
                    nullable = "nullable",
                    description = "柴犬存款",
                ),
            )
        )

        // execute
        val result = EntityCodeRestMonster(request).produce()

        // assert
        val idIndex = result.indexOf("\t@Column(name = \"shiba_deposit\", precision=19, scale=2)")
        assertAll(
            { assert(idIndex != -1) { "Failed to generate @Column [numeric(19,2)]" } },
            { assert(result[idIndex + 1].contains("var shibaDeposit: BigDecimal?,")) { "Failed to generate field [BigDecimal?]" } },
        )

    }

    @Test
    fun test_generate_by_table_bool() {

        // mock data
        val request = EntityCodeGenerateByTableRequestDto(
            tableName = "shiba",
            enversAudit = true,
            columns = listOf(
                Column(
                    key = "",
                    name = "cute",
                    type = "bool",
                    nullable = "notnull",
                    description = "是否可愛",
                ),
            )
        )

        // execute
        val result = EntityCodeRestMonster(request).produce()

        // assert
        val idIndex = result.indexOf("\t@Column(name = \"cute\", nullable = false)")
        assertAll(
            { assert(idIndex != -1) { "Failed to generate @Column [bool]" } },
            { assert(result[idIndex + 1].contains("var cute: Boolean,")) { "Failed to generate field [Boolean?]" } },
        )

    }

    @Test
    fun test_generate_by_table_timestamp() {

        // mock data
        val request = EntityCodeGenerateByTableRequestDto(
            tableName = "shiba",
            enversAudit = true,
            columns = listOf(
                Column(
                    key = "",
                    name = "shiba_birth_day",
                    type = "timestamp",
                    nullable = "notnull",
                    description = "柴犬生日",
                ),
            )
        )

        // execute
        val result = EntityCodeRestMonster(request).produce()

        // assert
        val idIndex = result.indexOf("\t@Column(name = \"shiba_birth_day\", nullable = false)")
        assertAll(
            { assert(idIndex != -1) { "Failed to generate @Column [timestamp]" } },
            { assert(result[idIndex + 1].contains("var shibaBirthDay: LocalDateTime,")) { "Failed to generate field [LocalDateTime]" } },
        )

    }

    @Test
    fun test_generate_by_table_datetime() {

        // mock data
        val request = EntityCodeGenerateByTableRequestDto(
            tableName = "shiba",
            enversAudit = true,
            columns = listOf(
                Column(
                    key = "",
                    name = "shiba_birth_day",
                    type = "datetime",
                    nullable = "notnull",
                    description = "柴犬生日",
                ),
            )
        )

        // execute
        val result = EntityCodeRestMonster(request).produce()

        // assert
        val idIndex = result.indexOf("\t@Column(name = \"shiba_birth_day\", nullable = false)")
        assertAll(
            { assert(idIndex != -1) { "Failed to generate @Column [datetime]" } },
            { assert(result[idIndex + 1].contains("var shibaBirthDay: LocalDateTime,")) { "Failed to generate field [LocalDateTime]" } },
        )

    }

    @Test
    fun test_generate_by_table_date() {

        // mock data
        val request = EntityCodeGenerateByTableRequestDto(
            tableName = "shiba",
            enversAudit = true,
            columns = listOf(
                Column(
                    key = "",
                    name = "shiba_day",
                    type = "date",
                    nullable = "notnull",
                    description = "世界柴犬生日",
                ),
            )
        )

        // execute
        val result = EntityCodeRestMonster(request).produce()

        // assert
        val idIndex = result.indexOf("\t@Column(name = \"shiba_day\", nullable = false)")
        assertAll(
            { assert(idIndex != -1) { "Failed to generate @Column [date]" } },
            { assert(result[idIndex + 1].contains("var shibaDay: LocalDate,")) { "Failed to generate field [LocalDate]" } },
        )

    }

    @Test
    fun test_generate_by_table_time() {

        // mock data
        val request = EntityCodeGenerateByTableRequestDto(
            tableName = "shiba",
            enversAudit = true,
            columns = listOf(
                Column(
                    key = "",
                    name = "shiba_time",
                    type = "time",
                    nullable = "notnull",
                    description = "世界柴犬時",
                ),
            )
        )

        // execute
        val result = EntityCodeRestMonster(request).produce()

        // assert
        val idIndex = result.indexOf("\t@Column(name = \"shiba_time\", nullable = false)")
        assertAll(
            { assert(idIndex != -1) { "Failed to generate @Column [time]" } },
            { assert(result[idIndex + 1].contains("var shibaTime: LocalTime,")) { "Failed to generate field [LocalTime]" } },
        )

    }

    @Test
    fun test_generate_by_table_text() {

        // mock data
        val request = EntityCodeGenerateByTableRequestDto(
            tableName = "shiba",
            enversAudit = true,
            columns = listOf(
                Column(
                    key = "",
                    name = "shiba_say",
                    type = "text",
                    nullable = "notnull",
                    description = "柴犬口頭禪",
                ),
            )
        )

        // execute
        val result = EntityCodeRestMonster(request).produce()

        // assert
        val idIndex = result.indexOf("\t@Column(name = \"shiba_say\", nullable = false, columnDefinition = \"text\")")
        assertAll(
            { assert(idIndex != -1) { "Failed to generate @Column [text]" } },
            { assert(result[idIndex + 1].contains("var shibaSay: String,")) { "Failed to generate field [String]" } },
        )

    }

    @Test
    fun test_generate_by_table_int() {

        // mock data
        val request = EntityCodeGenerateByTableRequestDto(
            tableName = "shiba",
            enversAudit = true,
            columns = listOf(
                Column(
                    key = "",
                    name = "shiba_lucky_number",
                    type = "int",
                    nullable = "notnull",
                    description = "柴犬幸運數字",
                ),
            )
        )

        // execute
        val result = EntityCodeRestMonster(request).produce()

        // assert
        val idIndex = result.indexOf("\t@Column(name = \"shiba_lucky_number\", nullable = false)")
        assertAll(
            { assert(idIndex != -1) { "Failed to generate @Column [int]" } },
            { assert(result[idIndex + 1].contains("var shibaLuckyNumber: Int,")) { "Failed to generate field [Int]" } },
        )

    }

    @Test
    fun test_generate_by_table_bigint() {

        // mock data
        val request = EntityCodeGenerateByTableRequestDto(
            tableName = "shiba",
            enversAudit = true,
            columns = listOf(
                Column(
                    key = "",
                    name = "shiba_lucky_number",
                    type = "bigint",
                    nullable = "notnull",
                    description = "柴犬幸運數字",
                ),
            )
        )

        // execute
        val result = EntityCodeRestMonster(request).produce()

        // assert
        val idIndex = result.indexOf("\t@Column(name = \"shiba_lucky_number\", nullable = false)")
        assertAll(
            { assert(idIndex != -1) { "Failed to generate @Column [bigint]" } },
            { assert(result[idIndex + 1].contains("var shibaLuckyNumber: Long,")) { "Failed to generate field [Long]" } },
        )

    }

    @Test
    fun test_generate_by_table_double_precision() {

        // mock data
        val request = EntityCodeGenerateByTableRequestDto(
            tableName = "shiba",
            enversAudit = true,
            columns = listOf(
                Column(
                    key = "",
                    name = "shiba_lucky_number",
                    type = "double precision",
                    nullable = "notnull",
                    description = "柴犬幸運數字",
                ),
            )
        )

        // execute
        val result = EntityCodeRestMonster(request).produce()

        // assert
        val idIndex = result.indexOf("\t@Column(name = \"shiba_lucky_number\", nullable = false)")
        assertAll(
            { assert(idIndex != -1) { "Failed to generate @Column [double precision]" } },
            { assert(result[idIndex + 1].contains("var shibaLuckyNumber: Double,")) { "Failed to generate field [Double]" } },
        )

    }

    @Test
    fun test_generate_by_table_float8() {

        // mock data
        val request = EntityCodeGenerateByTableRequestDto(
            tableName = "shiba",
            enversAudit = true,
            columns = listOf(
                Column(
                    key = "",
                    name = "shiba_lucky_number",
                    type = "float8",
                    nullable = "notnull",
                    description = "柴犬幸運數字",
                ),
            )
        )

        // execute
        val result = EntityCodeRestMonster(request).produce()

        // assert
        val idIndex = result.indexOf("\t@Column(name = \"shiba_lucky_number\", nullable = false)")
        assertAll(
            { assert(idIndex != -1) { "Failed to generate @Column [float8]" } },
            { assert(result[idIndex + 1].contains("var shibaLuckyNumber: Double,")) { "Failed to generate field [Double]" } },
        )

    }

    @Test
    fun test_generate_by_table_real() {

        // mock data
        val request = EntityCodeGenerateByTableRequestDto(
            tableName = "shiba",
            enversAudit = true,
            columns = listOf(
                Column(
                    key = "",
                    name = "shiba_lucky_number",
                    type = "real",
                    nullable = "notnull",
                    description = "柴犬幸運數字",
                ),
            )
        )

        // execute
        val result = EntityCodeRestMonster(request).produce()

        // assert
        val idIndex = result.indexOf("\t@Column(name = \"shiba_lucky_number\", nullable = false)")
        assertAll(
            { assert(idIndex != -1) { "Failed to generate @Column [real]" } },
            { assert(result[idIndex + 1].contains("var shibaLuckyNumber: Float,")) { "Failed to generate field [Float]" } },
        )

    }

    @Test
    fun test_generate_by_table_bytea() {

        // mock data
        val request = EntityCodeGenerateByTableRequestDto(
            tableName = "shiba",
            enversAudit = true,
            columns = listOf(
                Column(
                    key = "",
                    name = "shiba_lucky_number_array",
                    type = "bytea",
                    nullable = "notnull",
                    description = "柴犬幸運數字",
                ),
            )
        )

        // execute
        val result = EntityCodeRestMonster(request).produce()

        // assert
        val idIndex = result.indexOf("\t@Column(name = \"shiba_lucky_number_array\", nullable = false)")
        assertAll(
            { assert(idIndex != -1) { "Failed to generate @Column [bytea]" } },
            { assert(result[idIndex + 1].contains("var shibaLuckyNumberArray: ByteArray,")) { "Failed to generate field [ByteArray]" } },
        )

    }


}
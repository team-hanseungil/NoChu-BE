package team.hanseungil.nochu.domain.emotion.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class EmotionMapMapper : AttributeConverter<Map<String, Double>, String> {
    private val mapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: Map<String, Double>?): String {
        return mapper.writeValueAsString(attribute ?: emptyMap<String, Double>())
    }

    override fun convertToEntityAttribute(dbData: String?): Map<String, Double> {
        if (dbData.isNullOrBlank()) return emptyMap()
        val javaType = mapper.typeFactory
            .constructMapType(Map::class.java, String::class.java, Double::class.java)
        return mapper.readValue(dbData, javaType)
    }
}